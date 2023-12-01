package com.dace.dmgr.system;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.lobby.User;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 엔티티의 정보 데이터를 저장하고 관리하는 클래스.
 */
public final class EntityInfoRegistry {
    /** 유저 목록 (플레이어 : 유저 정보) */
    private static final Map<Player, User> userMap = new HashMap<>();
    /** 게임 유저 목록 (플레이어 : 게임 유저 정보) */
    private static final Map<Player, GameUser> gameUserMap = new HashMap<>();
    /** 엔티티 목록 (엔티티 : 전투 시스템의 엔티티 정보) */
    private static final Map<LivingEntity, CombatEntity> combatEntityMap = new HashMap<>();
    /** 게임에 소속되지 않은 엔티티 목록 */
    private static final Set<CombatEntity> independentCombatEntitySet = new HashSet<>();

    /**
     * @param player 플레이어
     * @return 유저 정보 객체
     */
    public static User getUser(Player player) {
        return userMap.get(player);
    }

    /**
     * @param player 플레이어
     * @param user   유저 정보 객체
     */
    public static void addUser(Player player, User user) {
        userMap.put(player, user);
    }

    /**
     * @param player 플레이어
     */
    public static void removeUser(Player player) {
        userMap.remove(player);
    }

    /**
     * @param player 플레이어
     * @return 유저 정보 객체
     */
    public static GameUser getGameUser(Player player) {
        return gameUserMap.get(player);
    }

    /**
     * @param player   플레이어
     * @param gameUser 게임 시스템의 플레이어 객체
     */
    public static void addGameUser(Player player, GameUser gameUser) {
        gameUserMap.put(player, gameUser);
    }

    /**
     * @param player 플레이어
     */
    public static void removeGameUser(Player player) {
        gameUserMap.remove(player);
    }

    /**
     * @param entity 엔티티
     * @return 전투 시스템의 엔티티 객체
     */
    public static CombatEntity getCombatEntity(LivingEntity entity) {
        return combatEntityMap.get(entity);
    }

    /**
     * @param entity       엔티티
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public static void addCombatEntity(LivingEntity entity, CombatEntity combatEntity) {
        combatEntityMap.put(entity, combatEntity);
    }

    /**
     * @param entity 엔티티
     */
    public static void removeCombatEntity(LivingEntity entity) {
        combatEntityMap.remove(entity);
    }

    /**
     * @param player 플레이어
     * @return 전투 시스템의 플레이어 객체
     */
    public static CombatUser getCombatUser(Player player) {
        return combatEntityMap.get(player) == null ? null : (CombatUser) combatEntityMap.get(player);
    }

    /**
     * 게임에 소속되지 않은 모든 엔티티를 반환한다.
     *
     * @return 게임에 소속되지 않은 모든 엔티티
     */
    public static CombatEntity[] getAllIndependentCombatEntities() {
        return independentCombatEntitySet.toArray(new CombatEntity[0]);
    }

    /**
     * 게임에 소속되지 않은 엔티티를 추가한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public static void addIndependentCombatEntity(CombatEntity combatEntity) {
        independentCombatEntitySet.add(combatEntity);
    }

    /**
     * 게임에 소속되지 않은 엔티티를 제거한다.
     *
     * @param combatEntity 전투 시스템의 엔티티 객체
     */
    public static void removeIndependentCombatEntity(CombatEntity combatEntity) {
        independentCombatEntitySet.remove(combatEntity);
    }
}
