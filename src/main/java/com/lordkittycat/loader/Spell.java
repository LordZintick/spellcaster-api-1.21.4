package com.lordkittycat.loader;

import com.google.gson.JsonObject;
import com.lordkittycat.SpellCasterAPI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Spell implements StringConvertable {
    public final String displayName;
    public final float cooldown;
    public final String description;
    public final String ingredient;
    public final ArrayList<String> methods;
    public final JsonObject parameters;
    public Identifier id;

    public Spell(String displayName, float cooldown, String description, String ingredient, ArrayList<String> methods, JsonObject parameters) {
        this.displayName = displayName;
        this.cooldown = cooldown;
        this.description = description;
        this.ingredient = ingredient;
        this.methods = methods;
        this.parameters = parameters;
    }

    public Spell(String displayName, float cooldown, String ingredient, ArrayList<String> methods, JsonObject parameters) {
        this.displayName = displayName;
        this.cooldown = cooldown;
        this.ingredient = ingredient;
        this.methods = methods;
        this.parameters = parameters;
        this.description = "(no description)";
    }

    public record SpellParameterProvider(World world, PlayerEntity player, ItemStack stack) {
    }

    @Nullable
    private Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private Float tryParseFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private Boolean tryParseBool(String s) {
        return "true".equalsIgnoreCase(s) ? Boolean.TRUE : "false".equalsIgnoreCase(s) ? Boolean.FALSE : null;
    }

    private void runMethod(String methodID, SpellParameterProvider parameterProvider) {
        String[] methodIDSplit = methodID.split(":");
        if (methodIDSplit.length == 2) {
            String[] parameters = methodIDSplit[1].split("\\(")[1].replace(")", "").split(",");
            String collectionID = methodIDSplit[0];
            String actionID = methodIDSplit[1].split("\\(")[0];

            ArrayList<Class<?>> paramClasses = extractClasses(parameters);
            final ArrayList<Class<?>> finalClasses = new ArrayList<>();
            finalClasses.add(SpellParameterProvider.class);
            finalClasses.addAll(paramClasses);

            final ArrayList<Object> finalParameters = new ArrayList<>();
            finalParameters.add(parameterProvider);
            finalParameters.addAll(extractValues(parameters));

            for (Class<?> clazz : SpellLoader.SPELL_COLLECTIONS.getValues()) {
                Method method = null;
                for (Method searchMethod : clazz.getMethods()) {
                    if (searchMethod.isAnnotationPresent(SpellAction.class)) {
                        Annotation annot = searchMethod.getAnnotation(SpellAction.class);
                        if (annot instanceof SpellAction spellAction) {
                            if (Objects.equals(spellAction.id(), actionID)) {
                                method = searchMethod;
                            }
                        }
                    }
                }
                if (method != null) {
                    try {
                        method.invoke(this, finalParameters.toArray());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        SpellCasterAPI.LOGGER.error("An error occurred when casting spell: {}", e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    throw new IllegalArgumentException("Could not find method with ID: \"" + methodID + "\"");
                }
            }
        } else {
            throw new IllegalArgumentException("Method ID must be in this format: <spell collection id>:<spell action identifier>(<parameters>)");
        }
    }

    private @NotNull ArrayList<Class<?>> extractClasses(String[] parameters) {
        ArrayList<String> params = new ArrayList<>(List.of(parameters));
        ArrayList<Class<?>> paramClasses = new ArrayList<>();
        for (String param : params) {
            Class<?> clazz;
            SpellCasterAPI.LOGGER.info("Parsing parameter: {} to get class", param);
            if (tryParseInt(param) != null) {
                clazz = Integer.class;
            } else if (tryParseFloat(param) != null) {
                clazz = Float.class;
            } else if (tryParseBool(param) != null) {
                clazz = Boolean.class;
            } else {
                clazz = String.class;
            }
            paramClasses.add(clazz);
        }
        return paramClasses;
    }

    private @NotNull ArrayList<Object> extractValues(String[] parameters) {
        ArrayList<String> rawParams = new ArrayList<>(List.of(parameters));
        ArrayList<Object> params = new ArrayList<>();
        for (String param : rawParams) {
            SpellCasterAPI.LOGGER.info("Parsing parameter: {}", param);
            if (tryParseInt(param) != null) {
                params.add(Integer.parseInt(param));
            } else if (tryParseFloat(param) != null) {
                params.add(Float.parseFloat(param));
            } else if (tryParseBool(param) != null) {
                params.add(Boolean.parseBoolean(param));
            } else {
                params.add(param);
            }
        }
        return params;
    }

    public void cast(SpellParameterProvider parameterProvider) {
        SpellCasterAPI.LOGGER.info("Casting spell {}!", displayName);
        for (String methodID : methods) {
            runMethod(methodID, parameterProvider);
        }
    }

    @Override
    public String asString() {
        return "Spell: {\"displayName\": " + this.displayName + ",\"cooldown\": " + this.cooldown + ",\"methodID\":\n" + this.methods + "\n,\"ingredient\": " + ingredient + "}";
    }
}
