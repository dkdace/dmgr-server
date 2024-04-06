package com.dace.dmgr.game.map;

import lombok.NonNull;
import org.bukkit.World;

/**
 * 게임에 사용되는 맵을 관리하는 인터페이스.
 */
public interface GameMap {
    /**
     * @return 맵 이름
     */
    @NonNull
    String getName();

    /**
     * @return 맵 월드
     */
    @NonNull
    World getWorld();

    /**
     * @return 레드 팀 스폰 위치 목록
     */
    @NonNull
    GlobalLocation @NonNull [] getRedTeamSpawns();

    /**
     * @return 블루 팀 스폰 위치 목록
     */
    @NonNull
    GlobalLocation @NonNull [] getBlueTeamSpawns();

    /**
     * @return 힐 팩 위치 목록
     */
    @NonNull
    GlobalLocation @NonNull [] getHealPackLocations();
}
