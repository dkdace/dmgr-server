package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.entity.Healable;

/**
 * 치유를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * <p>엔티티가 {@link Healable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Healable
 */
public final class HealModule extends DamageModule {
    public HealModule(Healable combatEntity, boolean isUltProvider, int maxHealth) {
        super(combatEntity, isUltProvider, maxHealth);
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param provider 제공자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     */
    public void heal(Healer provider, int amount, boolean isUlt) {
        if (getHealth() == getMaxHealth())
            return;

        provider.onGiveHeal((Healable) combatEntity, amount, isUlt);
        ((Healable) combatEntity).onTakeHeal(provider, amount, isUlt);

        setHealth(getHealth() + amount);
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param projectile 제공자가 발사한 투사체
     * @param amount     치유량
     * @param isUlt      궁극기 충전 여부
     */
    public void heal(Projectile projectile, int amount, boolean isUlt) {
        CombatEntity provider = projectile.getShooter();
        if (provider instanceof Healer)
            heal((Healer) provider, amount, isUlt);
    }
}
