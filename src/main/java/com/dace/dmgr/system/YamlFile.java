package com.dace.dmgr.system;

import com.dace.dmgr.DMGR;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Yaml 파일 관리 기능을 제공하는 클래스.
 *
 * <p>하나의 Yaml 파일에 여러 key-value 쌍을 저장할 수 있다.</p>
 *
 * <p>Example:</p>
 *
 * <pre>
 * [TestYaml.yml]
 *   key1: TestValue
 *   key2: 1234
 * </pre>
 */
public final class YamlFile {
    /** Yaml 설정 객체 */
    private final YamlConfiguration config;
    /** 파일 저장을 위한 객체 */
    private final File file;

    /**
     * 파일 관리 인스턴스를 생성한다.
     *
     * <p>지정한 경로에 Yaml 파일이 생성된다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // foo/testFile.yml 생성
     * new YamlFile("foo/testFile")
     * }</pre>
     *
     * @param path 파일 경로
     */
    public YamlFile(String path) {
        file = new File(DMGR.getPlugin().getDataFolder(), path + ".yml");
        config = YamlConfiguration.loadConfiguration(file);

        try {
            if (!file.exists())
                config.save(file);
            config.load(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 파일에 값을 저장한다.
     *
     * @param key   키
     * @param value 값
     */
    public void set(String key, Object value) {
        try {
            config.set(key, value);
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 파일에서 값을 불러온다.
     *
     * @param key 키
     * @param <T> 지정한 값의 타입
     * @return 값. 데이터가 존재하지 않으면 {@code null}을 반환한다.
     */
    public <T> T get(String key) {
        return get(key, null);
    }

    /**
     * 파일에서 값을 불러온다.
     *
     * @param key 키
     * @param def 기본값
     * @param <T> 지정한 값의 타입
     * @return 값. 데이터가 존재하지 않으면 {@code def}를 반환한다.
     */
    public <T> T get(String key, T def) {
        return (T) config.get(key, def);
    }
}
