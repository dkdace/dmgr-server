package com.dace.dmgr.system;

/**
 * 기본 전역 설정 클래스.
 */
public final class GeneralConfig {
    /** 리소스팩 URL */
    public static final String RESOURCE_PACK_URL;
    /** 채팅 쿨타임 */
    public static final int CHAT_COOLDOWN;
    /** 명령어 쿨타임 */
    public static final int COMMAND_COOLDOWN;
    /** 랭킹 업데이트 주기 (분) */
    public static final int RANKING_UPDATE_PERIOD;
    /** 설정파일 관리를 위한 객체 */
    private static final YamlFile yamlFile = new YamlFile("GeneralConfig");

    static {
        RESOURCE_PACK_URL = yamlFile.get("resourcePackUrl", "");
        CHAT_COOLDOWN = yamlFile.get("chatCooldown", 0);
        COMMAND_COOLDOWN = yamlFile.get("commandCooldown", 0);
        RANKING_UPDATE_PERIOD = yamlFile.get("rankingUpdatePeriod", 5);
    }
}
