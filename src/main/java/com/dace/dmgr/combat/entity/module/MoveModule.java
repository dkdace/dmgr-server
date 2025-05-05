package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.util.EntityUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * 움직일 수 있는 엔티티의 모듈 클래스.
 *
 * <p>엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 */
public final class MoveModule {
    /** 넉백 저항 기본값 */
    private static final double DEFAULT_VALUE = 1;

    /** 엔티티 인스턴스 */
    private final Movable combatEntity;
    /** 이동속도 값 */
    @NonNull
    @Getter
    private final AbilityStatus speedStatus;
    /** 넉백 저항 값 */
    @NonNull
    @Getter
    private final AbilityStatus resistanceStatus;

    /** 넉백 타임스탬프 */
    private Timestamp knockbackTimestamp = Timestamp.now();

    /**
     * 이동 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param speed        이동속도 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public MoveModule(@NonNull Movable combatEntity, double speed) {
        Validate.isTrue(speed >= 0, "speed >= 0 (%f)", speed);
        Validate.isTrue(combatEntity.getEntity() instanceof LivingEntity, "combatEntity.getEntity()가 LivingEntity를 상속받지 않음");

        this.combatEntity = combatEntity;
        this.speedStatus = new AbilityStatus(speed);
        this.resistanceStatus = new AbilityStatus(DEFAULT_VALUE);

        combatEntity.addOnTick(i -> {
            double finalSpeed = Math.max(0, getFinalSpeed());

            LivingEntity livingEntity = combatEntity.getEntity();
            livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(finalSpeed);

            if (livingEntity instanceof Player)
                ((Player) livingEntity).setFlySpeed((float) (finalSpeed * 0.35));

            if (canJump() && combatEntity.canJump()) {
                if (livingEntity.hasPotionEffect(PotionEffectType.JUMP) && livingEntity.getPotionEffect(PotionEffectType.JUMP).getAmplifier() < 0)
                    livingEntity.removePotionEffect(PotionEffectType.JUMP);
            } else
                livingEntity.addPotionEffect(
                        new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -6, false, false), true);
        });
    }

    /**
     * 최종 이동속도를 반환한다.
     *
     * @return 최종 이동속도
     */
    private double getFinalSpeed() {
        if (!canMove() || !combatEntity.canMove())
            return 0;

        double speed = speedStatus.getValue();

        LivingEntity livingEntity = combatEntity.getEntity();
        if (!(livingEntity instanceof Player))
            return speed;

        if (((Player) livingEntity).isSprinting()) {
            speed *= 0.88;
            if (!livingEntity.isOnGround())
                speed *= speed / speedStatus.getBaseValue();
        }

        return speed;
    }

    /**
     * 엔티티가 움직일 수 있는 기본 조건을 확인한다.
     *
     * @return 이동 가능 여부
     */
    private boolean canMove() {
        return !(combatEntity instanceof Damageable)
                || !((Damageable) combatEntity).getStatusEffectModule().hasRestriction(CombatRestriction.DEFAULT_MOVE);
    }

    /**
     * 엔티티가 점프할 수 있는 기본 조건을 확인한다.
     *
     * @return 점프 가능 여부
     */
    private boolean canJump() {
        return !(combatEntity instanceof Damageable)
                || !((Damageable) combatEntity).getStatusEffectModule().hasRestriction(CombatRestriction.JUMP);
    }

    /**
     * 엔티티를 지정한 속도로 밀어낸다. (이동기).
     *
     * @param velocity 속도
     * @param isReset  초기화 여부. {@code true}로 지정 시 기존 속도 초기화.
     */
    public void push(@NonNull Vector velocity, boolean isReset) {
        if (!(combatEntity instanceof Damageable) || !isKnockbacked()
                && !((Damageable) combatEntity).getStatusEffectModule().hasRestriction(CombatRestriction.PUSH))
            combatEntity.getEntity().setVelocity(isReset ? velocity : combatEntity.getEntity().getVelocity().add(velocity));
    }

    /**
     * 엔티티를 지정한 속도로 밀어낸다. (이동기).
     *
     * @param velocity 속도
     */
    public void push(@NonNull Vector velocity) {
        push(velocity, false);
    }


    /**
     * 엔티티를 지정한 속도로 강제로 밀쳐낸다. (넉백 효과).
     *
     * <p>또한 잠시동안 이동기({@link MoveModule#push(Vector, boolean)})의 사용을 제한한다.</p>
     *
     * @param velocity 속도
     * @param isReset  초기화 여부. {@code true}로 지정 시 기존 속도 초기화.
     * @see MoveModule#push(Vector, boolean)
     */
    public void knockback(@NonNull Vector velocity, boolean isReset) {
        knockbackTimestamp = Timestamp.now().plus(Timespan.ofTicks(3));

        Vector finalVelocity = velocity.multiply(Math.max(0, 2 - resistanceStatus.getValue()));
        combatEntity.getEntity().setVelocity(isReset ? finalVelocity : combatEntity.getEntity().getVelocity().add(finalVelocity));
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

    /**
     * 엔티티를 지정한 위치로 순간이동 시킨다.
     *
     * @param location 이동할 위치
     */
    public void teleport(@NonNull Location location) {
        if (!(combatEntity instanceof Damageable) || !((Damageable) combatEntity).getStatusEffectModule().hasRestriction(CombatRestriction.TELEPORT))
            EntityUtil.teleport(combatEntity.getEntity(), location);
    }
}
