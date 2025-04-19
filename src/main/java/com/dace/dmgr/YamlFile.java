package com.dace.dmgr;

import com.dace.dmgr.combat.Core;
import com.dace.dmgr.item.gui.ChatSoundOption;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.*;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Yaml 파일을 관리하는 클래스.
 *
 * <p>하나의 Yaml 파일에 여러 항목 (key-value 쌍)을 저장할 수 있다.</p>
 *
 * <p>Example:</p>
 *
 * <pre><code>
 * // foo/test_file.yml 생성
 * YamlFile testFile = new YamlFile(Paths.get("foo", "test_file.yml"));
 *
 * testFile.init()
 *     .onFinish(() -&gt; {
 *         // 성공 시 실행할 작업.
 *     })
 *     .onError(Exception ex) -&gt; {
 *         // 실패(예외 발생) 시 실행할 작업.
 *     });
 * </code></pre>
 */
public final class YamlFile implements Initializable<Void> {
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
        SERIALIZER_MAP.put(Material.class, new EnumSerializer<>(Material.class));
        SERIALIZER_MAP.put(Core.class, new EnumSerializer<>(Core.class));
        SERIALIZER_MAP.put(Timespan.class, Timespan.Serializer.getInstance());
        SERIALIZER_MAP.put(GlobalLocation.class, GlobalLocation.Serializer.getInstance());
        SERIALIZER_MAP.put(UserData.class, UserData.Serializer.getInstance());
        SERIALIZER_MAP.put(ChatSoundOption.ChatSound.class, new YamlFile.EnumSerializer<>(ChatSoundOption.ChatSound.class));
    }

    /** Yaml 설정 인스턴스 */
    private final YamlConfiguration config;
    /** 파일 저장 인스턴스 */
    private final File file;
    /** 기본 섹션 인스턴스 */
    @NonNull
    @Getter
    private final Section defaultSection;

    /** 초기화 여부 */
    @Getter
    private boolean isInitialized = false;

    /**
     * Yaml 파일 관리 인스턴스를 생성한다.
     *
     * @param path 파일 상대 경로 (플러그인 폴더로부터의 경로)
     */
    public YamlFile(@NonNull Path path) {
        this.file = DMGR.getPlugin().getDataFolder().toPath().resolve(path).toFile();
        this.config = YamlConfiguration.loadConfiguration(file);
        this.defaultSection = new Section();
    }

    /**
     * 지정한 타입에 대한 기본 직렬화 처리기를 반환한다.
     *
     * @param type 타입
     * @param <T>  역직렬화된 데이터 타입
     * @param <R>  Yaml 파일에 저장할 직렬화된 데이터 타입
     * @return 직렬화 처리기
     * @throws NullPointerException 해당하는 Serializer가 존재하지 않으면 발생
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Serializer<T, R> getDefaultSerializer(@NonNull Class<T> type) {
        return (Serializer<T, R>) Validate.notNull(SERIALIZER_MAP.get(type), "%s에 대한 Serializer가 존재하지 않음", type.getName());
    }

    /**
     * Yaml 파일을 불러온다.
     *
     * <p>파일이 존재하지 않으면 새 파일을 생성한다.</p>
     */
    @Override
    @NonNull
    public AsyncTask<Void> init() {
        if (isInitialized)
            throw new IllegalStateException("인스턴스가 이미 초기화됨");

        return new AsyncTask<>((onFinish, onError) -> {
            try {
                if (!file.exists()) {
                    ConsoleLogger.warning("파일을 찾을 수 없음. 파일 생성 중 : {0}", file);
                    config.save(file);
                }

                config.load(file);
                isInitialized = true;

                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("파일 불러오기 실패 : {0}", ex, file);
                onError.accept(ex);
            }
        });
    }

    /**
     * Yaml 파일을 저장한다.
     */
    @NonNull
    public AsyncTask<Void> save() {
        validate();

        return new AsyncTask<>((onFinish, onError) -> {
            try {
                config.save(file);
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("파일 저장 실패 : {0}", ex, file);
                onError.accept(ex);
            }
        });
    }

    /**
     * Yaml 파일을 저장한다. (동기 실행).
     */
    public void saveSync() {
        validate();

        try {
            config.save(file);
        } catch (Exception ex) {
            ConsoleLogger.severe("파일 저장 실패 : {0}", ex, file);
        }
    }

    /**
     * 섹션 항목의 직렬화 및 역직렬화를 관리하는 인터페이스.
     *
     * @param <T> 역직렬화된 데이터 타입
     * @param <R> Yaml 파일에 저장할 직렬화된 데이터 타입
     */
    public interface Serializer<T, R> {
        /**
         * 지정한 값을 직렬화한다.
         *
         * @param value 값
         * @return 직렬화된 값
         */
        @NonNull
        R serialize(@NonNull T value);

        /**
         * 지정한 값을 역직렬화한다.
         *
         * @param value 값
         * @return 역직렬화된 값
         */
        @NonNull
        T deserialize(@NonNull R value);
    }

    @NoArgsConstructor
    @Getter
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

    @AllArgsConstructor
    private static class EnumSerializer<E extends Enum<E>> implements Serializer<E, String> {
        @NonNull
        private final Class<E> enumClass;

        @Override
        @NonNull
        public String serialize(@NonNull E value) {
            return value.name();
        }

        @Override
        @NonNull
        public E deserialize(@NonNull String value) {
            return Enum.valueOf(enumClass, value);
        }
    }

    @AllArgsConstructor
    public static class ListSerializer<T, R> implements Serializer<List<T>, List<R>> {
        private final Serializer<T, R> serializer;

        public ListSerializer(@NonNull Class<T> type) {
            this.serializer = getDefaultSerializer(type);
        }

        @Override
        @NonNull
        public List<R> serialize(@NonNull List<T> value) {
            return value.stream().map(serializer::serialize).collect(Collectors.toList());
        }

        @Override
        @NonNull
        public List<T> deserialize(@NonNull List<R> value) {
            return value.stream().map(serializer::deserialize).collect(Collectors.toList());
        }
    }

    /**
     * Yaml 파일의 섹션을 나타내는 클래스.
     */
    public final class Section {
        /** 섹션 경로 */
        private final String path;
        /** 항목 목록 (키 : 항목 인스턴스) */
        private final HashMap<String, Entry<?>> entries = new HashMap<>();
        /** 하위 섹션 목록 (이름 : 섹션 인스턴스) */
        private final HashMap<String, Section> subSections = new HashMap<>();

        private Section() {
            this.path = "";
        }

        private Section(@NonNull Section section, @NonNull String name) {
            this.path = section.path + "." + name;
        }

        /**
         * 지정한 이름의 섹션을 반환한다.
         *
         * @param name 섹션 이름
         * @return 섹션 인스턴스
         */
        @NonNull
        public Section getSection(@NonNull String name) {
            return subSections.computeIfAbsent(name, k -> new Section(this, k));
        }

        /**
         * 지정한 키에 해당하는 항목을 반환한다.
         *
         * @param key          키
         * @param defaultValue 기본값. 항목이 존재하지 않으면 이 값으로 설정됨
         * @param <T>          항목의 값 타입
         * @return 항목 인스턴스
         */
        @NonNull
        @SuppressWarnings("unchecked")
        public <T> Entry<T> getEntry(@NonNull String key, @NonNull T defaultValue) {
            return (Entry<T>) entries.computeIfAbsent(key, k -> new Entry<>(k, defaultValue, getDefaultSerializer((Class<T>) defaultValue.getClass())));
        }

        /**
         * 지정한 키에 해당하는 목록 항목을 반환한다.
         *
         * @param key        키
         * @param serializer 직렬화 처리기
         * @param <T>        항목의 값 타입
         * @return 항목 인스턴스
         */
        @NonNull
        @SuppressWarnings("unchecked")
        public <T, R> Entry<List<T>> getListEntry(@NonNull String key, @NonNull Serializer<List<T>, R> serializer) {
            return (Entry<List<T>>) entries.computeIfAbsent(key, k -> new Entry<>(k, new ArrayList<>(), (Serializer<List<T>, Object>) serializer));
        }

        /**
         * 섹션의 항목 (key-value 쌍)을 나타내는 클래스.
         *
         * @param <T> 항목의 값 타입
         * @see Section#getEntry(String, Object)
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public final class Entry<T> {
            /** 키 */
            @NonNull
            private final String key;
            /** 기본값 */
            @NonNull
            private final T defaultValue;
            /** 직렬화 처리기 */
            @NonNull
            private final Serializer<T, Object> serializer;

            /** 값 */
            @Nullable
            private T value;

            /**
             * 현재 섹션의 {@link ConfigurationSection} 인스턴스를 반환한다.
             *
             * @return {@link ConfigurationSection}
             */
            @NonNull
            private ConfigurationSection getConfigurationSection() {
                if (path.isEmpty())
                    return config;

                return config.getConfigurationSection(path) == null ? config.createSection(path) : config.getConfigurationSection(path);
            }

            /**
             * 값을 반환한다.
             *
             * <pre><code>
             * // 키 "user"의 값 반환
             * Entry&lt;Integer&gt; userEntry = section.getEntry("user", 1234);
             * int user = userEntry.get();
             *
             * // 키 "test"의 값 반환
             * Entry&lt;Boolean&gt; testEntry = section.getEntry("test", false);
             * boolean test = testEntry.get();
             * </code></pre>
             *
             * @return 값. 데이터가 존재하지 않으면 기본값 반환
             */
            @NonNull
            public T get() {
                validate();

                if (value == null) {
                    Object getValue = getConfigurationSection().get(key);
                    if (getValue instanceof ConfigurationSection)
                        getValue = ((ConfigurationSection) getValue).getValues(true);

                    value = getValue == null ? defaultValue : serializer.deserialize(getValue);
                }

                return value;
            }

            /**
             * 값을 설정한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // 키 "user"의 값을 500으로 설정
             * Entry&lt;Integer&gt; userEntry = section.getEntry("user", 1234);
             * userEntry.set("user", 500);
             *
             * // 키 "test"의 값을 true로 설정
             * Entry&lt;Boolean&gt; testEntry = section.getEntry("test", false);
             * testEntry.set("test", true);
             * </code></pre>
             *
             * @param value 값. {@code null}로 지정 시 삭제
             */
            public void set(@Nullable T value) {
                validate();

                this.value = value;
                getConfigurationSection().set(key, value == null || value == defaultValue ? null : serializer.serialize(value));
            }
        }
    }
}
