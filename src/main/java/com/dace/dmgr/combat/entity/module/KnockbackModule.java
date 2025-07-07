package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 엔티티의 넉백 모듈 클래스.
 */
public final class KnockbackModule extends CombatEntityModule<Movable> {
    /** 넉백 타임스탬프 */
    private Timestamp knockbackTimestamp = Timestamp.now();

    /**
     * 넉백 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public KnockbackModule(@NonNull Movable combatEntity) {
        super(combatEntity);
    }

    /**
     * 지정한 엔티티가 {@link Movable}인 경우 특정 방향과 속도로 밀쳐낸다.
     *
     * <p>주로 넉백 스킬에 사용된다.</p>
     *
     * @param combatEntity 대상 엔티티
     * @param direction    방향
     * @param speed        속도
     * @param isReset      초기화 여부. {@code true}로 지정 시 기존 속도 초기화
     * @see KnockbackModule#knockback(Vector, boolean)
     */
    public static void knockback(@NonNull Damageable combatEntity, @NonNull Vector direction, double speed, boolean isReset) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getKnockbackModule().knockback(direction.clone().normalize().multiply(speed), isReset);
    }

    /**
     * 지정한 엔티티가 {@link Movable}인 경우 특정 방향과 속도로 밀쳐낸다.
     *
     * <p>주로 넉백 스킬에 사용된다.</p>
     *
     * @param combatEntity 대상 엔티티
     * @param direction    방향
     * @param speed        속도
     * @see KnockbackModule#knockback(Vector)
     */
    public static void knockback(@NonNull Damageable combatEntity, @NonNull Vector direction, double speed) {
        knockback(combatEntity, direction, speed, false);
    }

    /**
     * 지정한 엔티티가 {@link Movable}인 경우 판정 중심지에서 맞은 위치로 밀쳐낸다.
     *
     * <p>주로 범위형 넉백 스킬에 사용된다.</p>
     *
     * @param combatEntity 대상 엔티티
     * @param center       판정 중심지
     * @param location     맞은 위치
     * @param speed        속도
     * @param isReset      초기화 여부. {@code true}로 지정 시 기존 속도 초기화
     * @see KnockbackModule#knockback(Vector, boolean)
     */
    public static void knockback(@NonNull Damageable combatEntity, @NonNull Location center, @NonNull Location location, double speed, boolean isReset) {
        knockback(combatEntity, LocationUtil.getDirection(center, location.clone().add(0, 0.5, 0)), speed, isReset);
    }

    /**
     * 지정한 엔티티가 {@link Movable}인 경우 판정 중심지에서 맞은 위치로 밀쳐낸다.
     *
     * <p>주로 범위형 넉백 스킬에 사용된다.</p>
     *
     * @param combatEntity 대상 엔티티
     * @param center       판정 중심지
     * @param location     맞은 위치
     * @param speed        속도
     * @see KnockbackModule#knockback(Vector)
     */
    public static void knockback(@NonNull Damageable combatEntity, @NonNull Location center, @NonNull Location location, double speed) {
        knockback(combatEntity, center, location, speed, false);
    }

    @Override
    protected double getBaseValue() {
        return 1;
    }

    /**
     * 엔티티를 지정한 속도로 강제로 밀쳐낸다. (넉백 효과).
     *
     * <p>또한 잠시동안 이동기({@link MoveModule#push(Vector, boolean)})의 사용을 제한한다.</p>
     *
     * @param velocity 속도
     * @param isReset  초기화 여부. {@code true}로 지정 시 기존 속도 초기화
     * @see MoveModule#push(Vector, boolean)
     */
    public void knockback(@NonNull Vector velocity, boolean isReset) {
        knockbackTimestamp = Timestamp.now().plus(Timespan.ofTicks(3));

        Vector finalVelocity = velocity.clone().multiply(Math.max(0, 2 - getValue()));
        combatEntity.getEntity().setVelocity(isReset ? finalVelocity : combatEntity.getEntity().getVelocity().add(finalVelocity));

        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getCombatantType().getCombatant().onKnockbacked((CombatUser) combatEntity, velocity.length());
    }

    /**
     * 엔티티를 지정한 속도로 강제로 밀쳐낸다. (넉백 효과).
     *
     * <p>또한 잠시동안 이동기({@link MoveModule#push(Vector)})의 사용을 제한한다.</p>
     *
     * @param velocity 속도
     * @see MoveModule#push(Vector)
     */
    public void knockback(@NonNull Vector velocity) {
        knockback(velocity, false);
    }

    /**
     * 엔티티가 넉백 효과를 받은 상태인지 확인한다.
     *
     * @return 넉백 효과 상태 여부
     */
    public boolean isKnockbacked() {
        return knockbackTimestamp.isAfter(Timestamp.now());
    }
}
