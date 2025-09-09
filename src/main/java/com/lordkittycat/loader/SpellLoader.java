package com.lordkittycat.loader;

import com.google.gson.Gson;
import com.lordkittycat.SpellCasterAPI;
import com.lordkittycat.StateManager;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
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
        public static final ArrayList<Spell> DEFAULT_SPELLS = new ArrayList<>();
        private boolean hasDefaults = true;

        public void updateHasDefaultsFromState(StateManager sm) {
            hasDefaults = sm.hasDefaultSpellPack;
        }
        
        static {
            DEFAULT_SPELLS.add(new Spell("Arrow Blast",
                    2f,
                    "Shoots a blast of arrows in every direction",
                    List.of("minecraft:arrow", "minecraft:tnt"),
                    List.of("spellcaster-api:shootEntities(minecraft;arrow,200,0.5,0.0,2.0)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Arrow Cluster",
                    2f,
                    "Shoots a cluster of arrows where you are looking",
                    List.of("minecraft:arrow", "minecraft:cobblestone"),
                    List.of("spellcaster-api:shootEntities(minecraft;arrow,25,2.5,0.0,0.1)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Arrow Stream",
                    3f,
                    "Shoots a continuous stream of arrows",
                    List.of("minecraft:arrow", "minecraft:iron_ingot"),
                    List.of("spellcaster-api:shootEntities(minecraft;arrow,25,2.0,0.05,0.01)"),
                    40)
            );
            DEFAULT_SPELLS.add(new Spell("Big Fireball",
                    1.5f,
                    "Shoots an explosive ghast fireball that can be reflected",
                    List.of("minecraft:tnt", "minecraft:fire_charge"),
                    List.of("spellcaster-api:fireball(0.75, true)"),
                    35,
                    "minecraft:flame")
            );
            DEFAULT_SPELLS.add(new Spell("Cookie Blast",
                    2f,
                    "Explodes into many, many cookies",
                    List.of("minecraft:tnt", "minecraft:cookie"),
                    List.of("spellcaster-api:shootItems(minecraft;cookie,200,0.5,0.0,2.0)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Dash",
                    6f,
                    "Grants extreme speed for a short time",
                    List.of("minecraft:sugar", "minecraft:sugar"),
                    List.of("spellcaster-api:addEffect(minecraft;speed,5,31)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Dragon Barrage",
                    3.5f,
                    "Shoots a barrage of dragon fireballs that erupt into poisonous gas",
                    List.of("minecraft:fire_charge", "minecraft:dragon_breath", "minecraft:bow"),
                    List.of("spellcaster-api:shootEntities(minecraft;dragon_fireball, 4, 0.7, 0.3, 0.0)"),
                    50,
                    "minecraft:flame")
            );
            DEFAULT_SPELLS.add(new Spell("Dragon Fireball",
                    1f,
                    "Shoots a dragon fireball that erupts into poisonous gas",
                    List.of("minecraft:fire_charge", "minecraft:dragon_breath"),
                    List.of("spellcaster-api:dragonFireball(1.5)"),
                    35,
                    "minecraft:flame")
            );
            DEFAULT_SPELLS.add(new Spell("Explosive Barrage",
                    4.5f,
                    "Shoots a barrage of explosive ghast fireballs that can be reflected",
                    List.of("minecraft:tnt", "minecraft:fire_charge", "minecraft:bow"),
                    List.of("spellcaster-api:shootEntities(minecraft;fireball, 5, 0.75, 0.25, 0.0)"),
                    65,
                    "minecraft:flame")
            );
            DEFAULT_SPELLS.add(new Spell("Fang Circle",
                    5f,
                    "Conjures a circle of evoker fangs around you",
                    List.of("minecraft:shears", "minecraft:diamond"),
                    List.of("spellcaster-api:fangs(5, true)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Fang Line",
                    2f,
                    "Conjures a line of evoker fangs where you are looking",
                    List.of("minecraft:shears", "minecraft:gold_ingot"),
                    List.of("spellcaster-api:fangs(10, false)"),
                    25)
            );
            DEFAULT_SPELLS.add(new Spell("Fireball",
                    0.5f,
                    "Shoots a blaze fireball that ignites whatever it hits but can be blocked with a shield",
                    List.of("minecraft:fire_charge"),
                    List.of("spellcaster-api:fireball(1.0, false)"),
                    5,
                    "minecraft:flame")
            );
            DEFAULT_SPELLS.add(new Spell("Fireball Barrage",
                    3.5f,
                    "Shoots a barrage of blaze fireballs where you are looking that ignite things but can be blocked with a shield",
                    List.of("minecraft:fire_charge", "minecraft:bow"),
                    List.of("spellcaster-api:shootEntities(minecraft;small_fireball, 8, 1.0, 0.15, 0.0)"),
                    50,
                    "minecraft:flame")
            );
            DEFAULT_SPELLS.add(new Spell("Gigantism",
                    10f,
                    "You listened to your parents and ate your vegetables, and look at you now!",
                    List.of("minecraft:command_block", "minecraft:enchanted_golden_apple"),
                    List.of("spellcaster-api:setScale(10.0,15.0)"),
                    100)
            );
            DEFAULT_SPELLS.add(new Spell("Grow",
                    5f,
                    "Grow to 4 blocks tall for a bit",
                    List.of("minecraft:seeds", "minecraft:golden_apple"),
                    List.of("spellcaster-api:setScale(2.0,30.0)"),
                    25)
            );
            DEFAULT_SPELLS.add(new Spell("Heal",
                    2.5f,
                    "Instantly heal some health",
                    List.of("minecraft:glistering_melon"),
                    List.of("spellcaster-api:addEffect(minecraft;instant_health,1,1)"),
                    25)
            );
            DEFAULT_SPELLS.add(new Spell("Invulnerability",
                    0.5f,
                    "Become completely immune to damage",
                    List.of("minecraft:command_block", "minecraft:shield"),
                    List.of("spellcaster-api:addEffect(minecraft;resistance,10000000,255)"),
                    100)
            );
            DEFAULT_SPELLS.add(new Spell("Large Fang Circle",
                    6f,
                    "Conjures a huge circle of evoker fangs around you",
                    List.of("minecraft:shears", "minecraft:diamond", "minecraft:emerald"),
                    List.of("spellcaster-api:fangs(15, true)"),
                    75)
            );
            DEFAULT_SPELLS.add(new Spell("Leap",
                    6f,
                    "Grants extreme jump power for a short time",
                    List.of("minecraft:slime_block", "minecraft:slime_ball"),
                    List.of("spellcaster-api:addEffect(minecraft;jump_boost,5,14)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Long Fang Line",
                    3f,
                    "Conjures a long line of evoker fangs where you are looking",
                    List.of("minecraft:shears", "minecraft:gold_ingot", "minecraft:emerald"),
                    List.of("spellcaster-api:fangs(25, false)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Night Vision",
                    5f,
                    "Allows you to see in the dark for a bit",
                    List.of("minecraft:golden_carrot"),
                    List.of("spellcaster-api:addEffect(minecraft;night_vision,120,0)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Regenerate",
                    5f,
                    "Regenerates a large portion of your health over eight seconds",
                    List.of("minecraft:ghast_tear"),
                    List.of("spellcaster-api:addEffect(minecraft;regeneration,8,2)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Resist",
                    5f,
                    "Grants fire+damage resistance for a short time",
                    List.of("minecraft:shield", "minecraft:magma_cream"),
                    List.of("spellcaster-api:addEffect(minecraft;resistance,30,1)", "spellcaster-api:addEffect(minecraft;fire_resistance,30,0)"),
                    75)
            );
            DEFAULT_SPELLS.add(new Spell("Saturate",
                    5f,
                    "Instantly refills your hunger",
                    List.of("minecraft:cooked_chicken", "minecraft:cooked_beef", "minecraft:cooked_mutton"),
                    List.of("spellcaster-api:addEffect(minecraft;saturation,1,255)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Shrink",
                    5f,
                    "Shrink to half height (1 block tall) for a bit",
                    List.of("minecraft:rotten_flesh", "minecraft:golden_apple"),
                    List.of("spellcaster-api:setScale(0.5,30.0)"),
                    25)
            );
            DEFAULT_SPELLS.add(new Spell("Smoke Screen",
                    2.5f,
                    "Makes you and nearby players invisible, and creates a cloud of smoke that makes it hard to see",
                    List.of("minecraft:white_wool", "minecraft:glass"),
                    List.of("/playsound block.wool.break player @a ~ ~ ~ 1 1",
                            "/execute positioned ~ ~ ~ run effect give @a[distance=0..2] invisibility 30 0 true",
                            "/particle cloud ~ ~ ~ 2 2 2 0 500 normal @a"),
                    50,
                    "minecraft:cloud")
            );
            DEFAULT_SPELLS.add(new Spell("Spectral Blast",
                    2f,
                    "Shoots a blast of spectral arrows in every direction",
                    List.of("minecraft:spectral_arrow", "minecraft:tnt"),
                    List.of("spellcaster-api:shootEntities(minecraft;spectral_arrow,200,0.5,0.0,2.0)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Spectral Cluster",
                    2f,
                    "Shoots a cluster of spectral arrows where you are looking",
                    List.of("minecraft:spectral_arrow", "minecraft:cobblestone"),
                    List.of("spellcaster-api:shootEntities(minecraft;spectral_arrow,25,2.5,0.0,0.1)"),
                    50)
            );
            DEFAULT_SPELLS.add(new Spell("Spectral Stream",
                    3f,
                    "Shoots a continuous stream of spectral arrows",
                    List.of("minecraft:spectral_arrow", "minecraft:iron_ingot"),
                    List.of("spellcaster-api:shootEntities(minecraft;spectral_arrow,25,2.0,0.05,0.01)"),
                    40)
            );
            DEFAULT_SPELLS.add(new Spell("Speedy Fireball",
                    1f,
                    "Shoots a fast blaze fireball that ignites whatever it hits but can be blocked by a shield",
                    List.of("minecraft:fire_charge", "minecraft:sugar"),
                    List.of("spellcaster-api:fireball(8.0, false)"),
                    10)
            );
            DEFAULT_SPELLS.add(new Spell("Teledash",
                    0.5f,
                    "Teleports forward 5 blocks",
                    List.of("minecraft:ender_pearl", "minecraft:sugar"),
                    List.of("spellcaster-api:teledash(5.0)"),
                    20)
            );
            DEFAULT_SPELLS.add(new Spell("Teleport",
                    1.5f,
                    "Shoots an ender pearl that teleports you. Watch out for endermites!",
                    List.of("minecraft:ender_pearl"),
                    List.of("spellcaster-api:teleport(2.0)"),
                    15)
            );
            DEFAULT_SPELLS.add(new Spell("Timeslow",
                    15f,
                    "Slows time for 10 seconds",
                    List.of("minecraft:command_block", "minecraft:clock", "minecraft:fermented_spider_eye"),
                    List.of("spellcaster-api:tickRate(10,10.0)"),
                    100)
            );
            DEFAULT_SPELLS.add(new Spell("Timestop",
                    15f,
                    "Stops time for 10 seconds",
                    List.of("minecraft:command_block", "minecraft:clock", "minecraft:ice"),
                    List.of("spellcaster-api:tickRate(0,10.0)"),
                    100)
            );
            DEFAULT_SPELLS.add(new Spell("Wither Skull",
                    1f,
                    "Shoots a wither skull that slightly explodes, inflicts the wither effect, and turns killed mobs into wither roses",
                    List.of("minecraft:wither_skeleton_skull"),
                    List.of("spellcaster-api:shootEntities(minecraft;wither_skull, 1, 2.0, 0.0, 0.0)"),
                    20)
            );
        }
        
        public SpellContainer() {
            addAll(DEFAULT_SPELLS);
        }

        public void removeDefaults() {
            this.removeIf(this::removeDefaultsCondition);
            if (hasDefaults) {
                hasDefaults = false;
            }
        }

        private boolean removeDefaultsCondition(Spell spell) {
            return DEFAULT_SPELLS.contains(spell) && hasDefaults;
        }

        public void addDefaults() {
            if (!hasDefaults) {
                hasDefaults = true;
                addAll(DEFAULT_SPELLS);
            }
        }

        public void castSpell(Identifier id, Spell.SpellParameterProvider parameterProvider) {
            boolean foundSpell = false;
            for (Spell spell : this) {
                if (Objects.equals(spell.id, id) && !foundSpell) {
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

    public static void reloadData(ResourceManager resourceManager) {
        SpellCasterAPI.LOGGER.info("Reloading spell data...");
        SpellContainer beforeSpellsNoDefaults = new SpellContainer();
        beforeSpellsNoDefaults.addAll(SPELLS);
        beforeSpellsNoDefaults.removeDefaults();
        BEFORE_RELOAD_SPELLS.clear();
        BEFORE_RELOAD_SPELLS.addAll(beforeSpellsNoDefaults);
        SPELLS.clear();
        SpellCasterAPI.LOGGER.info("Cleared spell data - Spells cleared: {}", BEFORE_RELOAD_SPELLS.size());
        if (SPELLS.hasDefaults) {
            SPELLS.addAll(SpellContainer.DEFAULT_SPELLS);
        }
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
}