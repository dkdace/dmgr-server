package com.dace.dmgr.game;

import com.dace.dmgr.system.YamlFile;

/**
 * 게임 설정 클래스.
 */
public final class GameConfig {
    /** 일반 게임과 랭크 게임의 최대 방 갯수 */
    public static final int MAX_ROOM_COUNT;
    /** 게임을 시작하기 위한 최소 인원 수 (일반) */
    public static final int NORMAL_MIN_PLAYER_COUNT;
    /** 최대 수용 가능한 인원 수 (일반) */
    public static final int NORMAL_MAX_PLAYER_COUNT;
    /** 게임을 시작하기 위한 최소 인원 수 (랭크) */
    public static final int RANK_MIN_PLAYER_COUNT;
    /** 최대 수용 가능한 인원 수 (랭크) */
    public static final int RANK_MAX_PLAYER_COUNT;
    /** 랭크가 결정되는 배치 판 수 */
    public static final int RANK_PLACEMENT_PLAY_COUNT;
    /** 게임 시작까지 필요한 대기 시간 (초) */
    public static final int WAITING_TIME;
    /** 상대 팀 스폰 입장 시 초당 피해량 */
    public static final int OPPOSITE_SPAWN_DAMAGE_PER_SECOND;
    /** 설정파일 관리를 위한 객체 */
    private static final YamlFile yamlFile = new YamlFile("GameConfig");

    static {
        MAX_ROOM_COUNT = yamlFile.get("maxRoomCount", 5);
        NORMAL_MIN_PLAYER_COUNT = yamlFile.get("normalMinPlayerCount", 2);
        NORMAL_MAX_PLAYER_COUNT = yamlFile.get("normalMaxPlayerCount", 10);
        RANK_MIN_PLAYER_COUNT = yamlFile.get("rankMinPlayerCount", 6);
        RANK_MAX_PLAYER_COUNT = yamlFile.get("rankMaxPlayerCount", 12);
        RANK_PLACEMENT_PLAY_COUNT = yamlFile.get("rankPlacementPlayCount", 5);
        WAITING_TIME = yamlFile.get("waitingTime", 30);
        OPPOSITE_SPAWN_DAMAGE_PER_SECOND = yamlFile.get("oppositeSpawnDamagePerSecond", 250);
    }
}
