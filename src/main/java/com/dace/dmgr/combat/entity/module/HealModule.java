package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.interaction.Projectile;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 치유를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * <p>엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see Healable
 */
@Getter
public final class HealModule extends DamageModule {
    /** 회복량 배수 기본값 */
    private static final double DEFAULT_VALUE = 1;
    /** 회복량 배수 값 */
    @NonNull
    private final AbilityStatus healMultiplierStatus;

    /**
     * 회복 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param maxHealth    최대 체력. 1 이상의 값
     * @param hasHealthBar 생명력 홀로그램 표시 여부
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public HealModule(@NonNull Healable combatEntity, int maxHealth, boolean hasHealthBar) {
        super(combatEntity, maxHealth, hasHealthBar);
        this.healMultiplierStatus = new AbilityStatus(DEFAULT_VALUE);
    }

    /**
     * 엔티티의 치유 로직을 처리한다.
     *
     * @param provider           제공자
     * @param amount             치유량
     * @param giveHealMultiplier 주는 치유량 배수
     * @param takeHealMultiplier 받는 치유량 배수
     * @param isUlt              궁극기 충전 여부
     * @return 치유 여부. 치유를 받았으면 {@code true} 반환
     */
    private boolean handleHeal(@Nullable Healer provider, double amount, double giveHealMultiplier, double takeHealMultiplier, boolean isUlt) {
        if (combatEntity.getEntity().isDead() || isFullHealth() || combatEntity.getStatusEffectModule().hasRestriction(CombatRestriction.HEALED))
            return false;
        if (amount == 0)
            return true;

        double finalAmount = Math.max(0, amount * (giveHealMultiplier + takeHealMultiplier - 1));
        if (getHealth() + finalAmount > getMaxHealth())
            finalAmount = getMaxHealth() - getHealth();

        if (provider != null)
            provider.onGiveHeal((Healable) combatEntity, finalAmount, isUlt);

        ((Healable) combatEntity).onTakeHeal(provider, finalAmount);

        setHealth(getHealth() + finalAmount);

        return true;
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param provider 제공자
     * @param amount   치유량. 0 이상의 값
     * @param isUlt    궁극기 충전 여부
     * @return 치유 여부. 치유를 받았으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public boolean heal(@Nullable Healer provider, double amount, boolean isUlt) {
        Validate.isTrue(amount >= 0, "amount >= 0 (%f)", amount);

        double giveHealMultiplier = provider == null ? 1 : provider.getHealerModule().getHealMultiplierStatus().getValue();
        double takeHealMultiplier = healMultiplierStatus.getValue();

        return handleHeal(provider, amount, giveHealMultiplier, takeHealMultiplier, isUlt);
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param projectile 제공자가 발사한 투사체
     * @param amount     치유량. 0 이상의 값
     * @param isUlt      궁극기 충전 여부
     * @return 치유 여부. 치유를 받았으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public boolean heal(@NonNull Projectile<? extends Healable> projectile, double amount, boolean isUlt) {
        Validate.isTrue(amount >= 0, "amount >= 0 (%f)", amount);

        CombatEntity provider = projectile.getShooter();
        if (provider instanceof Healer) {
            double giveHealMultiplier = projectile.getHealIncrement();
            double takeHealMultiplier = healMultiplierStatus.getValue();

            return handleHeal((Healer) provider, amount, giveHealMultiplier, takeHealMultiplier, isUlt);
        }

        return false;
    }
}
