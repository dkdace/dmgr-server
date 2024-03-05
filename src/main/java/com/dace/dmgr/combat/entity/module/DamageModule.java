package com.dace.dmgr.combat.entity.module;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.AbilityStatus;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

/**
 * 피해를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Damageable}을 상속받는 클래스여야 하며,
 * 엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see Damageable
 */
@Getter
public class DamageModule {
    /** 기본값 */
    public static final double DEFAULT_VALUE = 1;
    /** 엔티티 객체 */
    @NonNull
    protected final Damageable combatEntity;
    /** 엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부. */
    protected final boolean isUltProvider;
    /** 방어력 배수 값 */
    @NonNull
    private final AbilityStatus defenseMultiplierStatus;
    /** 최대 체력 */
    protected int maxHealth;

    /**
     * 피해 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity      대상 엔티티
     * @param isUltProvider     엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부
     * @param maxHealth         최대 체력
     * @param defenseMultiplier 방어력 배수 기본값
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public DamageModule(@NonNull Damageable combatEntity, boolean isUltProvider, int maxHealth, double defenseMultiplier) {
        if (!(combatEntity.getEntity() instanceof LivingEntity))
            throw new IllegalArgumentException("'combatEntity'의 엔티티가 LivingEntity를 상속받지 않음");

        this.combatEntity = combatEntity;
        this.defenseMultiplierStatus = new AbilityStatus(defenseMultiplier);
        this.isUltProvider = isUltProvider;
        this.maxHealth = maxHealth;

        setMaxHealth(getMaxHealth());
        setHealth(getMaxHealth());
    }

    /**
     * 피해 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity  대상 엔티티
     * @param isUltProvider 엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부
     * @param maxHealth     최대 체력
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public DamageModule(@NonNull Damageable combatEntity, boolean isUltProvider, int maxHealth) {
        this(combatEntity, isUltProvider, maxHealth, DEFAULT_VALUE);
    }

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final int getHealth() {
        return (int) (Math.round(((LivingEntity) combatEntity.getEntity()).getHealth() * 50 * 100) / 100);
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final void setHealth(int health) {
        ((LivingEntity) combatEntity.getEntity()).setHealth(Math.min(Math.max(0, health), getMaxHealth()) / 50.0);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final void setMaxHealth(int health) {
        maxHealth = health;
        ((LivingEntity) combatEntity.getEntity()).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
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
     * 엔티티의 피해 로직을 처리한다.
     *
     * @param attacker          공격자
     * @param damage            피해량
     * @param damageMultiplier  공격력 배수
     * @param defenseMultiplier 방어력 배수
     * @param damageType        피해 타입
     * @param isCrit            치명타 여부
     * @param isUlt             궁극기 충전 여부
     */
    private void handleDamage(Attacker attacker, int damage, double damageMultiplier, double defenseMultiplier,
                              @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        if (combatEntity.getEntity().isDead() || !combatEntity.canTakeDamage())
            return;

        if (isCrit)
            damage *= 2;

        int finalDamage = (int) (damage * (1 + damageMultiplier - defenseMultiplier));
        if (getHealth() - finalDamage < 0)
            finalDamage = getHealth();
        int reducedDamage = ((int) (damage * damageMultiplier)) - finalDamage;
        if (getHealth() - reducedDamage < 0)
            reducedDamage = getHealth();

        if (attacker != null)
            attacker.onAttack(combatEntity, finalDamage, damageType, isCrit, isUlt);
        combatEntity.onDamage(attacker, finalDamage, reducedDamage, damageType, isCrit, isUlt);
        playHitEffect();

        if (getHealth() > finalDamage)
            setHealth(getHealth() - finalDamage);
        else {
            if (attacker != null)
                attacker.onKill(combatEntity);
            combatEntity.onDeath(attacker);
        }
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
    public final void damage(Attacker attacker, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        double damageMultiplier = attacker == null || damageType == DamageType.AREA ?
                1 : attacker.getAttackModule().getDamageMultiplierStatus().getValue();
        double defenseMultiplier = attacker == null ?
                1 : defenseMultiplierStatus.getValue();

        handleDamage(attacker, damage, damageMultiplier, defenseMultiplier, damageType, isCrit, isUlt);
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
    public final void damage(@NonNull Projectile projectile, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        CombatEntity attacker = projectile.getShooter();
        if (attacker instanceof Attacker) {
            double damageMultiplier = projectile.getDamageIncrement();
            double defenseMultiplier = defenseMultiplierStatus.getValue();

            handleDamage((Attacker) attacker, damage, damageMultiplier, defenseMultiplier, damageType, isCrit, isUlt);
        }
    }

    /**
     * 엔티티의 피격 효과를 재생한다.
     */
    private void playHitEffect() {
        if (CooldownUtil.getCooldown(this, Cooldown.DAMAGE_ANIMATION) == 0) {
            CooldownUtil.setCooldown(this, Cooldown.DAMAGE_ANIMATION);
            WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

            packet.setEntityID(combatEntity.getEntity().getEntityId());
            packet.setEntityStatus((byte) 2);

            packet.broadcastPacket();
        }
    }
}
