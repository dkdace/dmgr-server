package com.dace.dmgr;

import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
            return (Entry<T>) entries.computeIfAbsent(key, k -> new Entry<>(k, defaultValue));
        }

        /**
         * 지정한 키에 해당하는 목록 항목을 반환한다.
         *
         * @param key 키
         * @param <T> 항목의 값 타입
         * @return 목록 항목 인스턴스
         */
        @NonNull
        @SuppressWarnings("unchecked")
        public <T> ListEntry<T> getListEntry(@NonNull String key) {
            return (ListEntry<T>) entries.computeIfAbsent(key, ListEntry::new);
        }

        /**
         * 섹션의 항목 (key-value 쌍)을 나타내는 클래스.
         *
         * @param <T> 항목의 값 타입
         * @see Section#getEntry(String, Object)
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public class Entry<T> {
            /** 키 */
            @NonNull
            private final String key;
            /** 기본값 */
            @NonNull
            private final T defaultValue;

            /** 값 */
            @Nullable
            private T value;

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
            @SuppressWarnings("unchecked")
            public T get() {
                validate();

                if (value == null) {
                    Object getValue = getConfigurationSection().get(key, defaultValue);

                    if (getValue.getClass().isInstance(defaultValue))
                        value = (T) getValue;
                    else if (defaultValue instanceof Byte)
                        value = (T) Byte.valueOf(((Number) getValue).byteValue());
                    else if (defaultValue instanceof Short)
                        value = (T) Short.valueOf(((Number) getValue).shortValue());
                    else if (defaultValue instanceof Integer)
                        value = (T) Integer.valueOf(((Number) getValue).intValue());
                    else if (defaultValue instanceof Long)
                        value = (T) Long.valueOf(((Number) getValue).longValue());
                    else if (defaultValue instanceof Float)
                        value = (T) Float.valueOf(((Number) getValue).floatValue());
                    else if (defaultValue instanceof Double)
                        value = (T) Double.valueOf(((Number) getValue).doubleValue());
                    else
                        value = defaultValue;
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
                getConfigurationSection().set(key, value == defaultValue ? null : value);
            }
        }

        /**
         * 섹션의 목록 항목 (여러 key-value 쌍)을 나타내는 클래스.
         *
         * @param <T> 항목의 값 타입
         * @see Section#getListEntry(String)
         */
        public final class ListEntry<T> extends Entry<List<T>> {
            private ListEntry(@NonNull String key) {
                super(key, Collections.emptyList());
            }

            /**
             * 값 목록을 반환한다.
             *
             * <pre><code>
             * // 키 "users"의 값 목록 반환
             * ListEntry&lt;Integer&gt; usersEntry = section.getListEntry("users");
             * List&lt;Integer&gt; users = usersEntry.get();
             *
             * // 키 "tests"의 값 목록 반환
             * ListEntry&lt;String&gt; testsEntry = section.getListEntry("tests");
             * List&lt;String&gt; tests = testsEntry.get();
             * </code></pre>
             *
             * @return 값 목록
             */
            @Override
            @NonNull
            @UnmodifiableView
            @SuppressWarnings("unchecked")
            public List<@NonNull T> get() {
                validate();

                if (super.value == null)
                    super.value = (List<T>) getConfigurationSection().getList(super.key, new ArrayList<>());

                return Collections.unmodifiableList(super.value);
            }

            /**
             * 목록에 값을 추가한다.
             *
             * <pre><code>
             * // 키 "users"의 값 목록에 "player" 추가
             * ListEntry&lt;String&gt; usersEntry = section.getListEntry("users");
             * usersEntry.add("player");
             * </code></pre>
             *
             * @param value 추가할 값
             */
            public void add(@NonNull T value) {
                validate();

                if (super.value == null)
                    super.value = new ArrayList<>();

                super.value.add(value);
                set(super.value);
            }

            /**
             * 목록에서 값을 제거한다.
             *
             * <pre><code>
             * // 키 "users"의 값 목록에서 "player" 제거
             * ListEntry&lt;String&gt; usersEntry = section.getListEntry("users");
             * usersEntry.remove("player");
             * </code></pre>
             *
             * @param value 제거할 값
             */
            public void remove(@NonNull T value) {
                validate();

                if (super.value == null)
                    return;

                super.value.remove(value);
                set(super.value);
            }
        }
    }
}
