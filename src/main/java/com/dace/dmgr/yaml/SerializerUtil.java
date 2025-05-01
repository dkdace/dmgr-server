package com.dace.dmgr.yaml;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.location.BlockRegion;
import com.dace.dmgr.util.location.CuboidRegion;
import com.dace.dmgr.util.location.GlobalLocation;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 직렬화 처리기({@link Serializer}) 관련 기능을 제공하는 클래스.
 */
@UtilityClass
public final class SerializerUtil {
    /** 클래스별 직렬화 처리기 목록 (클래스 : 직렬화 처리기) */
    private static final HashMap<Class<?>, Serializer<?, ?>> SERIALIZER_MAP = new HashMap<>();

    static {
        SERIALIZER_MAP.put(Boolean.class, new DefaultSerializer<>());
        SERIALIZER_MAP.put(Byte.class, new NumberSerializer<>(Number::byteValue));
        SERIALIZER_MAP.put(Short.class, new NumberSerializer<>(Number::shortValue));
        SERIALIZER_MAP.put(Integer.class, new NumberSerializer<>(Number::intValue));
        SERIALIZER_MAP.put(Long.class, new NumberSerializer<>(Number::longValue));
        SERIALIZER_MAP.put(Float.class, new NumberSerializer<>(Number::floatValue));
        SERIALIZER_MAP.put(Double.class, new NumberSerializer<>(Number::doubleValue));
        SERIALIZER_MAP.put(String.class, new DefaultSerializer<>());
        SERIALIZER_MAP.put(Timespan.class, Timespan.TimespanSerializer.getInstance());
        SERIALIZER_MAP.put(GlobalLocation.class, GlobalLocation.GlobalLocationSerializer.getInstance());
        SERIALIZER_MAP.put(UserData.class, UserData.UserDataSerializer.getInstance());
        SERIALIZER_MAP.put(CuboidRegion.class, CuboidRegion.CuboidRegionSerializer.getInstance());
        SERIALIZER_MAP.put(BlockRegion.class, BlockRegion.BlockRegionSerializer.getInstance());
    }

    @NonNull
    static <T, R> Serializer<List<T>, List<R>> getListSerializer(@NonNull TypeToken<List<T>> typeToken) {
        return new Serializer<List<T>, List<R>>() {
            @SuppressWarnings("unchecked")
            private Serializer<T, R> getNestedSerializer() {
                TypeToken<T> nestedType = (TypeToken<T>) Validate.notNull(typeToken.getNestedTypeTokens())[0];
                return getDefaultSerializer(nestedType.getRawType(), nestedType);
            }

            @Override
            @NonNull
            public List<R> serialize(@NonNull List<T> value) {
                return value.stream().map(v -> getNestedSerializer().serialize(v)).collect(Collectors.toList());
            }

            @Override
            @NonNull
            public List<T> deserialize(@NonNull List<R> value) {
                return value.stream().map(v -> getNestedSerializer().deserialize(v)).collect(Collectors.toList());
            }
        };
    }

    @NonNull
    static <E extends Enum<E>> Serializer<E, String> getEnumSerializer(@NonNull TypeToken<E> typeToken) {
        return new Serializer<E, String>() {
            @Override
            @NonNull
            public String serialize(@NonNull E value) {
                return value.name();
            }

            @Override
            @NonNull
            public E deserialize(@NonNull String value) {
                return Enum.valueOf(typeToken.getRawType(), value);
            }
        };
    }

    /**
     * 지정한 타입에 대한 기본 직렬화 처리기를 반환한다.
     *
     * @param rawType   원시 타입 (클래스)
     * @param typeToken 타입
     * @param <T>       역직렬화된 데이터 타입
     * @param <R>       Yaml 파일에 저장할 직렬화된 데이터 타입
     * @return 직렬화 처리기
     * @throws NullPointerException 해당하는 Serializer가 존재하지 않으면 발생
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, R> Serializer<T, R> getDefaultSerializer(@NonNull Class<T> rawType, @NonNull TypeToken<T> typeToken) {
        if (List.class.isAssignableFrom(rawType))
            return getListSerializer((TypeToken) typeToken);
        else if (Enum.class.isAssignableFrom(rawType))
            return getEnumSerializer((TypeToken) typeToken);

        return (Serializer<T, R>) Validate.notNull(SERIALIZER_MAP.get(rawType), "%s에 대한 Serializer가 존재하지 않음", rawType.getName());
    }

    /**
     * 지정한 타입에 대한 기본 직렬화 처리기를 반환한다.
     *
     * @param rawType 원시 타입 (클래스)
     * @param <T>     역직렬화된 데이터 타입
     * @param <R>     Yaml 파일에 저장할 직렬화된 데이터 타입
     * @return 직렬화 처리기
     * @throws NullPointerException 해당하는 Serializer가 존재하지 않으면 발생
     */
    public static <T, R> Serializer<T, R> getDefaultSerializer(@NonNull Class<T> rawType) {
        return getDefaultSerializer(rawType, new TypeToken<T>(rawType) {
        });
    }

    @NoArgsConstructor
    private static final class DefaultSerializer<T> implements Serializer<T, T> {
        @Override
        @NonNull
        public T serialize(@NonNull T value) {
            return value;
        }

        @Override
        @NonNull
        public T deserialize(@NonNull T value) {
            return value;
        }
    }

    @AllArgsConstructor
    private static final class NumberSerializer<T extends Number> implements Serializer<T, Number> {
        private final Function<Number, T> onDeserialize;

        @Override
        @NonNull
        public Number serialize(@NonNull T value) {
            return value;
        }

        @Override
        @NonNull
        public T deserialize(@NonNull Number value) {
            return onDeserialize.apply(value);
        }
    }
}
