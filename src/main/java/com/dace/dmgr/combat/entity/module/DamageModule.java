package com.dace.dmgr.combat.entity.module;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.ReflectionUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.WeakHashMap;

/**
 * 피해를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * <p>엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see Damageable
 */
public class DamageModule {
    /** 방어력 배수 기본값 */
    private static final double DEFAULT_VALUE = 1;
    /** 치명타 배수 기본값 */
    private static final double DEFAULT_CRIT_MULTIPLIER = 2;

    /** 엔티티 인스턴스 */
    protected final Damageable combatEntity;
    /** 방어력 배수 값 */
    @NonNull
    @Getter
    private final AbilityStatus defenseMultiplierStatus;
    /** 생명력 홀로그램 표시 여부 */
    private final boolean hasHealthBar;
    /** 보호막 목록 */
    private final ArrayList<Shield> shields = new ArrayList<>();
    /** 플레이어별 생명력 홀로그램 표시 타임스탬프 목록 (공격자 : 종료 시점) */
    private final WeakHashMap<CombatUser, Timestamp> showHealthHologramTimestampMap = new WeakHashMap<>();

    /** 최대 체력 */
    @Getter
    private int maxHealth;
    /** 피격 시 애니메이션 타임스탬프 */
    private Timestamp damageAnimationTimestamp = Timestamp.now();

    /**
     * 피해 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param maxHealth    최대 체력. 1 이상의 값
     * @param hasHealthBar 생명력 홀로그램 표시 여부
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public DamageModule(@NonNull Damageable combatEntity, int maxHealth, boolean hasHealthBar) {
        Validate.isTrue(maxHealth >= 1, "maxHealth >= 1 (%d)", maxHealth);
        Validate.isTrue(combatEntity.getEntity() instanceof LivingEntity, "combatEntity.getEntity()가 LivingEntity를 상속받지 않음");

        this.combatEntity = combatEntity;
        this.defenseMultiplierStatus = new AbilityStatus(DEFAULT_VALUE);
        this.maxHealth = maxHealth;
        this.hasHealthBar = hasHealthBar;

        setMaxHealth(getMaxHealth());
        setHealth(getMaxHealth());
        ((LivingEntity) combatEntity.getEntity()).setRemoveWhenFarAway(false);

        combatEntity.addOnRemove(this::clearShields);
        if (hasHealthBar)
            combatEntity.addTask(new DelayTask(this::createHealthBar, 5));
    }

    /**
     * 생명력 홀로그램을 생성한다.
     */
    private void createHealthBar() {
        TextHologram textHologram = new TextHologram(combatEntity.getEntity(), target -> {
            CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(target));
            if (targetCombatUser == null)
                return true;

            if (combatEntity.isEnemy(targetCombatUser)) {
                Timestamp expiration = showHealthHologramTimestampMap.get(targetCombatUser);
                return expiration != null && expiration.isAfter(Timestamp.now())
                        && LocationUtil.canPass(target.getEyeLocation(), combatEntity.getCenterLocation());
            } else
                return targetCombatUser != combatEntity;
        }, 0);

