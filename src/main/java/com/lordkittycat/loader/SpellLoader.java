package com.lordkittycat.loader;

import com.google.gson.Gson;
import com.lordkittycat.SpellCasterAPI;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class SpellLoader {
    public static final SpellContainer SPELLS = new SpellContainer();
    public static final BasicRegistry<Class<?>> SPELL_COLLECTIONS = new BasicRegistry<>();

    public static final class BasicRegistry<T> {
        private final ArrayList<String> IDS = new ArrayList<>();
        private final ArrayList<T> VALUES = new ArrayList<>();

        public void register(String id, T value) {
            if (!IDS.contains(id)) {
                IDS.add(id);
            } else {
                throw new IllegalArgumentException("Identifier is already registered: \"" + id + "\"");
            }
            if (!VALUES.contains(value)) {
                VALUES.add(value);
            } else {
                throw new IllegalArgumentException("Value is already registered: \"" + value.toString() + "\"");
            }
        }

        public T get(String id) {
            if (IDS.contains(id)) {
                try {
                    return VALUES.get(IDS.indexOf(id));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not get value for identifier: \"" + id + "\"");
                }
            } else {
                throw new IllegalArgumentException("Could not find identifier: \"" + id + "\"");
            }
        }

        public String getID(T value) {
            if (VALUES.contains(value)) {
                try {
                    return IDS.get(VALUES.indexOf(value));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not get identifier for value: \"" + value.toString() + "\"");
                }
            } else {
                throw new IllegalArgumentException("Could not find value: \"" + value.toString() + "\"");
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
            for (Spell spell : this) {
                if (Objects.equals(spell.id, id)) {
                    spell.cast(parameterProvider);
                }
            }
            throw new IllegalArgumentException("Could not find spell with id: " + id.toString());
        }

        public Spell getByIngredient(Identifier ingredient) {
            for (Spell spell : this) {
                if (Objects.equals(Identifier.of(spell.ingredient), ingredient)) {
                    return spell;
                }
            }
            throw new IllegalArgumentException("Could not find spell with ingredient: " + ingredient.toString() + " (make sure that spell ingredients are valid identifiers)");
        }

        public Spell getByID(Identifier id) {
            for (Spell spell : this) {
                if (Objects.equals(spell.id, id)) {
                    return spell;
                }
            }
            return null;
        }
    }

    public static void reloadData(MinecraftServer server) {
        SpellCasterAPI.LOGGER.info("Reloading spell data...");
        int bfspells = SPELLS.size();
        SPELLS.clear();
        SpellCasterAPI.LOGGER.info("Cleared spell data - Spells cleared: {}", bfspells);
        loadData(server);
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

    public static void loadData(MinecraftServer server) {
        Gson gson = new Gson();
        SpellCasterAPI.LOGGER.info("Loading spell data...");
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
}