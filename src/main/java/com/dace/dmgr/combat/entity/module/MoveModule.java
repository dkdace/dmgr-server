package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.util.EntityUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
public final class MoveModule extends CombatEntityModule<Movable> {
    /** 이동속도 기본값 */
    @Getter
    private final double baseValue;
    /** 점프 강도 */
    @Getter
    @Setter
    private int jumpStrength = 0;

    /**
     * 이동 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param speed        이동속도 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public MoveModule(@NonNull Movable combatEntity, double speed) {
        super(combatEntity);
        Validate.isTrue(speed >= 0, "speed >= 0 (%f)", speed);
        Validate.isTrue(combatEntity.getEntity() instanceof LivingEntity, "combatEntity.getEntity()가 LivingEntity를 상속받지 않음");

        this.baseValue = speed;

        combatEntity.addOnTick(i -> {
            double finalSpeed = Math.max(0, getFinalSpeed());

            LivingEntity livingEntity = combatEntity.getEntity();
            livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(finalSpeed);

            if (livingEntity instanceof Player)
                ((Player) livingEntity).setFlySpeed((float) (finalSpeed * 0.35));

            if (canJump() && combatEntity.canJump()) {
                if (jumpStrength > 0)
                    livingEntity.addPotionEffect(
                            new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, jumpStrength - 1, false, false), true);
                else
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

        double speed = getValue();

        LivingEntity livingEntity = combatEntity.getEntity();
        if (!(livingEntity instanceof Player))
            return speed;

        if (((Player) livingEntity).isSprinting()) {
            speed *= 0.88;
            if (!livingEntity.isOnGround())
                speed *= speed / baseValue;
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
     * @param isReset  초기화 여부. {@code true}로 지정 시 기존 속도 초기화
     */
    public void push(@NonNull Vector velocity, boolean isReset) {
        if (!(combatEntity instanceof Damageable) || !combatEntity.getKnockbackModule().isKnockbacked()
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
     * 엔티티를 지정한 위치로 순간이동 시킨다.
     *
     * @param location 이동할 위치
     */
    public void teleport(@NonNull Location location) {
        if (!(combatEntity instanceof Damageable) || !((Damageable) combatEntity).getStatusEffectModule().hasRestriction(CombatRestriction.TELEPORT))
            EntityUtil.teleport(combatEntity.getEntity(), location);
    }
}
