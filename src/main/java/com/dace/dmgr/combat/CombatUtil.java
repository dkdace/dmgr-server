package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Hitbox;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 전투 시스템에 사용되는 기능을 제공하는 클래스.
 */
public final class CombatUtil {
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
        CombatEntity<?> entity = EntityInfoRegistry.getAllCombatEntities().stream()
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
        return EntityInfoRegistry.getAllCombatEntities().stream()
                .filter(entity ->
                        entity != attacker && isEnemy(attacker, entity))
                .filter(entity ->
                        LocationUtil.isInHitbox(location, entity.getHitbox(), range) ||
                                LocationUtil.isInHitbox(location, entity.getCritHitbox(), range))
                .collect(Collectors.toSet());
    }

    /**
     * 지정한 플레이어의 시야(yaw/pitch)를 변경한다.
     *
     * @param player 대상 플레이어
     * @param yaw    변경할 yaw
     * @param pitch  변경할 pitch
     */
    private static void setYawAndPitch(Player player, float yaw, float pitch) {
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
     * 지정한 플레이어에게 화면 반동 효과를 적용한다. 총기 반동에 사용된다.
     *
     * @param combatUser      대상 플레이어
     * @param up              수직 반동
     * @param side            수평 반동
     * @param upSpread        수직 반동 분산도
     * @param sideSpread      수평 반동 분산도
     * @param ticks           반동 진행 시간
     * @param firstMultiplier 초탄 반동 계수. {@code 1}로 설정 시 차탄과 동일
     */
    public static void setRecoil(CombatUser combatUser, float up, float side, float upSpread, float sideSpread, int ticks, float firstMultiplier) {
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
                setYawAndPitch(combatUser.getEntity(), finalSide, -finalUp);

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
    public static void setBulletSpread(CombatUser combatUser, float increment, float recovery, float max) {
        if (combatUser.getBulletSpread() == 0) {
            combatUser.addBulletSpread(increment, max);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
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
