package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.CooldownUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.util.Vector;

/**
 * 엔티티의 넉백 모듈 클래스.
 */
@Getter
public final class KnockbackModule {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "Knockback";
    /** 넉백 저항 기본값 */
    private static final double DEFAULT_VALUE = 1;
    /** 엔티티 객체 */
    @NonNull
    private final CombatEntity combatEntity;
    /** 넉백 저항 값 */
    @NonNull
    private final AbilityStatus resistanceStatus;

    /**
     * 넉백 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param resistance   넉백 저항 기본값
     */
    public KnockbackModule(@NonNull CombatEntity combatEntity, double resistance) {
        this.combatEntity = combatEntity;
        this.resistanceStatus = new AbilityStatus(resistance);
    }

    /**
     * 넉백 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public KnockbackModule(@NonNull CombatEntity combatEntity) {
        this(combatEntity, DEFAULT_VALUE);
    }

    /**
     * 엔티티를 지정한 속도로 강제로 밀쳐낸다. (넉백 효과).
     *
     * @param velocity 속도
     * @param isReset  초기화 여부. {@code true}로 지정 시 기존 속도 초기화.
     * @see CombatEntity#push(Vector, boolean)
     */
    public void knockback(@NonNull Vector velocity, boolean isReset) {
        CooldownUtil.setCooldown(combatEntity, COOLDOWN_ID, 3);
        Vector finalVelocity = velocity.multiply(Math.max(0, 2 - resistanceStatus.getValue()));
        combatEntity.getEntity().setVelocity(isReset ? finalVelocity : combatEntity.getEntity().getVelocity().add(finalVelocity));
    }

    /**
     * 엔티티를 지정한 속도로 강제로 밀쳐낸다. (넉백 효과).
     *
     * @param velocity 속도
     * @see CombatEntity#push(Vector)
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
