package com.dace.dmgr.system;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.temporal.Temporal;
import com.dace.dmgr.lobby.User;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 엔티티의 정보 데이터를 저장하고 관리하는 클래스.
 */
public final class EntityInfoRegistry {
    private static final Map<Player, User> userMap = new HashMap<>();
    private static final Map<LivingEntity, CombatEntity> combatEntityMap = new HashMap<>();
    private static final Map<Player, CombatUser> combatUserMap = new HashMap<>();
    private static final Map<LivingEntity, Temporal> temporalEntityMap = new HashMap<>();

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
     * @return 모든 전투 시스템의 엔티티
     */
    public static Collection<CombatEntity> getAllCombatEntities() {
        return combatEntityMap.values();
    }

    /**
     * @param entity 엔티티
     * @return 전투 시스템의 엔티티 객체
     */
    public static CombatEntity getCombatEntity(LivingEntity entity) {
        return combatEntityMap.get(entity);
    }

    /**
     * @param player 플레이어
     * @return 전투 시스템의 플레이어 객체
     */
    public static CombatUser getCombatUser(Player player) {
        return combatUserMap.get(player);
    }

    /**
     * @param player     플레이어
     * @param combatUser 전투 시스템의 플레이어 객체
     */
    public static void addCombatUser(Player player, CombatUser combatUser) {
        combatUserMap.put(player, combatUser);
        combatEntityMap.put(player, combatUser);
    }

    /**
     * @param player 플레이어
     */
    public static void removeCombatUser(Player player) {
        combatUserMap.remove(player);
        combatEntityMap.remove(player);
    }

    /**
     * @param entity 엔티티
     * @return 전투 시스템의 일시적 엔티티 객체
     */
    public static Temporal getTemporalEntity(LivingEntity entity) {
        return temporalEntityMap.get(entity);
    }

    /**
     * @param entity         엔티티
     * @param temporalEntity 전투 시스템의 일시적 엔티티 객체
     */
    public static void addTemporalEntity(LivingEntity entity, Temporal temporalEntity) {
        temporalEntityMap.put(entity, temporalEntity);
        combatEntityMap.put(entity, temporalEntity);
    }

    /**
     * @param entity 엔티티
     */
    public static void removeTemporalEntity(LivingEntity entity) {
        temporalEntityMap.remove(entity);
        combatEntityMap.remove(entity);
    }
}
