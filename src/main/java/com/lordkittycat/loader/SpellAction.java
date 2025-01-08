package com.lordkittycat.loader;

import com.lordkittycat.SpellCasterAPI;
import net.minecraft.util.Identifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SpellAction implements StringConvertable {
    public final String methodID;
    public final String description;
    public final ArrayList<String> requiredParams;
    public Identifier id;

    public SpellAction(String methodID, String description, ArrayList<String> requiredParams) {
        this.methodID = methodID;
        this.description = description;
        this.requiredParams = requiredParams;
    }

    public void activate(Object... params) {
        SpellCasterAPI.LOGGER.info("Activating spell action {}!", id.toString());
        String[] methodIDSplit = methodID.split(":");
        if (methodIDSplit.length == 2) {
            String className = methodIDSplit[0];
            String methodName = methodIDSplit[1];

            ArrayList<Class<?>> paramClasses = new ArrayList<>();
            for (Object object : params) {
                paramClasses.add(object.getClass());
            }

            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid class name: " + className + ", could not find class.");
            }
            Method method;
            try {
                method = clazz.getMethod(methodName, paramClasses.toArray(Class[]::new));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Invalid method name: " + methodName + ", could not find method in class " + className + " with parameters " + Arrays.toString(params));
            }
            try {
                ArrayList<String> paramNames = new ArrayList<>();
                for (Object object : params) {
                    paramNames.add(object.getClass().getSimpleName());
                }
                if (Objects.equals(paramNames, requiredParams)) {
                    method.invoke(this, params);
                } else {
                    throw new IllegalArgumentException("Unable to activate spell action: Provided parameters " + Arrays.toString(params) + " and required parameters " + requiredParams.toString() + " do not match!");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                SpellCasterAPI.LOGGER.error("An error occurred when activating spell action: {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Method ID must be in this format: <fully specified class name with package name>:<method name>");
        }
    }

    @Override
    public String asString() {
        return "SpellAction: {\"methodName\":" + this.methodID + ",\"id\":" + this.id.toString() + "}";
    }
}
