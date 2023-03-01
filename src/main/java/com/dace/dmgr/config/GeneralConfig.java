package com.dace.dmgr.config;

import com.dace.dmgr.util.YamlFile;

/**
 * 전역 설정 클래스.
 */
public class GeneralConfig {
    /** 리소스팩 URL */
    public static String resourcePackUrl = "";
    /** 채팅 쿨타임 */
    public static int chatCooldown = 0;
    /** 명령어 쿨타임 */
    public static int commandCooldown = 0;
    private static GeneralConfig instance;
    /** 설정파일 관리를 위한 객체 */
    public YamlFile yamlFile = new YamlFile("GeneralConfig");

    /**
     * 저장된 설정을 불러온다.
     */
    public GeneralConfig() {
        resourcePackUrl = yamlFile.get("resourcePackUrl", resourcePackUrl);
        chatCooldown = yamlFile.get("chatCooldown", chatCooldown);
        commandCooldown = yamlFile.get("commandCooldown", commandCooldown);
    }

    /**
     * 인스턴스를 초기화한다.
     */
    public static void init() {
        if (instance == null)
            instance = new GeneralConfig();
    }
}
