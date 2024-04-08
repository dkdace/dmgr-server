package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * 전투 시스템에 사용되는 기능을 제공하는 클래스.
 */
@UtilityClass
public final class CombatUtil {
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
     * @param weakeningDistance 피해 감소가 시작하는 거리. (단위: 블록)
     * @param isHalf            {@code true}면 최소 피해량이 절반까지만 감소,
     *                          {@code false}면 최소 피해량이 0이 될 때까지 감소
     * @return 최종 피해량
     * @throws IllegalArgumentException 두 위치가 서로 다른 월드에 있으면 발생
     */
    public static int getDistantDamage(@NonNull Location start, @NonNull Location end, int damage, double weakeningDistance, boolean isHalf) {
        if (start.getWorld() != end.getWorld())
            throw new IllegalArgumentException("'start'와 'end'가 서로 다른 월드에 있음");

        double distance = start.distance(end);

        if (distance > weakeningDistance) {
            distance = distance - weakeningDistance;
            int finalDamage = (int) ((damage / 2.0) * ((weakeningDistance - distance) / weakeningDistance) + damage / 2.0);

            if (isHalf && finalDamage < damage / 2) {
                finalDamage = damage / 2;
            } else if (finalDamage < 0)
                finalDamage = 0;

            return finalDamage;
        } else
            return damage;
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 가장 가까운 엔티티를 반환한다.
     *
     * @param location  위치
     * @param range     범위 (반지름). (단위: 블록)
     * @param condition 조건
     * @return 범위 내 가장 가까운 엔티티
     */
    public static CombatEntity getNearCombatEntity(@NonNull Location location, double range, @NonNull Predicate<CombatEntity> condition) {
        return Arrays.stream(CombatEntityUtil.getAllExcluded())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted() &&
                                location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes()).mapToDouble(hitbox ->
                                hitbox.getDistance(location)).min().orElse(Double.MAX_VALUE) <= range)
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 가장 가까운 엔티티를 반환한다.
     *
     * @param game      대상 게임. {@code null}로 지정 시
     *                  {@link CombatUtil#getNearCombatEntity(Location, double, Predicate)} 호출
     * @param location  위치
     * @param range     범위 (반지름). (단위: 블록)
     * @param condition 조건
     * @return 범위 내 가장 가까운 엔티티
     */
    public static CombatEntity getNearCombatEntity(Game game, @NonNull Location location, double range, @NonNull Predicate<CombatEntity> condition) {
        if (game == null)
            return getNearCombatEntity(location, range, condition);
        return Arrays.stream(game.getAllCombatEntities())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted() &&
                                location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes()).mapToDouble(hitbox ->
                                hitbox.getDistance(location)).min().orElse(Double.MAX_VALUE) <= range)
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param location  위치
     * @param range     범위 (반지름). (단위: 블록)
     * @param condition 조건
     * @return 범위 내 모든 엔티티
     */
    @NonNull
    public static CombatEntity[] getNearCombatEntities(@NonNull Location location, double range, @NonNull Predicate<CombatEntity> condition) {
        return Arrays.stream(CombatEntityUtil.getAllExcluded())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted() &&
                                location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes()).anyMatch(hitbox -> hitbox.isInHitbox(location, range)))
                .toArray(CombatEntity[]::new);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param game      대상 게임. {@code null}로 지정 시
     *                  {@link CombatUtil#getNearCombatEntities(Location, double, Predicate)} 호출
     * @param location  위치
     * @param range     범위 (반지름). (단위: 블록)
     * @param condition 조건
     * @return 범위 내 모든 엔티티
     */
    @NonNull
    public static CombatEntity[] getNearCombatEntities(Game game, @NonNull Location location, double range, @NonNull Predicate<CombatEntity> condition) {
        if (game == null)
            return getNearCombatEntities(location, range, condition);
        return Arrays.stream(game.getAllCombatEntities())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted() &&
                                location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes()).anyMatch(hitbox -> hitbox.isInHitbox(location, range)))
                .toArray(CombatEntity[]::new);
    }

    /**
     * 지정한 플레이어의 시야(yaw/pitch)를 변경한다.
     *
     * @param player 대상 플레이어
     * @param yaw    변경할 yaw
     * @param pitch  변경할 pitch
     * @see CombatUtil#addYawAndPitch(Player, double, double)
     */
    public static void setYawAndPitch(@NonNull Player player, double yaw, double pitch) {
        WrapperPlayServerPosition packet = new WrapperPlayServerPosition();

        packet.setX(0);
        packet.setY(0);
        packet.setZ(0);
        packet.setYaw((float) yaw);
        packet.setPitch((float) pitch);
        packet.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.X,
                WrapperPlayServerPosition.PlayerTeleportFlag.Y, WrapperPlayServerPosition.PlayerTeleportFlag.Z)));

        packet.sendPacket(player);
    }

    /**
     * 지정한 플레이어의 시야(yaw/pitch)를 변경한다.
     *
     * @param player 대상 플레이어
     * @param yaw    추가할 yaw
     * @param pitch  추가할 pitch
     * @see CombatUtil#setYawAndPitch(Player, double, double)
     */
    public static void addYawAndPitch(@NonNull Player player, double yaw, double pitch) {
        WrapperPlayServerPosition packet = new WrapperPlayServerPosition();

        packet.setX(0);
        packet.setY(0);
        packet.setZ(0);
        packet.setYaw((float) yaw);
        packet.setPitch((float) pitch);
        packet.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.values())));

        packet.sendPacket(player);
    }

    /**
     * 지정한 플레이어에게 화면 반동 효과를 적용한다.
     *
     * <p>주로 총기 반동에 사용된다.</p>
     *
     * @param combatUser      대상 플레이어
     * @param up              수직 반동
     * @param side            수평 반동
     * @param upSpread        수직 반동 분산도
     * @param sideSpread      수평 반동 분산도
     * @param duration        반동 진행 시간 (tick)
     * @param firstMultiplier 초탄 반동 계수. 1로 설정 시 차탄과 동일
     */
    public static void setRecoil(@NonNull CombatUser combatUser, double up, double side, double upSpread, double sideSpread, int duration, double firstMultiplier) {
        final double finalUpSpread = upSpread * (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 0.5;
        final double finalSideSpread = sideSpread * (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 0.5;
        final boolean first = CooldownUtil.getCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY) == 0;
        CooldownUtil.setCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY);

        int sum = 0;
        for (int i = 1; i <= duration; i++) {
            sum += i;
        }

        final int finalSum = sum;

        TaskUtil.addTask(combatUser, new IntervalTask(i -> {
            double finalUp = (up + finalUpSpread) / ((double) finalSum / (duration - i));
            double finalSide = (side + finalSideSpread) / ((double) finalSum / (duration - i));
            if (first) {
                finalUp *= firstMultiplier;
                finalSide *= firstMultiplier;
            }
            addYawAndPitch(combatUser.getEntity(), finalSide, -finalUp);

            return true;
        }, 1, duration));
    }
}
