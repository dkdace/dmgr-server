package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Particle;
import org.jetbrains.annotations.Nullable;

/**
 * 치유를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * @see Healable
 */
public final class HealModule extends CombatEntityModule<Healable> {
    /** 회복 입자 효과 */
    private static final PlayableEffect.Function<Double> HEAL_PARTICLE = amount ->
            ParticleEffect.Normal.builder(Particle.HEART).count((int) (amount / 100.0)).horizontalSpread(0.3).verticalSpread(0.1).build();

    /**
     * 회복 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public HealModule(@NonNull Healable combatEntity) {
        super(combatEntity);
    }

    @Override
    protected double getBaseValue() {
        return 1;
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
        DamageModule damageModule = combatEntity.getDamageModule();

        if (combatEntity.getEntity().isDead() || damageModule.isFullHealth()
                || combatEntity.getStatusEffectModule().hasRestriction(CombatRestriction.HEALED))
            return false;
        if (amount == 0)
            return true;

        double finalAmount = Math.max(0, amount * (giveHealMultiplier + takeHealMultiplier - 1));
        if (damageModule.getHealth() + finalAmount > damageModule.getMaxHealth())
            finalAmount = damageModule.getMaxHealth() - damageModule.getHealth();

        damageModule.setHealth(damageModule.getHealth() + finalAmount);

        if (provider != null)
            provider.onGiveHeal(combatEntity, finalAmount, isUlt);

        combatEntity.onTakeHeal(provider, finalAmount);

        if (finalAmount >= 100 || finalAmount / 100.0 > Math.random())
            HEAL_PARTICLE.apply(finalAmount).play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.3, 0));

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

        double giveHealMultiplier = provider == null ? 1 : provider.getHealerModule().getValue();
        double takeHealMultiplier = getValue();

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
            double takeHealMultiplier = getValue();

            return handleHeal((Healer) provider, amount, giveHealMultiplier, takeHealMultiplier, isUlt);
        }

        return false;
    }
}
