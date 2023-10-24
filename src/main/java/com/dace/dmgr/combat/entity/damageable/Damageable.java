package com.dace.dmgr.combat.entity.damageable;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import org.bukkit.attribute.Attribute;

/**
 * 생명력 수치를 조정하고 피해를 입을 수 있는 엔티티의 인터페이스.
 *
 * @see Healable
 */
public interface Damageable extends CombatEntity {
    @Override
    default void onInit() {
        setMaxHealth(getMaxHealth());
        setHealth(getMaxHealth());
        onInitDamageable();
    }

    /**
     * @see CombatEntity#onInit()
     */
    void onInitDamageable();

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    default int getHealth() {
        return (int) (Math.round(getEntity().getHealth() * 50 * 100) / 100);
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    default void setHealth(int health) {
        getEntity().setHealth(Math.min(Math.max(0, health), getMaxHealth()) / 50.0);
    }

    /**
     * 엔티티의 최대 체력을 반환한다.
     *
     * <p>오버라이딩하여 엔티티 생성 시의 최대 체력을 설정할 수 있다.</p>
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    default int getMaxHealth() {
        return (int) (getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 50);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    default void setMaxHealth(int health) {
        getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
    }

    /**
     * 엔티티가 치명상인 지 확인한다.
     *
     * @return 체력이 25% 이하이면 {@code true} 반환
     */
    default boolean isLowHealth() {
        return getHealth() <= getMaxHealth() / 4;
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param attacker   공격자
     * @param damage     피해량
     * @param damageType 피해 타입
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     */
    default void damage(Attacker attacker, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        if (getEntity().isDead())
            return;
        if (!canTakeDamage())
            return;

        double damageMultiplier = attacker.getAbilityStatusManager().getAbilityStatus(Ability.DAMAGE).getValue();
        double defenseMultiplier = getAbilityStatusManager().getAbilityStatus(Ability.DEFENSE).getValue();
        damage *= (int) (1 + damageMultiplier - defenseMultiplier);
        if (isCrit)
            damage *= 2;

        attacker.onAttack(this, damage, damageType, isCrit, isUlt);
        onDamage(attacker, damage, damageType, isCrit, isUlt);
        playHitEffect();

        if (getHealth() - damage > 0)
            setHealth(getHealth() - damage);
        else {
            if (canDie()) {
                attacker.onKill(this);
                onDeath(attacker);
            } else
                setHealth(1);
        }
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param projectile 공격자가 발사한 투사체
     * @param damage     피해량
     * @param damageType 피해 타입
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     */
    default void damage(Projectile projectile, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        CombatEntity attacker = projectile.getShooter();
        if (!(attacker instanceof Attacker))
            return;
        if (getEntity().isDead())
            return;
        if (!canTakeDamage())
            return;

        double damageMultiplier = projectile.getDamageIncrement();
        double defenseMultiplier = getAbilityStatusManager().getAbilityStatus(Ability.DEFENSE).getValue();
        damage *= (int) (1 + damageMultiplier - defenseMultiplier);
        if (isCrit)
            damage *= 2;

        ((Attacker) attacker).onAttack(this, damage, damageType, isCrit, isUlt);
        onDamage((Attacker) attacker, damage, damageType, isCrit, isUlt);
        playHitEffect();

        if (getHealth() - damage > 0)
            setHealth(getHealth() - damage);
        else {
            if (canDie()) {
                ((Attacker) attacker).onKill(this);
                onDeath((Attacker) attacker);
            } else
                setHealth(1);
        }
    }

    /**
     * 엔티티의 피격 효과를 재생한다.
     */
    default void playHitEffect() {
        if (CooldownManager.getCooldown(this, Cooldown.DAMAGE_ANIMATION) == 0) {
            CooldownManager.setCooldown(this, Cooldown.DAMAGE_ANIMATION);
            WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

            packet.setEntityID(getEntity().getEntityId());
            packet.setEntityStatus((byte) 2);

            packet.broadcastPacket();
        }
    }

    /**
     * 엔티티가 피해를 입었을 때 실행될 작업.
     *
     * @param attacker   공격자
     * @param damage     피해량
     * @param damageType 타입
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     * @see Attacker#onAttack(Damageable, int, DamageType, boolean, boolean)
     */
    void onDamage(Attacker attacker, int damage, DamageType damageType, boolean isCrit, boolean isUlt);

    /**
     * 엔티티가 피해를 받을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 피격 가능 여부
     */
    default boolean canTakeDamage() {
        return true;
    }

    /**
     * 엔티티가 죽었을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @see Attacker#onKill(CombatEntity)
     */
    void onDeath(Attacker attacker);

    /**
     * 엔티티가 죽을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 죽을 수 있으면 {@code true} 반환
     */
    default boolean canDie() {
        return true;
    }
}
