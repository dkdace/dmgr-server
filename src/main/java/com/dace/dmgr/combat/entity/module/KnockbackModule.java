package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.CooldownUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * 엔티티의 넉백 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Damageable}을 상속받는 클래스여야 하며,
 * 엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see Damageable
 */
@Getter
public final class KnockbackModule {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "Knockback";
    /** 넉백 저항 기본값 */
    private static final double DEFAULT_VALUE = 1;
    /** 엔티티 객체 */
    @NonNull
    private final Damageable combatEntity;
    /** 넉백 저항 값 */
    @NonNull
    private final AbilityStatus resistanceStatus;

    /**
     * 넉백 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param resistance   넉백 저항 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를
     *                                  상속받지 않으면 발생
     */
    public KnockbackModule(@NonNull Damageable combatEntity, double resistance) {
        if (resistance < 0)
            throw new IllegalArgumentException("'resistance'가 0 이상이어야 함");
        if (!(combatEntity.getEntity() instanceof LivingEntity))
            throw new IllegalArgumentException("'combatEntity'의 엔티티가 LivingEntity를 상속받지 않음");

        this.combatEntity = combatEntity;
        this.resistanceStatus = new AbilityStatus(resistance);
    }

    /**
     * 넉백 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public KnockbackModule(@NonNull Damageable combatEntity) {
        this(combatEntity, DEFAULT_VALUE);
    }

    /**
     * 엔티티를 지정한 속도로 강제로 밀쳐낸다. (넉백 효과).
     *
     * @param velocity 속도
     * @param isReset  초기화 여부. {@code true}로 지정 시 기존 속도 초기화.
     * @see MoveModule#push(Vector, boolean)
     */
    public void knockback(@NonNull Vector velocity, boolean isReset) {
        if (combatEntity.getStatusEffectModule().hasAnyRestriction(CombatRestrictions.KNOCKBACKED))
            return;

        CooldownUtil.setCooldown(combatEntity, COOLDOWN_ID, 3);
        Vector finalVelocity = velocity.multiply(Math.max(0, 2 - resistanceStatus.getValue()));
        combatEntity.getEntity().setVelocity(isReset ? finalVelocity : combatEntity.getEntity().getVelocity().add(finalVelocity));
    }

    /**
     * 엔티티를 지정한 속도로 강제로 밀쳐낸다. (넉백 효과).
     *
     * @param velocity 속도
     * @see MoveModule#push(Vector)
     */
    public void knockback(@NonNull Vector velocity) {
        knockback(velocity, false);
    }

    /**
     * 엔티티가 넉백 효과를 받은 상태인 지 확인한다.
     *
     * @return 넉백 효과 상태 여부
     */
    public boolean isKnockbacked() {
        return CooldownUtil.getCooldown(combatEntity, COOLDOWN_ID) > 0;
    }
}
