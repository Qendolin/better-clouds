package com.qendolin.betterclouds.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Configs {
    private static final MethodHandles.Lookup METHOD_LOOKUP = MethodHandles.lookup();
    private static final ConcurrentHashMap<Class<?>, List<SerialField>> FIELD_CACHE = new ConcurrentHashMap<>();

    private Configs() {
    }

    public static void copy(Object target, Object source) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source");
        if (!target.getClass().equals(source.getClass())) {
            throw new IllegalArgumentException("Mismatched types: " + target.getClass().getName() + " != " + source.getClass().getName());
        }
        for (SerialField field : serialFields(target.getClass())) {
            Object value = field.get(source);
            field.set(target, copyValue(field.genericType(), value));
        }
    }

    public static boolean equal(Object left, Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null || !left.getClass().equals(right.getClass())) {
            return false;
        }
        for (SerialField field : serialFields(left.getClass())) {
            if (!valuesEqual(field.type(), field.genericType(), field.get(left), field.get(right))) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(Object object) {
        if (object == null) {
            return 0;
        }
        int result = 1;
        for (SerialField field : serialFields(object.getClass())) {
            result = 31 * result + valueHashCode(field.type(), field.genericType(), field.get(object));
        }
        return result;
    }

    private static Object copyValue(Type declaredType, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list) {
            Type elementType = declaredType instanceof ParameterizedType parameterizedType
                ? parameterizedType.getActualTypeArguments()[0]
                : null;
            List<Object> copy = new ArrayList<>(list.size());
            for (Object entry : list) {
                copy.add(copyValue(elementType, entry));
            }
            return copy;
        }
        Class<?> valueClass = value.getClass();
        if (serialFields(valueClass).isEmpty()) {
            return value;
        }
        Object copy = instantiate(valueClass);
        copy(copy, value);
        return copy;
    }

    private static boolean valuesEqual(Class<?> declaredClass, Type declaredType, Object left, Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (declaredClass == float.class || declaredClass == Float.class) {
            return Float.compare(((Number) left).floatValue(), ((Number) right).floatValue()) == 0;
        }
        if (declaredClass == double.class || declaredClass == Double.class) {
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue()) == 0;
        }
        if (List.class.isAssignableFrom(declaredClass) && left instanceof List<?> leftList && right instanceof List<?> rightList) {
            return listEquals(declaredType, leftList, rightList);
        }
        if (!serialFields(left.getClass()).isEmpty()) {
            return equal(left, right);
        }
        return Objects.equals(left, right);
    }

    private static boolean listEquals(Type declaredType, List<?> left, List<?> right) {
        if (left.size() != right.size()) {
            return false;
        }
        Type elementType = declaredType instanceof ParameterizedType parameterizedType
            ? parameterizedType.getActualTypeArguments()[0]
            : null;
        for (int i = 0; i < left.size(); i++) {
            Class<?> declaredClass = elementType instanceof Class<?> clazz ? clazz : Object.class;
            if (!valuesEqual(declaredClass, elementType, left.get(i), right.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static int valueHashCode(Class<?> declaredClass, Type declaredType, Object value) {
        if (value == null) {
            return 0;
        }
        if (declaredClass == float.class || declaredClass == Float.class) {
            return Float.hashCode(((Number) value).floatValue());
        }
        if (declaredClass == double.class || declaredClass == Double.class) {
            return Double.hashCode(((Number) value).doubleValue());
        }
        if (List.class.isAssignableFrom(declaredClass) && value instanceof List<?> list) {
            int result = 1;
            Type elementType = declaredType instanceof ParameterizedType parameterizedType
                ? parameterizedType.getActualTypeArguments()[0]
                : null;
            Class<?> elementClass = elementType instanceof Class<?> clazz ? clazz : Object.class;
            for (Object entry : list) {
                result = 31 * result + valueHashCode(elementClass, elementType, entry);
            }
            return result;
        }
        if (!serialFields(value.getClass()).isEmpty()) {
            return hashCode(value);
        }
        return value.hashCode();
    }

    private static List<SerialField> serialFields(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, Configs::createSerialFields);
    }

    private static List<SerialField> createSerialFields(Class<?> type) {
        return Arrays.stream(type.getFields())
            .filter(field -> field.isAnnotationPresent(SerialEntry.class))
            .sorted(Comparator.comparing(Field::getName))
            .map(Configs::createSerialField)
            .toList();
    }

    private static SerialField createSerialField(Field field) {
        try {
            field.setAccessible(true);
            return new SerialField(
                field.getName(),
                field.getType(),
                field.getGenericType(),
                METHOD_LOOKUP.unreflectGetter(field),
                METHOD_LOOKUP.unreflectSetter(field)
            );
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access @SerialEntry field " + field, e);
        }
    }

    private static Object instantiate(Class<?> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate " + type.getName(), e);
        }
    }

    private record SerialField(String name, Class<?> type, Type genericType, MethodHandle getter, MethodHandle setter) {
        private Object get(Object instance) {
            try {
                return getter.invoke(instance);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to read @SerialEntry field " + name, throwable);
            }
        }

        private void set(Object instance, Object value) {
            try {
                setter.invoke(instance, value);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to write @SerialEntry field " + name, throwable);
            }
        }
    }
}
