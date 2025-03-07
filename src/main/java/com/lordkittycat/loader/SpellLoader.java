package com.lordkittycat.loader;

import com.google.gson.Gson;
import com.lordkittycat.SpellCasterAPI;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class SpellLoader {
    public static final SpellContainer SPELLS = new SpellContainer();
    private static final SpellContainer BEFORE_RELOAD_SPELLS = new SpellContainer();
    public static final BasicRegistry<Class<?>> SPELL_PROVIDERS = new BasicRegistry<>();

    public static final class BasicRegistry<T> {
        private final ArrayList<String> IDS = new ArrayList<>();
        private final ArrayList<T> VALUES = new ArrayList<>();

        /**
         * Registers "value" to the registry with an identifier of "id"
         * @param id The ID to register to
         * @param value The value to register
         */
        public void register(String id, T value) {
            if (!IDS.contains(id)) {
                IDS.add(id);
            } else {
                SpellCasterAPI.LOGGER.warn("Identifier is already registered: \"{}\"", id);
            }
            if (!VALUES.contains(value)) {
                VALUES.add(value);
            } else {
                SpellCasterAPI.LOGGER.warn("Value is already registered: \"{}\"", value.toString());
            }
        }

        public T get(String id) {
            if (IDS.contains(id)) {
                try {
                    return VALUES.get(IDS.indexOf(id));
                } catch (Exception e) {
                    SpellCasterAPI.LOGGER.warn("Could not get value for identifier: \"{}\"", id);
                    return null;
                }
            } else {
                SpellCasterAPI.LOGGER.warn("Could not find identifier: \"{}\"", id);
                return null;
            }
        }

        public String getID(T value) {
            if (VALUES.contains(value)) {
                try {
                    return IDS.get(VALUES.indexOf(value));
                } catch (Exception e) {
                    SpellCasterAPI.LOGGER.warn("Could not get identifier for value: \"{}\"", value.toString());
                    return "";
                }
            } else {
                SpellCasterAPI.LOGGER.warn("Could not find value: \"{}\"", value.toString());
                return "";
            }
        }

        public ArrayList<String> getIDs() {
            return IDS;
        }

        public ArrayList<T> getValues() {
            return VALUES;
        }
    }

    public static final class SpellContainer extends ArrayList<Spell> {
        public void castSpell(Identifier id, Spell.SpellParameterProvider parameterProvider) {
            boolean foundSpell = false;
            for (Spell spell : this) {
                if (Objects.equals(spell.id, id)) {
                    spell.cast(parameterProvider);
                    foundSpell = true;
                }
            }
            if (!foundSpell) {
                SpellCasterAPI.LOGGER.warn("Could not find spell with id: {}", id.toString());
            }
        }

        public Spell getByIngredients(ArrayList<Identifier> ingredients) {
            for (Spell spell : this) {
                ArrayList<String> ingredientsStrings = new ArrayList<>();
                for (Identifier id : ingredients) {ingredientsStrings.add(id.toString());}
                if (spell.ingredients.containsAll(ingredientsStrings) && spell.ingredients.size() == ingredients.size()) {
                    return spell;
                } else {
                    SpellCasterAPI.LOGGER.debug("Spell with ingredients: {} does not match ingredients provided: {}", spell.ingredients, ingredientsStrings);
                }
            }
            SpellCasterAPI.LOGGER.warn("Could not find spell with ingredients: {} (make sure that spell ingredients are valid identifiers)", ingredients.toString());
            return null;
        }

        @Nullable
        public Spell getByID(Identifier id) {
            for (Spell spell : this) {
                if (Objects.equals(spell.id, id)) {
                    return spell;
                }
            }
            return null;
        }
    }

    private String getResourceContentAsString(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            return content.toString();
        }
    }

    public static void reloadData(MinecraftServer server) {
        SpellCasterAPI.LOGGER.info("Reloading spell data...");
        int bfcount = SPELLS.size();
        SPELLS.clear();
        SpellCasterAPI.LOGGER.info("Cleared spell data - Spells cleared: {}", bfcount);
        loadData(server);
    }

    public static void loadData(MinecraftServer server) {
        Gson gson = new Gson();
        SpellCasterAPI.LOGGER.info("Loading spell data...");
        server.getDataPackManager().scanPacks();
        for (ResourcePack pack : server.getDataPackManager().createResourcePacks()) {
            for (String namespace : pack.getNamespaces(ResourceType.SERVER_DATA)) {
                pack.findResources(ResourceType.SERVER_DATA, namespace, "spells", (identifier, inputStreamInputSupplier) -> {
                    try {
                        Spell spell = gson.fromJson(new BufferedReader(new InputStreamReader(inputStreamInputSupplier.get(), StandardCharsets.UTF_8)), Spell.class);
                        spell.id = Identifier.of(identifier.toString().replace("spells/", "").replace(".json",""));
                        SPELLS.add(spell);
                    } catch (IOException e) {
                        SpellCasterAPI.LOGGER.warn("An error occurred when creating spell reader: {}", e.getMessage());
                    }
                });
            }
        }
        SpellCasterAPI.LOGGER.info("Finished loading spell data. Spells loaded: {}", SPELLS.size());
    }

    public static void reloadData(ResourceManager resourceManager) {
        SpellCasterAPI.LOGGER.info("Reloading spell data...");
        BEFORE_RELOAD_SPELLS.clear();
        BEFORE_RELOAD_SPELLS.addAll(SPELLS);
        SPELLS.clear();
        SpellCasterAPI.LOGGER.info("Cleared spell data - Spells cleared: {}", BEFORE_RELOAD_SPELLS.size());
        loadData(resourceManager);
    }

    public static void loadData(ResourceManager manager) {
        Gson gson = new Gson();
        SpellCasterAPI.LOGGER.info("Loading spell data...");
        ArrayList<Spell> changed = new ArrayList<>();
        Map<Identifier, Resource> resources = manager.findResources("spells", path -> path.getPath().endsWith(".json"));
        for(Resource resource : resources.values()) {
            Identifier id = new ArrayList<>(resources.keySet()).get(new ArrayList<>(resources.values()).indexOf(resource));
            try (InputStream stream = manager.open(id)) {
                InputStreamReader inputReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader bufReader = new BufferedReader(inputReader);
                Spell spell = gson.fromJson(bufReader, Spell.class);
                bufReader.close();
                inputReader.close();
                spell.id = Identifier.of(id.toString().replace("spells/", "").replace(".json",""));
                SPELLS.add(spell);
                Spell beforeSpell = BEFORE_RELOAD_SPELLS.getByID(id);
                if (beforeSpell != null) {
                    if (!Objects.equals(beforeSpell.asString(), spell.asString())) {
                        changed.add(spell);
                    }
                }
            } catch (Exception e) {
                SpellCasterAPI.LOGGER.error("An error occurred when loading spell: {}", e.getMessage());
                e.printStackTrace();
            }
        }
        SpellCasterAPI.LOGGER.info("Finished loading spell data. Spells loaded: {}, New Spells: {}, Removed Stale: {}, Changed: {}", SPELLS.size(), Math.max(0, SPELLS.size() - BEFORE_RELOAD_SPELLS.size()), Math.max(0, BEFORE_RELOAD_SPELLS.size() - SPELLS.size()), changed.size());
    }

    public static void loadAllSpellPacks(Path path) {
        Path source = Path.of(new File("").getAbsolutePath().replace("run", "") + "/spell-packs");
        for (File packFile : source.toFile().listFiles()) {
            String packname = packFile.getName();
            loadSpellPack(path, packname);
        }
    }

    public static void deleteAllSpellPacks(Path path) {
        for (File packFile : path.toFile().listFiles()) {
            String packname = packFile.getName();
            Path datapackPath = Path.of(path + "/" + packname);

            if (new File(datapackPath.toString()).exists()) {
                SpellCasterAPI.LOGGER.info("Removing spell pack \"" + packname + "\"");
                try {
                    FileUtils.deleteDirectory(new File(datapackPath.toString()));
                } catch (IOException e) {
                    SpellCasterAPI.LOGGER.error("An error occurred when removing spell pack: {}", e.getMessage());
                }
            }
        }
    }

    public static void deleteSpellPack(Path path, String packname) {
        Path datapackPath = Path.of(path + "/" + packname);

        if (new File(datapackPath.toString()).exists()) {
            SpellCasterAPI.LOGGER.info("Removing spell pack \"" + packname + "\"");
            try {
                FileUtils.deleteDirectory(new File(datapackPath.toString()));
            } catch (IOException e) {
                SpellCasterAPI.LOGGER.error("An error occurred when removing spell pack: {}", e.getMessage());
            }
        }
    }

    public static void refreshCurrentSpellPacks(Path path) {
        for (File packFile : path.toFile().listFiles()) {
            String packname = packFile.getName();
            Path datapackPath = Path.of(path + "/" + packname);
            SpellPack pack = SpellPack.PACKS.getByID(packname);
            assert pack != null;

            if (new File(datapackPath.toString()).exists()) {
                pack.enabled = true;
                SpellCasterAPI.LOGGER.info("Refreshing spell pack \"" + packname + "\"");
                try {
                    FileUtils.deleteDirectory(new File(datapackPath.toString()));
                } catch (IOException e) {
                    SpellCasterAPI.LOGGER.error("An error occurred when refreshing spell pack: {}", e.getMessage());
                }
                loadSpellPack(path, packname);
            } else {
                pack.enabled = false;
            }
        }
    }

    public static void loadSpellPack(Path path, SpellPack.Type type) {
        Path source = Path.of(new File("").getAbsolutePath().replace("run", "") + "/spell-packs/" + type.id);
        Path datapackPath = Path.of(path.toString() + "/" + type.id);
        if (!new File(datapackPath.toString()).exists()) {
            SpellCasterAPI.LOGGER.info("Creating spell pack...");
            try {
                if (Files.notExists(datapackPath)) {
                    Files.createDirectories(datapackPath);
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                    for (Path entry : stream) {
                        Path targetPath = datapackPath.resolve(entry.getFileName());
                        if (Files.isDirectory(entry)) {
                            FileUtils.copyDirectory(entry.toFile(), targetPath.toFile());
                        } else {
                            Files.copy(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            } catch (IOException e) {
                SpellCasterAPI.LOGGER.error("An error occurred when loading spell pack: {}", e.getMessage());
            }
        } else {
            SpellCasterAPI.LOGGER.info("Refreshing spell pack \"" + type.name() + "\"");
            try {
                FileUtils.deleteDirectory(new File(datapackPath.toString()));
            } catch (IOException e) {
                SpellCasterAPI.LOGGER.error("An error occurred when refreshing spell pack: {}", e.getMessage());
            }
            loadSpellPack(path, type);
        }
    }

    private static void loadSpellPack(Path path, String name) {
        Path source = Path.of(new File("").getAbsolutePath().replace("run", "") + "/spell-packs/" + name);
        Path datapackPath = Path.of(path.toString() + "/" + name);

        if (!new File(datapackPath.toString()).exists()) {
            SpellCasterAPI.LOGGER.info("Creating spell pack...");
            try {
                if (Files.notExists(datapackPath)) {
                    Files.createDirectories(datapackPath);
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                    for (Path entry : stream) {
                        Path targetPath = datapackPath.resolve(entry.getFileName());
                        if (Files.isDirectory(entry)) {
                            FileUtils.copyDirectory(entry.toFile(), targetPath.toFile());
                        } else {
                            Files.copy(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            } catch (IOException e) {
                SpellCasterAPI.LOGGER.error("An error occurred when loading spell pack: {}", e.getMessage());
            }
        } else {
            SpellCasterAPI.LOGGER.info("Refreshing spell pack...");
            try {
                FileUtils.deleteDirectory(new File(datapackPath.toString()));
            } catch (IOException e) {
                SpellCasterAPI.LOGGER.error("An error occurred when refreshing spell pack: {}", e.getMessage());
            }
            loadSpellPack(path, name);
        }
    }
}