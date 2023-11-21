package com.dace.dmgr.game.map;

import com.dace.dmgr.game.GamePlayMode;
import org.bukkit.Material;

/**
 * 게임에 사용되는 맵을 관리하는 인터페이스.
 */
public interface GameMap {
    /**
     * @return 게임 모드
     */
    GamePlayMode getGamePlayMode();

    /**
     * @return 맵 이름
     */
    String getName();

    /**
     * @return 월드 이름
     */
    String getWorldName();

    /**
     * @return 레드 팀 스폰 위치 목록
     */
    GlobalLocation[] getRedTeamSpawns();

    /**
     * @return 블루 팀 스폰 위치 목록
     */
    GlobalLocation[] getBlueTeamSpawns();

    /**
     * 팀 스폰 지역을 확인하기 위한 블록.
     */
    interface REGION {
        /** 레드 팀 스폰 확인 블록 */
        Material RED_SPAWN_CHECK_BLOCK = Material.REDSTONE_ORE;
        /** 블루 팀 스폰 확인 블록 */
        Material BLUE_SPAWN_CHECK_BLOCK = Material.LAPIS_ORE;
        /** 스폰 지역 확인 Y 좌표 */
        int SPAWN_REGION_CHECK_Y_COORDINATE = 41;
    }
}
