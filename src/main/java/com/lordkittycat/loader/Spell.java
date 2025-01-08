package com.lordkittycat.loader;

import com.lordkittycat.SpellCasterAPI;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Spell implements StringConvertable {
    public final String displayName;
    public final float cooldown;
    public final ArrayList<String> actions;
    public final String description;

    public Identifier id;

    public Spell(String displayName, float cooldown, ArrayList<String> actions, String description) {
        this.displayName = displayName;
        this.cooldown = cooldown;
        this.actions = actions;
        this.description = description;
    }

    public Spell(String displayName, float cooldown, ArrayList<String> actions) {
        this.displayName = displayName;
        this.cooldown = cooldown;
        this.actions = actions;
        this.description = "(no description)";
    }

    public void cast(Object... params) {
        SpellCasterAPI.LOGGER.info("Casting spell {}!", displayName);
        for (String actionID : actions) {
            try {
                SpellAction action = SpellLoader.SPELL_ACTIONS.getByID(Identifier.of(actionID));
                action.activate(params);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error when casting spell: " + e.getMessage());
            }
        }
    }

    @Override
    public String asString() {
        return "Spell: {\"displayName\":" + this.displayName + ",\"cooldown\":" + this.cooldown + ",\"actions\":\n" + this.actions.toString() + "\n}";
    }
}
