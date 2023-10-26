package com.dace.dmgr.combat.entity.damageable;

import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Healer;

/**
 * 다른 엔티티로부터 치유를 받을 수 있는 엔티티의 인터페이스.
 */
public interface Healable extends Damageable {
    /**
     * 엔티티를 치유한다.
     *
     * @param provider 제공자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     */
    default void heal(Healer provider, int amount, boolean isUlt) {
        if (getHealth() == getMaxHealth())
            return;
        if (!canTakeHeal())
            return;

        provider.onGiveHeal(this, amount, isUlt);
        onTakeHeal(this, amount, isUlt);

        setHealth(getHealth() + amount);
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param projectile 제공자가 발사한 투사체
     * @param amount     치유량
     * @param isUlt      궁극기 충전 여부
     */
    default void heal(Projectile projectile, int amount, boolean isUlt) {
        CombatEntity provider = projectile.getShooter();
        if (!(provider instanceof Healer))
            return;
        if (getHealth() == getMaxHealth())
            return;
        if (!canTakeHeal())
            return;

        ((Healer) provider).onGiveHeal(this, amount, isUlt);
        onTakeHeal(this, amount, isUlt);

        setHealth(getHealth() + amount);
    }

    /**
     * 엔티티가 치유를 받을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 치유 가능 여부
     */
    default boolean canTakeHeal() {
        return true;
    }

    /**
     * 엔티티가 치유를 받았을 때 실행될 작업.
     *
     * @param provider 제공자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     * @see Healer#onGiveHeal(Healable, int, boolean)
     */
    void onTakeHeal(CombatEntity provider, int amount, boolean isUlt);
}
