package com.dace.dmgr.combat.entity.module;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 피해를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * <p>엔티티가 {@link Damageable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Damageable
 */
@AllArgsConstructor
public class DamageModule implements CombatEntityModule {
    /** 엔티티 객체 */
    protected final Damageable combatEntity;
    /** 엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부. */
    @Getter
    protected final boolean isUltProvider;

    /** 최대 체력 */
    @Getter
    protected int maxHealth;

    @Override
    @MustBeInvokedByOverriders
    public void onInit() {
        setMaxHealth(getMaxHealth());
        setHealth(getMaxHealth());
    }

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final int getHealth() {
        return (int) (Math.round(combatEntity.getEntity().getHealth() * 50 * 100) / 100);
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final void setHealth(int health) {
        combatEntity.getEntity().setHealth(Math.min(Math.max(0, health), getMaxHealth()) / 50.0);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final void setMaxHealth(int health) {
        maxHealth = health;
        combatEntity.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
    }

    /**
     * 엔티티가 치명상인 지 확인한다.
     *
     * @return 체력이 25% 이하이면 {@code true} 반환
     */
    public final boolean isLowHealth() {
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
    public final void damage(Attacker attacker, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        if (combatEntity.getEntity().isDead() || !combatEntity.canTakeDamage())
            return;

        if (isCrit)
            damage *= 2;
        double damageMultiplier = attacker.getAbilityStatusManager().getAbilityStatus(Ability.DAMAGE).getValue();
        double defenseMultiplier = combatEntity.getAbilityStatusManager().getAbilityStatus(Ability.DEFENSE).getValue();
        int finalDamage = (int) (damage * (1 + damageMultiplier - defenseMultiplier));
        int reducedDamage = ((int) (damage * (1 + damageMultiplier))) - finalDamage;

        attacker.onAttack(combatEntity, finalDamage, damageType, isCrit, isUlt);
        combatEntity.onDamage(attacker, finalDamage, reducedDamage, damageType, isCrit, isUlt);
        playHitEffect();

        if (getHealth() - finalDamage > 0)
            setHealth(getHealth() - finalDamage);
        else {
            attacker.onKill(combatEntity);
            combatEntity.onDeath(attacker);
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
    public final void damage(Projectile projectile, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        CombatEntity attacker = projectile.getShooter();
        if (attacker instanceof Attacker)
            damage((Attacker) attacker, damage, damageType, isCrit, isUlt);
    }

    /**
     * 엔티티의 피격 효과를 재생한다.
     */
    private void playHitEffect() {
        if (CooldownManager.getCooldown(this, Cooldown.DAMAGE_ANIMATION) == 0) {
            CooldownManager.setCooldown(this, Cooldown.DAMAGE_ANIMATION);
            WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

            packet.setEntityID(combatEntity.getEntity().getEntityId());
            packet.setEntityStatus((byte) 2);

            packet.broadcastPacket();
        }
    }
}