        combatEntity.addOnTick(i -> {
            ChatColor color;
            if (combatEntity.getStatusEffectModule().hasType(StatusEffectType.HEAL_BLOCK))
                color = ChatColor.DARK_PURPLE;
            else if (isLowHealth())
                color = ChatColor.RED;
            else if (isHalfHealth())
                color = ChatColor.YELLOW;
            else
                color = ChatColor.GREEN;

            textHologram.setContent(StringFormUtil.getProgressBar(getHealth(), getMaxHealth(), color));
        });
        combatEntity.addOnRemove(textHologram::remove);
    }

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 1000)
     */
    public final double getHealth() {
        return ((LivingEntity) combatEntity.getEntity()).getHealth() * 50.0;
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 1000)
     */
    public final void setHealth(double health) {
        ((LivingEntity) combatEntity.getEntity()).setHealth(Math.min(Math.max(0, health), getMaxHealth()) / 50.0);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param maxHealth 실제 체력×50 (체력 1줄 기준 1000). 1 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final void setMaxHealth(int maxHealth) {
        Validate.isTrue(maxHealth >= 1, "maxHealth >= 1", maxHealth);

        this.maxHealth = maxHealth;
        if (this.maxHealth < getHealth())
            setHealth(maxHealth);

        ((LivingEntity) combatEntity.getEntity()).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth / 50.0);
    }

    /**
     * 엔티티의 체력이 최대치인지 확인한다.
     *
     * @return 체력이 최대치이면 {@code true} 반환
     */
    public final boolean isFullHealth() {
        return getHealth() == getMaxHealth();
    }

    /**
     * 엔티티의 체력이 절반 이하인지 확인한다.
     *
     * @return 체력이 50% 이하면 {@code true} 반환
     */
    public final boolean isHalfHealth() {
        return getHealth() <= getMaxHealth() / 2.0;
    }

    /**
     * 엔티티가 치명상인지 확인한다.
     *
     * @return 체력이 25% 이하면 {@code true} 반환
     */
    public final boolean isLowHealth() {
        return getHealth() <= getMaxHealth() / 4.0;
    }

    /**
     * 엔티티의 보호막을 생성하여 반환한다.
     *
     * @param health 보호막 체력 (실제 보호막×50 (체력 1줄 기준 1000))
     * @return 보호막 인스턴스
     */
    @NonNull
    public final Shield createShield(int health) {
        return new Shield(health);
    }

    /**
     * 엔티티의 전체 보호막 체력을 반환한다.
     *
     * @return 전체 보호막 체력 (실제 보호막×50 (체력 1줄 기준 1000))
     * @see Shield#getHealth()
     */
    public final double getTotalShield() {
        return shields.stream().mapToDouble(Shield::getHealth).sum();
    }

    /**
     * 엔티티의 전체 보호막을 초기화한다.
     */
    public final void clearShields() {
        new ArrayList<>(shields).forEach(shield -> shield.setHealth(0));
    }

    /**
     * 엔티티의 피해 로직을 처리한다.
     *
     * @param attacker          공격자
     * @param damage            피해량
     * @param damageMultiplier  공격력 배수
     * @param defenseMultiplier 방어력 배수
     * @param damageType        피해 타입
     * @param location          맞은 위치
     * @param critMultiplier    치명타 배수. 1로 설정 시 치명타 미적용
     * @param isUlt             궁극기 충전 여부
     * @return 피해 여부. 피해를 입었으면 {@code true} 반환
     */
    private boolean handleDamage(@Nullable Attacker attacker, double damage, double damageMultiplier, double defenseMultiplier,
                                 @NonNull DamageType damageType, @Nullable Location location, double critMultiplier, boolean isUlt) {
        if (combatEntity.getEntity().isDead() || combatEntity.getStatusEffectModule().hasRestriction(CombatRestriction.DAMAGED))
            return false;
        if (damage == 0)
            return true;

        if (damageType == DamageType.IGNORE_DEFENSE || damageType == DamageType.FIXED) {
            defenseMultiplier = 1;
            if (damageType == DamageType.FIXED)
                damageMultiplier = 1;
        }

        damage *= critMultiplier;
        double finalDamage = getFinalDamage(damage, damageMultiplier, defenseMultiplier, damageType);
        double safeFinalDamage = Math.min(finalDamage, getHealth());
        boolean isKilled = getHealth() <= finalDamage;

        if (!isKilled)
            setHealth(getHealth() - finalDamage);

        if (attacker != null)
            attacker.onAttack(combatEntity, safeFinalDamage, critMultiplier != 1, isUlt);

        double reducedDamage = Math.max(0, damage * damageMultiplier - finalDamage);
        combatEntity.onDamage(attacker, safeFinalDamage, reducedDamage, location, critMultiplier != 1);

        playHitEffect();
        if (hasHealthBar && attacker instanceof CombatUser)
            showHealthHologramTimestampMap.put((CombatUser) attacker, Timestamp.now().plus(Timespan.ofSeconds(1)));

        if (isKilled) {
            if (attacker != null)
                attacker.onKill(combatEntity);
            combatEntity.onDeath(attacker);
        }

        return true;
    }

    /**
     * 최종 피해량을 계산하여 반환한다.
     *
     * @param damage            피해량
     * @param damageMultiplier  공격력 배수
     * @param defenseMultiplier 방어력 배수
     * @param damageType        피해 타입
     * @return 최종 피해량
     */
    private double getFinalDamage(double damage, double damageMultiplier, double defenseMultiplier, @NonNull DamageType damageType) {
        double finalDamage = Math.max(0, damage * (1 + damageMultiplier - defenseMultiplier));

        if (getTotalShield() > 0 && damageType != DamageType.IGNORE_DEFENSE) {
            ListIterator<Shield> iterator = shields.listIterator(shields.size());
            while (iterator.hasPrevious()) {
                Shield shield = iterator.previous();
                if (shield.getHealth() > finalDamage) {
                    shield.setHealth(shield.getHealth() - finalDamage);
                    finalDamage = 0;
                    break;
                }

                finalDamage -= shield.getHealth();
                shield.setHealth(0);
            }
        }

        return finalDamage;
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param attacker       공격자
     * @param damage         피해량. 0 이상의 값
     * @param damageType     피해 타입
     * @param location       맞은 위치
     * @param critMultiplier 치명타 배수. 1로 설정 시 치명타 미적용. 1 이상의 값
     * @param isUlt          궁극기 충전 여부
     * @return 피해 여부. 피해를 입었으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final boolean damage(@Nullable Attacker attacker, double damage, @NonNull DamageType damageType, @Nullable Location location,
                                double critMultiplier, boolean isUlt) {
        Validate.isTrue(damage >= 0, "damage >= 0 (%f)", damage);
        Validate.isTrue(critMultiplier >= 1, "critMultiplier >= 1 (%f)", critMultiplier);

        double damageMultiplier = attacker == null ? 1 : attacker.getAttackModule().getDamageMultiplierStatus().getValue();
        double defenseMultiplier = defenseMultiplierStatus.getValue();

        return handleDamage(attacker, damage, damageMultiplier, defenseMultiplier, damageType, location, critMultiplier, isUlt);
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param attacker   공격자
     * @param damage     피해량. 0 이상의 값
     * @param damageType 피해 타입
     * @param location   맞은 위치
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     * @return 피해 여부. 피해를 입었으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final boolean damage(@Nullable Attacker attacker, double damage, @NonNull DamageType damageType, @Nullable Location location, boolean isCrit,
                                boolean isUlt) {
        return damage(attacker, damage, damageType, location, isCrit ? DEFAULT_CRIT_MULTIPLIER : 1, isUlt);
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param projectile     공격자가 발사한 투사체
     * @param damage         피해량. 0 이상의 값
     * @param damageType     피해 타입
     * @param location       맞은 위치
     * @param critMultiplier 치명타 배수. 1로 설정 시 치명타 미적용. 1 이상의 값
     * @param isUlt          궁극기 충전 여부
     * @return 피해 여부. 피해를 입었으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final boolean damage(@NonNull Projectile<? extends Damageable> projectile, double damage, @NonNull DamageType damageType,
                                @Nullable Location location, double critMultiplier, boolean isUlt) {
        Validate.isTrue(damage >= 0, "damage >= 0 (%f)", damage);
        Validate.isTrue(critMultiplier >= 1, "critMultiplier >= 1 (%f)", critMultiplier);

        CombatEntity attacker = projectile.getShooter();
        if (attacker instanceof Attacker) {
            double damageMultiplier = projectile.getDamageIncrement();
            double defenseMultiplier = defenseMultiplierStatus.getValue();

            return handleDamage((Attacker) attacker, damage, damageMultiplier, defenseMultiplier, damageType, location, critMultiplier, isUlt);
        }

        return false;
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param projectile 공격자가 발사한 투사체
     * @param damage     피해량. 0 이상의 값
     * @param damageType 피해 타입
     * @param location   맞은 위치
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     * @return 피해 여부. 피해를 입었으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final boolean damage(@NonNull Projectile<? extends Damageable> projectile, double damage, @NonNull DamageType damageType,
                                @Nullable Location location, boolean isCrit, boolean isUlt) {
        return damage(projectile, damage, damageType, location, isCrit ? DEFAULT_CRIT_MULTIPLIER : 1, isUlt);
    }

    /**
     * 엔티티의 피격 효과를 재생한다.
     */
    private void playHitEffect() {
        if (damageAnimationTimestamp.isAfter(Timestamp.now()))
            return;

        damageAnimationTimestamp = Timestamp.now().plus(Timespan.ofTicks(6));

        WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

        packet.setEntityID(combatEntity.getEntity().getEntityId());
        packet.setEntityStatus((byte) 2);

        packet.broadcastPacket();
    }


    /**
     * 보호막을 나타내는 클래스.
     */
    @Getter
    public final class Shield {
        /** 남은 보호막 체력 */
        private double health;

        private Shield(int health) {
            setHealth(health);
        }

        /**
         * 보호막의 체력을 설정한다.
         *
         * @param health 실제 보호막×50 (체력 1줄 기준 1000)
         */
        public void setHealth(double health) {
            this.health = Math.max(0, health);

            if (health == 0)
                shields.remove(this);
            else if (!shields.contains(this))
                shields.add(this);

            if (combatEntity.getEntity() instanceof Player)
                displayShield();
        }

        /**
         * 플레이어에게 보호막 체력을 표시한다.
         */
        private void displayShield() {
            try {
                Method getHandleMethod = ReflectionUtil.getMethod(combatEntity.getEntity().getClass(), "getHandle");
                Object nmsPlayer = getHandleMethod.invoke(combatEntity.getEntity());
                Method setAbsorptionHeartsMethod = ReflectionUtil.getMethod(nmsPlayer.getClass(), "setAbsorptionHearts", Float.TYPE);

                setAbsorptionHeartsMethod.invoke(nmsPlayer, (float) (getTotalShield() / 50.0));
            } catch (Exception ex) {
                ConsoleLogger.severe("보호막을 표시할 수 없음", ex);
            }
        }
    }
}
