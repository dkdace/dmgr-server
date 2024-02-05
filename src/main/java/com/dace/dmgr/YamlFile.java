package com.dace.dmgr;

import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;

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
    @NonNull
    private final YamlConfiguration config;
    /** 읽기 전용 여부 */
    private final boolean isReadOnly;
    /** 파일 저장을 위한 객체 */
    @NonNull
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
        file = new File(DMGR.getPlugin().getDataFolder(), path + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
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
            throw new CannotAccessException();

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
                onInitError(ex);

            }
        }).onFinish(this::onInitFinish).onError(this::onInitError);
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
    protected abstract void onInitError(Exception ex);

    /**
     * 파일을 삭제한다.
     *
     * @throws IllegalStateException 읽기 전용으로 호출 시 발생
     */
    @NonNull
    public final AsyncTask<Void> delete() {
        checkAccess();

        if (isReadOnly)
            throw new IllegalStateException("인스턴스가 읽기 전용으로 생성됨");

        return new AsyncTask<>((onFinish, onError) -> {
            try {
                Files.delete(file.toPath());
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("파일 삭제 실패 : {0}", ex, file);
                onError.accept(ex);
            }
        });
    }

    /**
     * 파일을 다시 불러온다.
     */
    @NonNull
    public final AsyncTask<Void> reload() {
        checkAccess();

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
        checkAccess();

        if (isReadOnly)
            throw new IllegalStateException("인스턴스가 읽기 전용으로 생성됨");

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
     * 파일을 저장한다. (동기)
     *
     * <p>읽기 전용으로 지정된 경우 사용할 수 없다.</p>
     *
     * @throws IllegalStateException 읽기 전용으로 호출 시 발생
     */
    public final void saveSync() {
        checkAccess();

        if (isReadOnly)
            throw new IllegalStateException("인스턴스가 읽기 전용으로 생성됨");

        try {
            config.save(file);
        } catch (Exception ex) {
            ConsoleLogger.severe("파일 저장 실패 : {0}", ex, file);
        }
    }

    /**
     * 파일에 값을 저장한다.
     *
     * <p>읽기 전용으로 지정된 경우 사용할 수 없다.</p>
     *
     * @param key   키
     * @param value 값
     * @throws IllegalStateException 읽기 전용으로 호출 시 발생
     */
    protected final void set(@NonNull String key, Object value) {
        checkAccess();

        if (isReadOnly)
            throw new IllegalStateException("인스턴스가 읽기 전용으로 생성됨");

        config.set(key, value);
    }

    /**
     * 파일에서 정수 값을 불러온다.
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 값. 데이터가 존재하지 않으면 {@code defaultValue} 반환
     */
    protected final long getLong(@NonNull String key, long defaultValue) {
        checkAccess();
        return config.getLong(key, defaultValue);
    }

    /**
     * 파일에서 정수 값을 불러온다.
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 0 반환
     */
    protected final long getLong(@NonNull String key) {
        checkAccess();
        return getLong(key, 0);
    }

    /**
     * 파일에서 실수 값을 불러온다.
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 {@code null} 반환
     */
    protected final double getDouble(@NonNull String key, double defaultValue) {
        checkAccess();
        return config.getDouble(key, defaultValue);
    }

    /**
     * 파일에서 실수 값을 불러온다.
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 0 반환
     */
    protected final double getDouble(@NonNull String key) {
        checkAccess();
        return getDouble(key, 0);
    }

    /**
     * 파일에서 문자열 값을 불러온다.
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 값. 데이터가 존재하지 않으면 {@code defaultValue} 반환
     */
    protected final String getString(@NonNull String key, String defaultValue) {
        checkAccess();
        return config.getString(key, defaultValue);
    }

    /**
     * 파일에서 문자열 값을 불러온다.
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 {@code null} 반환
     */
    protected final String getString(@NonNull String key) {
        checkAccess();
        return getString(key, null);
    }

    /**
     * 파일에서 부울 값을 불러온다.
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 값. 데이터가 존재하지 않으면 {@code defaultValue} 반환
     */
    protected final boolean getBoolean(@NonNull String key, boolean defaultValue) {
        checkAccess();
        return config.getBoolean(key, defaultValue);
    }

    /**
     * 파일에서 부울 값을 불러온다.
     *
     * @param key 키
     * @return 값. 데이터가 존재하지 않으면 {@code null} 반환
     */
    protected final boolean getBoolean(@NonNull String key) {
        checkAccess();
        return getBoolean(key, false);
    }
}
