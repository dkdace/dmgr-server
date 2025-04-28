package com.dace.dmgr.game;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.GlobalLocation;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.game.map.GameMap;
import com.dace.dmgr.util.LocationUtil;
import lombok.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * 게임에서 사용하는 팀 정보를 관리하는 클래스.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Team {
    /** 현재 게임 */
    private final Game game;
    /** 팀 종류 */
    @NonNull
    @Getter
    private final Type type;
    /** 소속된 플레이어 목록 */
    private final HashSet<GameUser> teamUsers = new HashSet<>();
    /** 팀 점수 */
    @Getter
    private int score;

    /**
     * 게임 플레이어 추가 시 실행할 작업.
     *
     * @param gameUser 대상 플레이어
     */
    void onAddGameUser(@NonNull GameUser gameUser) {
        teamUsers.add(gameUser);
    }

    /**
     * 게임 플레이어 제거 시 실행할 작업.
     *
     * @param gameUser 대상 플레이어
     */
    void onRemoveGameUser(@NonNull GameUser gameUser) {
        teamUsers.remove(gameUser);
    }

    /**
     * 상태 팀을 반환한다.
     *
     * @return 상대 팀
     */
    @NonNull
    public Team getOppositeTeam() {
        return type.oppositeTeamFunction.apply(game);
    }

    /**
     * 팀에 소속된 모든 플레이어 목록을 반환한다.
     *
     * @return 팀에 속한 모든 플레이어
     */
    @NonNull
    @UnmodifiableView
    public Set<@NonNull GameUser> getTeamUsers() {
        return Collections.unmodifiableSet(teamUsers);
    }

    /**
     * 지정한 플레이어가 스폰 지역 안에 있는지 확인한다.
     *
     * @param gameUser 확인할 플레이어
     * @return 해당 플레이어가 팀 스폰 내부에 있으면 {@code true} 반환
     */
    public boolean isInSpawn(@NonNull GameUser gameUser) {
        return LocationUtil.isInSameBlockXZ(gameUser.getPlayer().getLocation(), GeneralConfig.getGameConfig().getSpawnRegionCheckYCoordinate(),
                type.teamSpawnBlockType);
    }

    /**
     * 팀 점수를 1 증가시킨다.
     */
    void addScore() {
        this.score += 1;
    }

    /**
     * 지정한 전투원을 선택한 팀원이 있는지 중복 여부를 확인한다.
     *
     * @param combatantType 확인할 전투원
     * @return 중복 여부
     */
    public boolean checkCombatantDuplication(@NonNull CombatantType combatantType) {
        return teamUsers.stream().anyMatch(targetGameUser -> {
            CombatUser targetCombatUser = CombatUser.fromUser(targetGameUser.getUser());
            return targetCombatUser != null && targetCombatUser.getCombatantType() == combatantType;
        });
    }

    /**
     * 팀 종류 (레드/블루).
     */
    @AllArgsConstructor
    public enum Type {
        RED(ChatColor.RED, "레드", GameMap::getRedTeamSpawns, Game::getBlueTeam, GeneralConfig.getGameConfig().getRedTeamSpawnBlock()),
        BLUE(ChatColor.BLUE, "블루", GameMap::getBlueTeamSpawns, Game::getRedTeam, GeneralConfig.getGameConfig().getBlueTeamSpawnBlock());

        /** 팀 색상 */
        @Getter
        private final ChatColor color;
        /** 이름 */
        @Getter
        private final String name;
        /** 팀 스폰 위치 목록 반환에 실행할 작업 */
        @Getter(AccessLevel.PACKAGE)
        private final Function<GameMap, GlobalLocation[]> teamSpawnFunction;
        /** 상대 팀 반환에 실행할 작업 */
        private final Function<Game, Team> oppositeTeamFunction;
        /** 팀 스폰 식별 블록 타입 */
        private final Material teamSpawnBlockType;
    }
}
