package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Hitbox;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static com.dace.dmgr.system.HashMapList.combatEntityMap;
import static com.dace.dmgr.system.HashMapList.combatUserMap;

/**
 * 전투 시스템에 사용되는 기능을 제공하는 클래스.
 */
public class CombatUtil {
    /** 적 처치 기여 (데미지 누적) 제한시간 */
    public static final int DAMAGE_SUM_TIME_LIMIT = 10 * 20;
    /** 암살 보너스 (첫 공격 후 일정시간 안에 적 처치) 제한시간 */
    public static final int FASTKILL_TIME_LIMIT = (int) 2.5 * 20;
    /** 리스폰 시간 */
    public static final int RESPAWN_TIME = 10 * 20;

    /**
     * 두 엔티티가 서로 적인 지 확인한다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @return 적이면 {@code true} 반환
     */
    public static boolean isEnemy(CombatEntity<?> attacker, CombatEntity<?> victim) {
        return !attacker.getTeam().equals(victim.getTeam());
    }

    /**
     * 지정한 위치를 기준으로 범위 안에 있는 가장 가까운 적과 치명타 여부를 반환한다.
     *
     * @param attacker 공격자 (기준 엔티티)
     * @param location 위치
     * @param range    범위 (반지름)
     * @return 범위 내 가장 가까운 적과 치명타 여부 (해당 적이 {@link CombatEntity#getCritHitbox()}
     * 안에 있으면 {@code true})
     * @see Hitbox
     */
    public static Map.Entry<CombatEntity<?>, Boolean> getNearEnemy(CombatEntity<?> attacker, Location location, float range) {
        CombatEntity<?> entity = combatEntityMap.values().stream()
                .min(Comparator.comparing(combatEntity -> Math.min(
                        location.distance(combatEntity.getHitbox().getCenter()),
                        location.distance(combatEntity.getCritHitbox().getCenter())
                )))
                .filter(combatEntity ->
                        combatEntity != attacker && isEnemy(attacker, combatEntity))
                .orElse(null);

        if (entity == null)
            return new AbstractMap.SimpleEntry<>(null, false);

        if (LocationUtil.isInHitbox(location, entity.getCritHitbox(), range)) {
            return new AbstractMap.SimpleEntry<>(entity, true);
        } else if (LocationUtil.isInHitbox(location, entity.getHitbox(), range)) {
            return new AbstractMap.SimpleEntry<>(entity, false);
        } else return new AbstractMap.SimpleEntry<>(null, false);
    }

    /**
     * 지정한 위치를 기준으로 범위 안에 있는 모든 적을 반환한다.
     *
     * @param attacker 공격자 (기준 엔티티)
     * @param location 위치
     * @param range    범위 (반지름)
     * @return 범위 내 모든 적
     * @see Hitbox
     */
    public static Set<CombatEntity<?>> getNearEnemies(CombatEntity<?> attacker, Location location, float range) {
        return combatEntityMap.values().stream()
                .filter(entity ->
                        entity != attacker && isEnemy(attacker, entity))
                .filter(entity ->
                        LocationUtil.isInHitbox(location, entity.getHitbox(), range) ||
                                LocationUtil.isInHitbox(location, entity.getCritHitbox(), range))
                .collect(Collectors.toSet());
    }

    /**
     * 엔티티에게 피격 효과 패킷을 전송한다.
     *
     * @param entity 대상 엔티티
     */
    public static void sendDamagePacket(Entity entity) {
        WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

        packet.setEntityID(entity.getEntityId());
        packet.setEntityStatus((byte) 2);

        packet.broadcastPacket();
    }

    /**
     * 지정한 플레이어에게 이동 패킷을 전송한다.
     *
     * @param player 대상 플레이어
     * @param yaw    변경할 yaw
     * @param pitch  변경할 pitch
     */
    private static void sendPacket(Player player, float yaw, float pitch) {
        WrapperPlayServerPosition packet = new WrapperPlayServerPosition();

        packet.setX(0);
        packet.setY(0);
        packet.setZ(0);
        packet.setYaw(yaw);
        packet.setPitch(pitch);
        packet.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.values())));

        packet.sendPacket(player);
    }

    /**
     * 지정한 플레이어에게 화면 반동 효과를 전송한다. 총기 반동에 사용된다.
     *
     * @param combatUser      대상 플레이어
     * @param up              수직 반동
     * @param side            수평 반동
     * @param upSpread        수직 반동 분산도
     * @param sideSpread      수평 반동 분산도
     * @param ticks           반동 진행 시간
     * @param firstMultiplier 초탄 반동 계수
     */
    public static void sendRecoil(CombatUser combatUser, float up, float side, float upSpread, float sideSpread, int ticks, float firstMultiplier) {
        final float finalUpSpread = (float) (upSpread * (Math.random() - Math.random()) * 0.5);
        final float finalSideSpread = (float) (sideSpread * (Math.random() - Math.random()) * 0.5);
        final boolean first = CooldownManager.getCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY) == 0;
        CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY);

        int sum = 0;
        for (int i = 1; i <= ticks; i++) {
            sum += i;
        }

        final int finalSum = sum;

        new TaskTimer(1, ticks) {
            @Override
            public boolean run(int i) {
                float finalUp = (up + finalUpSpread) / ((float) finalSum / (ticks - i));
                float finalSide = (side + finalSideSpread) / ((float) finalSum / (ticks - i));
                if (first) {
                    finalUp *= firstMultiplier;
                    finalSide *= firstMultiplier;
                }
                sendPacket(combatUser.getEntity(), finalSide, -finalUp);

                return true;
            }
        };
    }

    /**
     * 지정한 플레이어에게 총기 탄퍼짐 시스템을 적용한다.
     *
     * @param combatUser 대상 플레이어
     * @param increment  탄퍼짐 증가량
     * @param recovery   탄퍼짐 회복량
     * @param max        탄퍼짐 최대치
     */
    public static void applyBulletSpread(CombatUser combatUser, float increment, float recovery, float max) {
        if (combatUser.getBulletSpread() == 0) {
            combatUser.addBulletSpread(increment, max);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    if (combatUserMap.get(combatUser.getEntity()) == null)
                        return false;

                    if (CooldownManager.getCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY) == 0) {
                        if (combatUser.getBulletSpread() == 0)
                            return false;
                        combatUser.addBulletSpread(-recovery, max);
                    }
                    return true;
                }
            };
        } else
            combatUser.addBulletSpread(increment, max);
    }
}
