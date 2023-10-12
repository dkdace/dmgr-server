package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.HasCritHitbox;
import com.dace.dmgr.combat.entity.Hitbox;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
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
     * 지정한 피해량에 거리별 피해량 감소가 적용된 최종 피해량을 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 최종 피해량 : 10 (20m) ~ 5 (40m)
     * int damage = getDistantDamage(loc1, loc2, 10, 20, true)
     * // 최종 피해량 : 20 (10m) ~ 10 (20m) ~ 0 (30m)
     * int damage = getDistantDamage(loc1, loc2, 20, 10, false)
     * }</pre>
     *
     * @param start             시작 위치
     * @param end               끝 위치
     * @param damage            피해량
     * @param weakeningDistance 피해 감소가 시작하는 거리
     * @param isHalf            {@code true}면 최소 피해량이 절반까지만 감소하며,
     *                          {@code false}면 최소 피해량이 0이 될 때까지 감소한다.
     * @return 최종 피해량
     */
    public static int getDistantDamage(Location start, Location end, int damage, double weakeningDistance, boolean isHalf) {
        double distance = start.distance(end);

        if (distance > weakeningDistance) {
            distance = distance - weakeningDistance;
            int finalDamage = (int) ((damage / 2) * ((weakeningDistance - distance) / weakeningDistance) + damage / 2);

            if (isHalf && finalDamage < damage / 2) {
                finalDamage = damage / 2;
            } else if (finalDamage < 0)
                finalDamage = 0;

            return finalDamage;
        } else
            return damage;
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 가장 가까운 적과 치명타 여부를 반환한다.
     *
     * @param attacker  공격자 (기준 엔티티)
     * @param location  위치
     * @param range     범위 (반지름)
     * @param predicate 조건
     * @return 범위 내 가장 가까운 적과 치명타 여부 (해당 적이 {@link HasCritHitbox}을 상속받고 지정한 위치가
     * {@link HasCritHitbox#getCritHitbox()} 안에 있으면 {@code true})
     * @see Hitbox
     */
    public static CombatEntity<?> getNearEnemy(CombatEntity<?> attacker, Location location, float range, Predicate<CombatEntity<?>> predicate) {
        return EntityInfoRegistry.getAllCombatEntities().stream()
                .filter(predicate)
                .filter(combatEntity ->
                        combatEntity != attacker && isEnemy(attacker, combatEntity) &&
                                location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes()).mapToDouble(hitbox ->
                                hitbox.getDistance(location)).min().orElse(Double.MAX_VALUE) <= range)
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안에 있는 가장 가까운 적과 치명타 여부를 반환한다.
     *
     * @param attacker 공격자 (기준 엔티티)
     * @param location 위치
     * @param range    범위 (반지름)
     * @return 범위 내 가장 가까운 적과 치명타 여부 (해당 적이 {@link HasCritHitbox}을 상속받고 지정한 위치가
     * {@link HasCritHitbox#getCritHitbox()} 안에 있으면 {@code true})
     * @see Hitbox
     */
    public static CombatEntity<?> getNearEnemy(CombatEntity<?> attacker, Location location, float range) {
        return getNearEnemy(attacker, location, range, combatEntity -> true);
    }

    /**
     * 지정한 위치를 기준으로 범위 안에 있는 모든 적을 반환한다.
     *
     * @param attacker       공격자 (기준 엔티티)
     * @param location       위치
     * @param range          범위 (반지름)
     * @param canContainSelf 자가 포함 여부. {@code true}로 지정하면 공격자도 포함된다.
     * @return 범위 내 모든 적
     * @see Hitbox
     */
    public static Set<CombatEntity<?>> getNearEnemies(CombatEntity<?> attacker, Location location, float range, boolean canContainSelf) {
        return EntityInfoRegistry.getAllCombatEntities().stream()
                .filter(combatEntity ->
                        ((combatEntity != attacker && isEnemy(attacker, combatEntity)) || (canContainSelf && (attacker == combatEntity))) &&
                                location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity -> Arrays.stream(combatEntity.getHitboxes()).anyMatch(hitbox -> hitbox.isInHitbox(location, range)))
                .collect(Collectors.toSet());
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
        return getNearEnemies(attacker, location, range, false);
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
