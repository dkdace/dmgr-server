package com.dace.dmgr.util;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Java Reflection API 관련 기능을 제공하는 클래스.
 *
 * <p>모든 메소드는 첫 호출 이후로 캐싱된 값을 반환한다.</p>
 */
@UtilityClass
public final class ReflectionUtil {
    /** 클래스 이름별 클래스 목록 (이름 : 클래스) */
    private static final HashMap<String, Class<?>> CLASS_MAP = new HashMap<>();

    /**
     * 지정한 이름에 해당하는 클래스를 반환한다.
     *
     * <p>{@link Class#forName(String)}과 동일한 기능이다.</p>
     *
     * @param name 클래스 이름
     * @return 클래스 ({@link Class})
     * @throws ClassNotFoundException 클래스를 찾을 수 없으면 발생
     */
    @NonNull
    public static Class<?> getClass(@NonNull String name) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(name);
        return CLASS_MAP.computeIfAbsent(name, k -> clazz);
    }

    /**
     * 지정한 매개변수 목록에 해당하는 생성자를 클래스에서 찾아 반환한다.
     *
     * @param clazz          클래스
     * @param parameterTypes 매개변수 목록
     * @param <T>            {@code clazz}의 타입
     * @return 생성자 ({@link Constructor})
     * @throws NoSuchMethodException 생성자를 찾을 수 없으면 발생
     */
    @NonNull
    public static <T> Constructor<T> getConstructor(@NonNull Class<T> clazz, @NonNull Class<?> @NonNull ... parameterTypes) throws NoSuchMethodException {
        return ReflectionClass.fromClass(clazz).getConstructor(parameterTypes);
    }

    /**
     * 지정한 이름과 매개변수 목록에 해당하는 메소드를 클래스에서 찾아 반환한다.
     *
     * @param clazz          클래스
     * @param name           메소드 이름
     * @param parameterTypes 매개변수 목록
     * @return 메소드 ({@link Method})
     * @throws NoSuchMethodException 메소드를 찾을 수 없으면 발생
     */
    @NonNull
    public static Method getMethod(@NonNull Class<?> clazz, @NonNull String name, @NonNull Class<?> @NonNull ... parameterTypes) throws NoSuchMethodException {
        return ReflectionClass.fromClass(clazz).getMethod(name, parameterTypes);
    }

    /**
     * 지정한 이름에 해당하는 필드를 클래스에서 찾아 반환한다.
     *
     * @param clazz 클래스
     * @param name  필드 이름
     * @return 필드 ({@link Field})
     * @throws NoSuchFieldException 필드를 찾을 수 없으면 발생
     */
    @NonNull
    public static Field getField(@NonNull Class<?> clazz, @NonNull String name) throws NoSuchFieldException {
        return ReflectionClass.fromClass(clazz).getField(name);
    }

    /**
     * 리플렉션 값의 캐싱을 위해 사용하는 클래스.
     *
     * @param <T> 클래스의 타입
     */
    @AllArgsConstructor
    private static final class ReflectionClass<T> {
        /** 클래스별 ReflectionClass 목록 (클래스 : ReflectionClass) */
        private static final HashMap<Class<?>, ReflectionClass<?>> REFLECTION_CLASS_MAP = new HashMap<>();

        /** 매개변수 목록별 생성자 목록 (매개변수 목록 문자열 : 생성자) */
        private final HashMap<String, Constructor<T>> constructorMap = new HashMap<>();
        /** 메소드 이름 및 매개변수 목록별 메소드 목록 (이름+매개변수 목록 문자열 : 메소드) */
        private final HashMap<String, Method> methodMap = new HashMap<>();
        /** 필드 이름별 필드 목록 (이름 : 필드) */
        private final HashMap<String, Field> fieldMap = new HashMap<>();

        /** 클래스 */
        private final Class<T> clazz;

        @NonNull
        @SuppressWarnings("unchecked")
        private static <U> ReflectionClass<U> fromClass(@NonNull Class<U> clazz) {
            return (ReflectionClass<U>) REFLECTION_CLASS_MAP.computeIfAbsent(clazz, k -> new ReflectionClass<>(clazz));
        }

        @NonNull
        private Constructor<T> getConstructor(@NonNull Class<?> @NonNull ... parameterTypes) throws NoSuchMethodException {
            String keyString = Arrays.toString(parameterTypes);
            Constructor<T> constructor = constructorMap.getOrDefault(keyString, null);
            if (constructor == null) {
                try {
                    constructor = clazz.getConstructor(parameterTypes);
                } catch (NoSuchMethodException ex) {
                    constructor = clazz.getDeclaredConstructor(parameterTypes);
                }
                constructor.setAccessible(true);

                constructorMap.put(keyString, constructor);
            }

            return constructor;
        }

        @NonNull
        private Method getMethod(@NonNull String name, @NonNull Class<?> @NonNull ... parameterTypes) throws NoSuchMethodException {
            String keyString = name + Arrays.toString(parameterTypes);
            Method method = methodMap.getOrDefault(keyString, null);
            if (method == null) {
                try {
                    method = clazz.getMethod(name, parameterTypes);
                } catch (NoSuchMethodException ex) {
                    method = clazz.getDeclaredMethod(name, parameterTypes);
                }
                method.setAccessible(true);

                methodMap.put(keyString, method);
            }

            return method;
        }

        @NonNull
        private Field getField(@NonNull String name) throws NoSuchFieldException {
            Field field = fieldMap.get(name);
            if (field == null) {
                try {
                    field = clazz.getField(name);
                } catch (NoSuchFieldException ex) {
                    field = clazz.getDeclaredField(name);
                }
                field.setAccessible(true);

                fieldMap.put(name, field);
            }

            return field;
        }
    }
}
