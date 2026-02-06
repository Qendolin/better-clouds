package com.qendolin.betterclouds.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectAccess {
    protected final String name;

    public ReflectAccess(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    protected <T> T invoke(Object instance, Method m, Object... args) {
        try {
            return (T) m.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw IncompatibleModDependencyException.of(name, e);
        }
    }

    public static class IncompatibleModDependencyException extends RuntimeException {

        public IncompatibleModDependencyException(String message, Throwable cause) {
            super(message, cause);
        }

        public IncompatibleModDependencyException(String message) {
            super(message);
        }

        public static IncompatibleModDependencyException of(String dependencyName, Throwable cause) {
            return new IncompatibleModDependencyException("Your versions of Better Clouds and " + dependencyName + " are not compatible!", cause);
        }

        public static IncompatibleModDependencyException of(String dependencyName, String message) {
            return new IncompatibleModDependencyException("Your versions of Better Clouds and " + dependencyName + " are not compatible! " + message);
        }
    }
}
