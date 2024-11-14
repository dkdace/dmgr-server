package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * 전투 시스템에 사용되는 기능을 제공하는 클래스.
 */
@UtilityClass
public final class CombatUtil {
    /** 총기의 초탄 반동 딜레이 쿨타임 ID */
    private static final String WEAPON_FIRST_RECOIL_DELAY_COOLDOWN_ID = "WeaponFirstRecoilDelay";

    /**
     * 지정한 피해량에 거리별 피해량 감소가 적용된 최종 피해량을 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // 최종 피해량 : 10 (20m) ~ 5 (40m)
     * int damage = getDistantDamage(10, distance, 20)
     * </code></pre>
     *
     * @param damage            피해량
     * @param distance          거리 (단위: 블록). 0 이상의 값
     * @param weakeningDistance 피해 감소가 시작하는 거리. (단위: 블록). 0 이상의 값
     * @return 최종 피해량
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static int getDistantDamage(int damage, double distance, double weakeningDistance) {
        if (distance < 0)
            throw new IllegalArgumentException("'distance'가 0 이상이어야 함");
        if (weakeningDistance < 0)
            throw new IllegalArgumentException("'pitch'가 0 이상이어야 함");

        if (distance <= weakeningDistance)
            return damage;

        distance = distance - weakeningDistance;
        int finalDamage = (int) ((damage / 2.0) * ((weakeningDistance - distance) / weakeningDistance) + damage / 2.0);

        if (finalDamage < damage / 2)
            finalDamage = damage / 2;
        else if (finalDamage < 0)
            finalDamage = 0;

        return finalDamage;
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 가장 가까운 엔티티를 반환한다.
     *
     * @param location  위치
     * @param range     범위 (반지름). (단위: 블록). 0 이상의 값
     * @param condition 조건
     * @return 범위 내 가장 가까운 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntities(Location, double, Predicate)
     */
    @Nullable
    public static CombatEntity getNearCombatEntity(@NonNull Location location, double range, @NonNull Predicate<@NonNull CombatEntity> condition) {
        validateArgs(range);

        return Arrays.stream(CombatEntity.getAllExcluded())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted()
                                && location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes())
                                .mapToDouble(hitbox -> hitbox.getDistance(location))
                                .min()
                                .orElse(Double.MAX_VALUE) <= range)
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 가장 가까운 엔티티를 반환한다.
     *
     * @param game      대상 게임. {@code null}로 지정 시
     *                  {@link CombatUtil#getNearCombatEntity(Location, double, Predicate)} 호출
     * @param location  위치
     * @param range     범위 (반지름). (단위: 블록). 0 이상의 값
     * @param condition 조건
     * @return 범위 내 가장 가까운 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntities(Game, Location, double, Predicate)
     */
    @Nullable
    public static CombatEntity getNearCombatEntity(@Nullable Game game, @NonNull Location location, double range,
                                                   @NonNull Predicate<@NonNull CombatEntity> condition) {
        validateArgs(range);

        if (game == null)
            return getNearCombatEntity(location, range, condition);
        return Arrays.stream(game.getAllCombatEntities())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted()
                                && location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes())
                                .mapToDouble(hitbox -> hitbox.getDistance(location))
                                .min()
                                .orElse(Double.MAX_VALUE) <= range)
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param location  위치
     * @param range     범위 (반지름). (단위: 블록). 0 이상의 값
     * @param condition 조건
     * @return 범위 내 모든 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntity(Location, double, Predicate)
     */
    @NonNull
    public static CombatEntity @NonNull [] getNearCombatEntities(@NonNull Location location, double range, @NonNull Predicate<@NonNull CombatEntity> condition) {
        validateArgs(range);

        return Arrays.stream(CombatEntity.getAllExcluded())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted()
                                && location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
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
     * @param range     범위 (반지름). (단위: 블록). 0 이상의 값
     * @param condition 조건
     * @return 범위 내 모든 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntity(Game, Location, double, Predicate)
     */
    @NonNull
    public static CombatEntity @NonNull [] getNearCombatEntities(@Nullable Game game, @NonNull Location location, double range,
                                                                 @NonNull Predicate<@NonNull CombatEntity> condition) {
        validateArgs(range);

        if (game == null)
            return getNearCombatEntities(location, range, condition);
        return Arrays.stream(game.getAllCombatEntities())
                .filter(condition)
                .filter(combatEntity ->
                        combatEntity.canBeTargeted()
                                && location.distance(combatEntity.getEntity().getLocation()) < combatEntity.getMaxHitboxSize() + range)
                .filter(combatEntity ->
                        Arrays.stream(combatEntity.getHitboxes()).anyMatch(hitbox -> hitbox.isInHitbox(location, range)))
                .toArray(CombatEntity[]::new);
    }

    /**
     * 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param range 범위 (반지름). (단위: 블록)
     */
    private static void validateArgs(double range) {
        if (range < 0)
            throw new IllegalArgumentException("'range'가 0 이상이어야 함");
    }

    /**
     * 지정한 플레이어의 시야(yaw/pitch) 값을 설정한다.
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
     * 지정한 플레이어의 시야(yaw/pitch) 값을 추가한다.
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
     * @param duration        반동 진행 시간 (tick). 1~5 사이의 값
     * @param firstMultiplier 초탄 반동 계수. 1로 설정 시 차탄과 동일
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void setRecoil(@NonNull CombatUser combatUser, double up, double side, double upSpread, double sideSpread, int duration, double firstMultiplier) {
        if (duration < 1 || duration > 5)
            throw new IllegalArgumentException("'duration'이 1에서 5 사이여야 함");
        if (firstMultiplier < 0)
            throw new IllegalArgumentException("'firstMultiplier'가 0 이상이어야 함");

        double finalUpSpread = upSpread * (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 0.5;
        double finalSideSpread = sideSpread * (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 0.5;
        boolean first = CooldownUtil.getCooldown(combatUser, WEAPON_FIRST_RECOIL_DELAY_COOLDOWN_ID) == 0;
        int sum = IntStream.rangeClosed(1, duration).sum();
        CooldownUtil.setCooldown(combatUser, WEAPON_FIRST_RECOIL_DELAY_COOLDOWN_ID, 4);

        TaskUtil.addTask(combatUser, new IntervalTask(i -> {
            double finalUp = (up + finalUpSpread) / ((double) sum / (duration - i));
            double finalSide = (side + finalSideSpread) / ((double) sum / (duration - i));
            if (first) {
                finalUp *= firstMultiplier;
                finalSide *= firstMultiplier;
            }
            addYawAndPitch(combatUser.getEntity(), finalSide, -finalUp);

            return true;
        }, 1, duration));
    }

    /**
     * 엔티티를 지정한 위치에 소환한다.
     *
     * @param entityClass 엔티티 클래스
     * @param location    소환할 위치
     * @param <T>         {@link LivingEntity}를 상속받는 엔티티 타입
     * @return 엔티티
     */
    @NonNull
    public static <T extends LivingEntity> T spawnEntity(@NonNull Class<T> entityClass, @NonNull Location location) {
        T entity = location.getWorld().spawn(location, entityClass);
        if (entity.getVehicle() != null) {
            entity.getVehicle().remove();
            entity.leaveVehicle();
        }
        entity.getEquipment().clear();

        return entity;
    }
}
