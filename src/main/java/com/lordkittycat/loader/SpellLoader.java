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
    public static final SpellActionsList SPELL_ACTIONS = new SpellActionsList();
    public static final SpellsList SPELLS = new SpellsList();

    public static final class SpellActionsList extends ArrayList<SpellAction> {
        public SpellAction getByID(Identifier id) throws IllegalArgumentException {
            for (SpellAction spellAction : this) {
                if (Objects.equals(spellAction.id, id)) {
                    return spellAction;
                }
            }
            throw new IllegalArgumentException("Could not find spell action with action id: " + id.toString());
        }
    }

    public static final class SpellsList extends ArrayList<Spell> {
        public void castSpell(Identifier id, Object... params) {
            for (Spell spell : this) {
                if (Objects.equals(spell.id, id)) {
                    spell.cast(params);
                }
            }
            SpellCasterAPI.LOGGER.warn("Could not find spell with id: {}", id.toString());
        }
    }

    public static void reloadData(MinecraftServer server) {
        SpellCasterAPI.LOGGER.info("Reloading spell data...");
        int bfspells = SPELLS.size();
        int bfspellActions = SPELL_ACTIONS.size();
        SPELL_ACTIONS.clear();
        SPELLS.clear();
        SpellCasterAPI.LOGGER.info("Cleared spell data - Spells cleared: {}, Spell Actions cleared: {}", bfspells, bfspellActions);
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
                pack.findResources(ResourceType.SERVER_DATA, namespace, "spell_actions", (identifier, inputStreamInputSupplier) -> {
                    try {
                        SpellAction spellAction = gson.fromJson(new BufferedReader(new InputStreamReader(inputStreamInputSupplier.get(), StandardCharsets.UTF_8)), SpellAction.class);
                        spellAction.id = Identifier.of(identifier.toString().replace("spell_actions/", "").replace(".json",""));
                        SPELL_ACTIONS.add(spellAction);
                    } catch (IOException e) {
                        SpellCasterAPI.LOGGER.warn("An error occurred when creating spell action reader: " + e.getMessage());
                    }
                });
                pack.findResources(ResourceType.SERVER_DATA, namespace, "spells", (identifier, inputStreamInputSupplier) -> {
                    try {
                        Spell spell = gson.fromJson(new BufferedReader(new InputStreamReader(inputStreamInputSupplier.get(), StandardCharsets.UTF_8)), Spell.class);
                        spell.id = Identifier.of(identifier.toString().replace("spells/", "").replace(".json",""));
                        SPELLS.add(spell);
                    } catch (IOException e) {
                        SpellCasterAPI.LOGGER.warn("An error occurred when creating spell reader: " + e.getMessage());
                    }
                });
            }
        }
        SpellCasterAPI.LOGGER.info("Finished loading spell data - Spells loaded: {}, Spell Actions loaded: {}", SPELLS.size(), SPELL_ACTIONS.size());
    }
}