package com.dace.dmgr;

import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Yaml 파일을 관리하는 클래스.
 *
 * <p>하나의 Yaml 파일에 여러 key-value 쌍을 저장할 수 있다.</p>
 *
 * <p>해당 클래스를 상속받아 Yaml 파일 시스템을 구현할 수 있다.</p>
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * public class TestFile extends YamlFile {
 *     // ...
 * }
 *
 * // foo/testFile.yml 생성
 * TestFile testFile = new TestFile("foo/testFile");
 *
 * testFile.init().onFinish(() -> {
 *     // 성공 시 실행할 작업.
 * }).onError(Exception ex) -> {
 *     // 실패(예외 발생) 시 실행할 작업.
 * });
 * }</pre>
 */
public abstract class YamlFile implements Initializable<Void> {
    /** Yaml 설정 객체 */
    private final YamlConfiguration config;
    /** 읽기 전용 여부 */
    private final boolean isReadOnly;
    /** 파일 저장을 위한 객체 */
    private final File file;
    /** 초기화 여부 */
    @Getter
    private boolean isInitialized = false;

    /**
     * Yaml 파일 관리 인스턴스를 생성한다.
     *
     * @param path       파일 경로
     * @param isReadOnly 읽기 전용 여부
     */
    protected YamlFile(@NonNull String path, boolean isReadOnly) {
        this.file = new File(DMGR.getPlugin().getDataFolder(), path + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.isReadOnly = isReadOnly;
    }

    /**
     * Yaml 파일 관리 인스턴스를 생성한다.
     *
     * @param path 파일 경로
     */
    protected YamlFile(@NonNull String path) {
        this(path, false);
    }

    /**
     * Yaml 파일을 불러온다.
     *
     * <p>파일이 존재하지 않으면 새 파일을 생성한다.</p>
     */
    @Override
    @NonNull
    public final AsyncTask<Void> init() {
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

                onFinish.andThen(v -> onInitFinish()).accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("파일 불러오기 실패 : {0}", ex, file);

                onInitError(ex);
                onError.andThen(this::onInitError).accept(ex);
            }
        });
    }

    /**
     * 초기화 성공 시 호출할 작업.
     */
    protected abstract void onInitFinish();

    /**
     * 초기화 실패(예외 발생) 시 호출할 작업.
     *
     * @param ex 발생한 예외
     */
    protected abstract void onInitError(@NonNull Exception ex);

    /**
     * 파일을 다시 불러온다.
     */
    @NonNull
    public final AsyncTask<Void> reload() {
        validate();

        return new AsyncTask<>((onFinish, onError) -> {
            try {
                config.load(file);
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("파일 불러오기 실패 : {0}", ex, file);
                onError.accept(ex);
            }
        });
    }

    /**
     * 파일을 저장한다.
     *
     * <p>읽기 전용으로 지정된 경우 사용할 수 없다.</p>
     *
     * @throws IllegalStateException 읽기 전용으로 호출 시 발생
     */
    @NonNull
    public final AsyncTask<Void> save() {
        validate();
        validateReadOnly();

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
     * 파일을 저장한다. (동기 실행).
     *
     * <p>읽기 전용으로 지정된 경우 사용할 수 없다.</p>
     *
     * @throws IllegalStateException 읽기 전용으로 호출 시 발생
     */
    public final void saveSync() {
        validate();
        validateReadOnly();

        try {
            config.save(file);
        } catch (Exception ex) {
            ConsoleLogger.severe("파일 저장 실패 : {0}", ex, file);
        }
    }

    /**
     * 섹션에서 실제 키 값을 반환한다.
     *
     * @param key 섹션이 포함된 키
     * @return 실제 키
     */
    private String getLastKey(@NonNull String key) {
        String[] sections = key.split("\\.");
        return sections[sections.length - 1];
    }

    /**
     * 섹션이 포함된 키에서 가장 깊은 섹션을 반환한다.
     *
     * @param key 섹션이 포함된 키
     * @return 가장 깊은 섹션
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    @NonNull
    private ConfigurationSection getDeepestSection(@NonNull String key) {
        if (key.startsWith(".") || key.endsWith(".") || key.contains(".."))
            throw new IllegalArgumentException("'key'가 유효하지 않음");

        ConfigurationSection configurationSection = config;
        String[] sections = key.split("\\.");
        for (int i = 0; i < sections.length - 1; i++) {
            if (configurationSection.getConfigurationSection(sections[i]) == null)
                configurationSection = configurationSection.createSection(sections[i]);
            else
                configurationSection = configurationSection.getConfigurationSection(sections[i]);
        }

        return configurationSection;
    }

    /**
     * 파일에 값을 저장한다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>읽기 전용으로 지정된 경우 사용할 수 없다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'에 값 1234 저장
     * yamlFile.set("test", 1234);
     * // 섹션 'user'의 키 'test'에 값 true 저장
     * yamlFile.set("user.test", true);
     * }</pre>
     *
     * @param key   키
     * @param value 값
     * @throws IllegalStateException    읽기 전용으로 호출 시 발생
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    protected final void set(@NonNull String key, @Nullable Object value) {
        validate();
        validateReadOnly();

        if (key.contains("."))
            getDeepestSection(key).set(getLastKey(key), value);
        else
            config.set(key, value);
    }

    /**
     * 파일에서 정수 값을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * long value = yamlFile.getLong("test", 0);
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * long value = yamlFile.getLong("user.test", 0);
     * }</pre>
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 값. 데이터가 존재하지 않으면 {@code defaultValue} 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    protected final long getLong(@NonNull String key, long defaultValue) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getLong(getLastKey(key), defaultValue);

        return config.getLong(key, defaultValue);
    }

    /**
     * 파일에서 정수 값 목록을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * List<Long> values = yamlFile.getLong("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * List<Long> values = yamlFile.getLong("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값 목록
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    @NonNull
    protected final List<@NonNull Long> getLongList(@NonNull String key) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getLongList(getLastKey(key));

        return config.getLongList(key);
    }

    /**
     * 파일에서 정수 값을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * long value = yamlFile.getLong("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * long value = yamlFile.getLong("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 0 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    protected final long getLong(@NonNull String key) {
        validate();
        return getLong(key, 0);
    }

    /**
     * 파일에서 실수 값을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * double value = yamlFile.getDouble("test", 2.5);
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * double value = yamlFile.getDouble("user.test", 2.5);
     * }</pre>
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 {@code defaultValue} 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    protected final double getDouble(@NonNull String key, double defaultValue) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getDouble(getLastKey(key), defaultValue);

        return config.getDouble(key, defaultValue);
    }

    /**
     * 파일에서 실수 값 목록을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * List<Double> values = yamlFile.getDouble("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * List<Double> values = yamlFile.getDouble("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값 목록
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    @NonNull
    protected final List<@NonNull Double> getDoubleList(@NonNull String key) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getDoubleList(getLastKey(key));

        return config.getDoubleList(key);
    }

    /**
     * 파일에서 실수 값을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * double value = yamlFile.getDouble("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * double value = yamlFile.getDouble("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 0 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    protected final double getDouble(@NonNull String key) {
        validate();
        return getDouble(key, 0);
    }

    /**
     * 파일에서 문자열 값을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * String value = yamlFile.getString("test", "Default");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * String value = yamlFile.getString("user.test", "Default");
     * }</pre>
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 값. 데이터가 존재하지 않으면 {@code defaultValue} 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    @NonNull
    protected final String getString(@NonNull String key, @NonNull String defaultValue) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getString(getLastKey(key), defaultValue);

        return config.getString(key, defaultValue);
    }

    /**
     * 파일에서 문자열 값 목록을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * List<String> values = yamlFile.getStringList("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * List<String> values = yamlFile.getStringList("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값 목록
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    @NonNull
    protected final List<@NonNull String> getStringList(@NonNull String key) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getStringList(getLastKey(key));

        return config.getStringList(key);
    }

    /**
     * 파일에서 문자열 값을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * String value = yamlFile.getString("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * String value = yamlFile.getString("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 빈 문자열 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    @NonNull
    protected final String getString(@NonNull String key) {
        validate();
        return getString(key, "");
    }

    /**
     * 파일에서 부울 값을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * boolean value = yamlFile.getBoolean("test", false);
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * boolean value = yamlFile.getBoolean("user.test", true);
     * }</pre>
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 값. 데이터가 존재하지 않으면 {@code defaultValue} 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    protected final boolean getBoolean(@NonNull String key, boolean defaultValue) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getBoolean(getLastKey(key), defaultValue);

        return config.getBoolean(key, defaultValue);
    }

    /**
     * 파일에서 부울 값 목록을 불러온다.
     *
     * <p>키 값에 섹션을 포함할 수 있으며, '.'으로 구분한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * List<Boolean> values = yamlFile.getBooleanList("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * List<Boolean> values = yamlFile.getBooleanList("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값 목록
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    @NonNull
    protected final List<@NonNull Boolean> getBooleanList(@NonNull String key) {
        validate();

        if (key.contains("."))
            return getDeepestSection(key).getBooleanList(getLastKey(key));

        return config.getBooleanList(key);
    }

    /**
     * 파일에서 부울 값을 불러온다.
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 키 'test'의 값 불러오기
     * boolean value = yamlFile.getBoolean("test");
     * // 섹션 'user'의 키 'test'의 값 불러오기
     * boolean value = yamlFile.getBoolean("user.test");
     * }</pre>
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 {@code false} 반환
     * @throws IllegalArgumentException {@code key}가 유효하지 않으면 발생
     */
    protected final boolean getBoolean(@NonNull String key) {
        validate();
        return getBoolean(key, false);
    }

    /**
     * 인스턴스가 읽기 전용으로 생성되었으면 예외를 발생시킨다.
     */
    private void validateReadOnly() {
        if (isReadOnly)
            throw new IllegalStateException("인스턴스가 읽기 전용으로 생성됨");
    }
}
