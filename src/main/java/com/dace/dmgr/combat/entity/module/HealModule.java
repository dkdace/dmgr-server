package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Projectile;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 치유를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Healable}을 상속받는 클래스여야 하며,
 * 엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see Healable
 */
@Getter
public final class HealModule extends DamageModule {
    /**
     * 치유 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity      대상 엔티티
     * @param isUltProvider     엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부
     * @param isShowHealthBar   생명력 홀로그램 표시 여부
     * @param maxHealth         최대 체력
     * @param defenseMultiplier 방어력 배수 기본값
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public HealModule(@NonNull Healable combatEntity, boolean isUltProvider, boolean isShowHealthBar, int maxHealth, double defenseMultiplier) {
        super(combatEntity, isUltProvider, isShowHealthBar, maxHealth, defenseMultiplier);
    }

    /**
     * 치유 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity    대상 엔티티
     * @param isUltProvider   엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부
     * @param isShowHealthBar 생명력 홀로그램 표시 여부
     * @param maxHealth       최대 체력
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public HealModule(@NonNull Healable combatEntity, boolean isUltProvider, boolean isShowHealthBar, int maxHealth) {
        super(combatEntity, isUltProvider, isShowHealthBar, maxHealth);
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param provider 제공자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     * @return 치유 여부. 치유를 받았으면 {@code true} 반환
     */
    public boolean heal(@Nullable Healer provider, int amount, boolean isUlt) {
        if (getHealth() == getMaxHealth())
            return false;
        if (combatEntity.getStatusEffectModule().hasStatusEffectType(StatusEffectType.HEAL_BLOCK))
            return false;

        int finalAmount = amount;
        if (getHealth() + finalAmount > getMaxHealth())
            finalAmount = getMaxHealth() - getHealth();

        if (provider != null)
            provider.onGiveHeal((Healable) combatEntity, finalAmount, isUlt);
        ((Healable) combatEntity).onTakeHeal(provider, finalAmount, isUlt);

        setHealth(getHealth() + finalAmount);

        return true;
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param projectile 제공자가 발사한 투사체
     * @param amount     치유량
     * @param isUlt      궁극기 충전 여부
     * @return 치유 여부. 치유를 받았으면 {@code true} 반환
     */
    public boolean heal(@NonNull Projectile projectile, int amount, boolean isUlt) {
        CombatEntity provider = projectile.getShooter();
        if (provider instanceof Healer)
            return heal((Healer) provider, amount, isUlt);

        return false;
    }
}
