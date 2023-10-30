package com.dace.dmgr.config;

import com.dace.dmgr.system.YamlFile;
import lombok.Getter;

/**
 * 전역 설정 클래스.
 */
public final class GeneralConfig {
    @Getter
    private static final GeneralConfig instance = new GeneralConfig();
    /** 설정파일 관리를 위한 객체 */
    private final YamlFile yamlFile = new YamlFile("GeneralConfig");
    /** 리소스팩 URL */
    @Getter
    private String resourcePackUrl = "";
    /** 채팅 쿨타임 */
    @Getter
    private int chatCooldown = 0;
    /** 명령어 쿨타임 */
    @Getter
    private int commandCooldown = 0;

    /**
     * 저장된 설정을 불러온다.
     */
    public GeneralConfig() {
        resourcePackUrl = yamlFile.get("resourcePackUrl", resourcePackUrl);
        chatCooldown = yamlFile.get("chatCooldown", chatCooldown);
        commandCooldown = yamlFile.get("commandCooldown", commandCooldown);
    }
}
