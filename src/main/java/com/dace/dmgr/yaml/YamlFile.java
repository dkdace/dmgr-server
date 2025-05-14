package com.dace.dmgr.yaml;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
     * Yaml 파일을 불러온다.
     *
     * <p>파일이 존재하지 않으면 새 파일을 생성한다.</p>
     */
    @Override
    @NonNull
    public AsyncTask<Void> init() {
        return AsyncTask.create(this::initSync);
    }

    /**
     * Yaml 파일을 불러온다. (동기 실행).
     *
     * <p>파일이 존재하지 않으면 새 파일을 생성한다.</p>
     */
    public void initSync() {
        if (isInitialized)
            throw new IllegalStateException("인스턴스가 이미 초기화됨");

        try {
            if (!file.exists()) {
                ConsoleLogger.warning("파일을 찾을 수 없음. 파일 생성 중 : {0}", file);
                config.save(file);
            }

            config.load(file);
            isInitialized = true;
        } catch (Exception ex) {
            ConsoleLogger.severe("파일 불러오기 실패 : {0}", ex, file);
            throw new IllegalStateException("파일을 불러올 수 없음");
        }
    }

    /**
     * Yaml 파일을 저장한다.
     */
    @NonNull
    public AsyncTask<Void> save() {
        return AsyncTask.create(this::saveSync);
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
            throw new IllegalStateException("파일을 저장할 수 없음");
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
         * @throws NullPointerException 값 타입에 해당하는 Serializer가 존재하지 않으면 발생
         */
        @NonNull
        @SuppressWarnings("unchecked")
        public <T> Entry<T> getEntry(@NonNull String key, @NonNull T defaultValue) {
            return (Entry<T>) entries.computeIfAbsent(key, k -> {
                Class<T> type = (Class<T>) defaultValue.getClass();

                return new Entry<>(k, defaultValue, SerializerUtil.getDefaultSerializer(type));
            });
        }

        /**
         * 지정한 키에 해당하는 목록 항목을 반환한다.
         *
         * <p>Example:</p>
         *
         * <pre><code>
         * Entry&lt;List&lt;Integer&gt;&gt; listEntry = section.getListEntry("test", new TypeToken&lt;List&lt;Integer&gt;&gt;() {});
         * </code></pre>
         *
         * @param key       키
         * @param typeToken 타입 토큰
         * @param <T>       항목의 값 타입
         * @return 항목 인스턴스
         */
        @NonNull
        @SuppressWarnings("unchecked")
        public <T> Entry<List<T>> getListEntry(@NonNull String key, @NonNull TypeToken<List<T>> typeToken) {
            return (Entry<List<T>>) entries.computeIfAbsent(key, k -> new Entry<>(k, new ArrayList<>(), SerializerUtil.getListSerializer(typeToken)));
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
            private final Serializer<T, ?> serializer;

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
             * <p>Example:</p>
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
            @SuppressWarnings("unchecked")
            public T get() {
                validate();

                if (value == null) {
                    Object getValue = getConfigurationSection().get(key);
                    if (getValue instanceof ConfigurationSection)
                        getValue = ((ConfigurationSection) getValue).getValues(true);

                    value = getValue == null ? defaultValue : ((Serializer<T, Object>) serializer).deserialize(getValue);
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
                getConfigurationSection().set(key, value == null ? null : serializer.serialize(value));
            }
        }
    }
}
