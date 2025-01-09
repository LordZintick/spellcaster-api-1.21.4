package com.lordkittycat.loader;

import com.google.gson.JsonObject;
import com.lordkittycat.SpellCasterAPI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class Spell implements StringConvertable {
    public final String displayName;
    public final float cooldown;
    public final String description;
    public final String ingredient;
    public final String methodID;
    public final JsonObject parameters;
    public Identifier id;

    public Spell(String displayName, float cooldown, String description, String ingredient, String methodID, JsonObject parameters) {
        this.displayName = displayName;
        this.cooldown = cooldown;
        this.description = description;
        this.ingredient = ingredient;
        this.methodID = methodID;
        this.parameters = parameters;
    }

    public Spell(String displayName, float cooldown, String ingredient, String methodID, JsonObject parameters) {
        this.displayName = displayName;
        this.cooldown = cooldown;
        this.ingredient = ingredient;
        this.methodID = methodID;
        this.parameters = parameters;
        this.description = "(no description)";
    }

    public record SpellParameterProvider(Map<String, Object> parameters, World world, PlayerEntity player, ItemStack stack) {
    }

    public void cast(SpellParameterProvider parameterProvider) {
        SpellCasterAPI.LOGGER.info("Casting spell {}!", displayName);
        String[] methodIDSplit = methodID.split(":");
        if (methodIDSplit.length == 2) {
            String className = methodIDSplit[0];
            String methodName = methodIDSplit[1];
            Map<String, Object> params = parameterProvider.parameters;

            ArrayList<Class<?>> paramClasses = new ArrayList<>();
            for (Object object : params.values()) {
                paramClasses.add(object.getClass());
            }
            ArrayList<Class<?>> shippedClasses = new ArrayList<>();
            shippedClasses.add(SpellParameterProvider.class);
            shippedClasses.addAll(paramClasses);

            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid class name: " + className + ", could not find class. Note that this needs to include the module as well (e.g. com.example.ExampleClass)");
            }
            Method method;
            try {
                method = clazz.getMethod(methodName, shippedClasses.toArray(Class[]::new));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Invalid method: " + methodName + ", could not find method in class " + className + " with parameters " + shippedClasses);
            }
            try {
                ArrayList<String> paramNames = new ArrayList<>();
                for (Object object : params.values()) {
                    paramNames.add(object.getClass().getSimpleName());
                }
                if (Objects.equals(paramNames, parameters)) {
                    method.invoke(this, parameterProvider);
                } else {
                    throw new IllegalArgumentException("Unable to cast spell: Provided parameters " + params + " and required parameters " + parameters.toString() + " do not match!");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                SpellCasterAPI.LOGGER.error("An error occurred when casting spell: {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Method ID must be in this format: <fully specified class name with package name>:<method name>");
        }
    }

    @Override
    public String asString() {
        return "Spell: {\"displayName\": " + this.displayName + ",\"cooldown\": " + this.cooldown + ",\"methodID\":\n" + this.methodID + "\n,\"ingredient\": " + ingredient + ",\"requiredParams\":" + this.parameters.toString() + "}";
    }
}
