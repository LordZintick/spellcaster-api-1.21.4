package com.lordkittycat.loader;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class SpellPack {
    public static final SpellPackCollection PACKS = new SpellPackCollection();

    public static final class SpellPackCollection extends ArrayList<SpellPack> {
        @Nullable
        public SpellPack getByID(String id) {
            for (SpellPack pack : this) {
                if (Objects.equals(pack.type.id, id)) {
                    return pack;
                }
            }
            return null;
        }
    }

    private SpellPack(Type type) {
        this.type = type;
        PACKS.add(this);
    }

    public enum Type {
        ARROW("arrows"),
        SPECTRAL("spectral"),
        FANGS("fangs"),
        MOVEMENT("movement"),
        FIREBALLS("fireballs"),
        EFFECTS("effects"),
        OPERATOR("operator"),
        MISC("misc");
        public final String id;

        Type(String id) {
            this.id = id;
        }

        @Nullable
        public static Type getByID(String id) {
            for (Type type1 : Type.values()) {
                if (type1.id.equals(id)) {
                    return type1;
                }
            }
            return null;
        }
    }

    public final Type type;
    public boolean enabled;

    public static final SpellPack ARROW_PACK = new SpellPack(Type.ARROW);
    public static final SpellPack SPECTRAL_PACK = new SpellPack(Type.SPECTRAL);
    public static final SpellPack FANGS_PACK = new SpellPack(Type.FANGS);
    public static final SpellPack MOVEMENT_PACK = new SpellPack(Type.MOVEMENT);
    public static final SpellPack FIREBALLS_PACK = new SpellPack(Type.FIREBALLS);
    public static final SpellPack EFFECTS_PACK = new SpellPack(Type.EFFECTS);
    public static final SpellPack OPERATOR_PACK = new SpellPack(Type.OPERATOR);
    public static final SpellPack MISC_PACk = new SpellPack(Type.MISC);
}
