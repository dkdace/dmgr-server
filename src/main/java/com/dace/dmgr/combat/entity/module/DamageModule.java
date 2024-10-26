package com.dace.dmgr.combat.entity.module;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.HologramUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 피해를 받을 수 있는 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Damageable}을 상속받는 클래스여야 하며,
 * 엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see Damageable
 */
public class DamageModule {
    /** 방어력 배수 기본값 */
    public static final double DEFAULT_VALUE = 1;
    /** 치명타 배수 기본값 */
    public static final double DEFAULT_CRIT_MULTIPLIER = 2;
    /** 생명력 홀로그램 ID */
    public static final String HEALTH_HOLOGRAM_ID = "HitHealth";
    /** 피격 시 애니메이션 쿨타임 ID */
    private static final String COOLDOWN_ID = "DamageAnimation";

    /** NMS 플레이어 반환 메소드 객체 */
    private static Method getHandleMethod;
    /** 플레이어 보호막 설정 메소드 객체 */
    private static Method setAbsorptionHeartsMethod;

    /** 엔티티 객체 */
    @NonNull
    @Getter
    protected final Damageable combatEntity;
    /** 엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부 */
    @Getter
    protected final boolean isUltProvider;
    /** 생명력 홀로그램 표시 여부 */
    @Getter
    protected final boolean isShowHealthBar;
    /** 살아있는(Living) 엔티티 여부 */
    @Getter
    protected final boolean isLiving;
    /** 죽었을 때 공격자에게 주는 점수 */
    @Getter
    protected final int score;
    /** 방어력 배수 값 */
    @NonNull
    @Getter
    private final AbilityStatus defenseMultiplierStatus;
    /** 보호막 목록 (보호막 ID : 보호막 양) */
    private final HashMap<String, Integer> shieldMap = new HashMap<>();
    /** 최대 체력 */
    @Getter
    protected int maxHealth;

    /**
     * 피해 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity      대상 엔티티
     * @param isUltProvider     엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부
     * @param isShowHealthBar   생명력 홀로그램 표시 여부
     * @param isLiving          살아있는(Living) 엔티티 여부
     * @param score             죽었을 때 공격자에게 주는 점수. 0 이상의 값
     * @param maxHealth         최대 체력. 0 이상의 값
     * @param defenseMultiplier 방어력 배수 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를
     *                                  상속받지 않으면 발생
     */
    public DamageModule(@NonNull Damageable combatEntity, boolean isUltProvider, boolean isShowHealthBar, boolean isLiving,
                        int score, int maxHealth, double defenseMultiplier) {
        if (score < 0 || maxHealth < 0 || defenseMultiplier < 0)
            throw new IllegalArgumentException("'score', 'maxHealth' 및 'defenseMultiplier'가 0 이상이어야 함");
        if (!(combatEntity.getEntity() instanceof LivingEntity))
            throw new IllegalArgumentException("'combatEntity'의 엔티티가 LivingEntity를 상속받지 않음");

        this.combatEntity = combatEntity;
        this.defenseMultiplierStatus = new AbilityStatus(defenseMultiplier);
        this.isUltProvider = isUltProvider;
        this.isShowHealthBar = isShowHealthBar;
        this.isLiving = isLiving;
        this.score = score;
        this.maxHealth = maxHealth;

        setMaxHealth(getMaxHealth());
        setHealth(getMaxHealth());

        if (isShowHealthBar)
            TaskUtil.addTask(combatEntity, new DelayTask(this::addHealthHologram, 5));
    }

    /**
     * 피해 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity    대상 엔티티
     * @param isUltProvider   엔티티가 공격당했을 때 공격자에게 궁극기 게이지 제공 여부
     * @param isShowHealthBar 생명력 홀로그램 표시 여부
     * @param score           죽었을 때 공격자에게 주는 점수. 0 이상의 값
     * @param maxHealth       최대 체력. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를
     *                                  상속받지 않으면 발생
     */
    public DamageModule(@NonNull Damageable combatEntity, boolean isUltProvider, boolean isShowHealthBar, boolean isLiving,
                        int score, int maxHealth) {
        this(combatEntity, isUltProvider, isShowHealthBar, isLiving, score, maxHealth, DEFAULT_VALUE);
    }

    /**
     * 생명력 홀로그램을 생성한다.
     */
    private void addHealthHologram() {
        HologramUtil.addHologram(HEALTH_HOLOGRAM_ID + combatEntity, combatEntity.getEntity(),
                0, combatEntity.getEntity().getHeight() + 0.4, 0, "§f");
        HologramUtil.setHologramVisibility(HEALTH_HOLOGRAM_ID + combatEntity, false, Bukkit.getOnlinePlayers().toArray(new Player[0]));

        new IntervalTask(i -> {
            if (combatEntity.isDisposed())
                return false;

            int current = getHealth();
            int max = getMaxHealth();
            ChatColor color;
            if (combatEntity.getStatusEffectModule().hasStatusEffectType(StatusEffectType.HEAL_BLOCK))
                color = ChatColor.DARK_PURPLE;
            else if (current <= max / 4)
                color = ChatColor.RED;
            else if (current <= max / 2)
                color = ChatColor.YELLOW;
            else
                color = ChatColor.GREEN;

            HologramUtil.editHologram(HEALTH_HOLOGRAM_ID + combatEntity, StringFormUtil.getProgressBar(current, max, color));

            return true;
        }, isCancelled -> HologramUtil.removeHologram(HEALTH_HOLOGRAM_ID + combatEntity), 1);
    }

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 1000)
     */
    public final int getHealth() {
        return (int) (Math.round(((LivingEntity) combatEntity.getEntity()).getHealth() * 50 * 100) / 100);
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 1000)
     */
    public final void setHealth(int health) {
        ((LivingEntity) combatEntity.getEntity()).setHealth(Math.min(Math.max(0, health), getMaxHealth()) / 50.0);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 1000). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final void setMaxHealth(int health) {
        if (health < 0)
            throw new IllegalArgumentException("'health'가 0 이상이어야 함");

        maxHealth = health;
        if (maxHealth < getHealth())
            setHealth(health);
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
     * 엔티티의 보호막을 반환한다.
     *
     * @return 실제 보호막×50 (체력 1줄 기준 1000)
     */
    public final int getShield() {
        return shieldMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 엔티티의 보호막을 반환한다.
     *
     * @param id 보호막 ID
     * @return 실제 보호막×50 (체력 1줄 기준 1000)
     */
    public final int getShield(@NonNull String id) {
        return shieldMap.getOrDefault(id, 0);
    }

    /**
     * 엔티티의 보호막을 설정한다.
     *
     * @param id     보호막 ID
     * @param shield 실제 보호막×50 (체력 1줄 기준 1000)
     */
    public final void setShield(@NonNull String id, int shield) {
        if (shield <= 0)
            shieldMap.remove(id);
        else
            shieldMap.put(id, shield);

        if (!(combatEntity.getEntity() instanceof Player))
            return;

        try {
            if (getHandleMethod == null) {
                getHandleMethod = combatEntity.getEntity().getClass().getMethod("getHandle");
                getHandleMethod.setAccessible(true);
            }

            Object nmsPlayer = getHandleMethod.invoke(combatEntity.getEntity());
            if (setAbsorptionHeartsMethod == null)
                setAbsorptionHeartsMethod = nmsPlayer.getClass().getMethod("setAbsorptionHearts", Float.TYPE);

            setAbsorptionHeartsMethod.invoke(nmsPlayer, getShield() / 50F);
        } catch (Exception ex) {
            ConsoleLogger.severe("보호막을 표시할 수 없음", ex);
        }
    }

    /**
     * 엔티티의 보호막을 초기화한다.
     */
    public final void clearShield() {
        shieldMap.clear();
        setShield("", 0);
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
    private boolean handleDamage(@Nullable Attacker attacker, int damage, double damageMultiplier, double defenseMultiplier,
                                 @NonNull DamageType damageType, Location location, double critMultiplier, boolean isUlt) {
        if (combatEntity.getEntity().isDead() || !combatEntity.canTakeDamage() ||
                combatEntity.getStatusEffectModule().hasAnyRestriction(CombatRestrictions.DAMAGED))
            return false;
        if (damage == 0)
            return true;

        if (damageType == DamageType.IGNORE_DEFENSE || damageType == DamageType.FIXED) {
            defenseMultiplier = 1;
            if (damageType == DamageType.FIXED)
                damageMultiplier = 1;
        }

        damage *= (int) critMultiplier;
        int finalDamage = Math.max(0, (int) (damage * (1 + damageMultiplier - defenseMultiplier)));

        if (getShield() > 0 && damageType != DamageType.IGNORE_DEFENSE)
            for (String id : shieldMap.keySet()) {
                if (getShield(id) > finalDamage) {
                    setShield(id, getShield() - finalDamage);
                    finalDamage = 0;
                    break;
                }

                finalDamage -= getShield(id);
                setShield(id, 0);
            }

        finalDamage = Math.min(finalDamage, getHealth());
        int reducedDamage = Math.max(0, (int) (damage * damageMultiplier) - finalDamage);

        if (attacker != null)
            attacker.onAttack(combatEntity, finalDamage, damageType, critMultiplier != 1, isUlt);
        combatEntity.onDamage(attacker, finalDamage, reducedDamage, damageType, location, critMultiplier != 1, isUlt);
        playHitEffect();

        if (getHealth() > finalDamage)
            setHealth(getHealth() - finalDamage);
        else {
            if (attacker != null)
                attacker.onKill(combatEntity);
            combatEntity.onDeath(attacker);
        }

        return true;
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param attacker       공격자
     * @param damage         피해량. 0 이상의 값
     * @param damageType     피해 타입
     * @param location       맞은 위치
     * @param critMultiplier 치명타 배수. 1로 설정 시 치명타 미적용. 0 이상의 값
     * @param isUlt          궁극기 충전 여부
     * @return 피해 여부. 피해를 입었으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final boolean damage(@Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, double critMultiplier, boolean isUlt) {
        if (damage < 0 || critMultiplier < 0)
            throw new IllegalArgumentException("'damage' 및 'critMultiplier'가 0 이상이어야 함");

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
    public final boolean damage(@Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit, boolean isUlt) {
        return damage(attacker, damage, damageType, location, isCrit ? DEFAULT_CRIT_MULTIPLIER : 1, isUlt);
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param projectile     공격자가 발사한 투사체
     * @param damage         피해량. 0 이상의 값
     * @param damageType     피해 타입
     * @param location       맞은 위치
     * @param critMultiplier 치명타 배수. 1로 설정 시 치명타 미적용. 0 이상의 값
     * @param isUlt          궁극기 충전 여부
     * @return 피해 여부. 피해를 입었으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final boolean damage(@NonNull Projectile projectile, int damage, @NonNull DamageType damageType, Location location, double critMultiplier, boolean isUlt) {
        if (damage < 0 || critMultiplier < 0)
            throw new IllegalArgumentException("'damage' 및 'critMultiplier'가 0 이상이어야 함");

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
    public final boolean damage(@NonNull Projectile projectile, int damage, @NonNull DamageType damageType, Location location, boolean isCrit, boolean isUlt) {
        return damage(projectile, damage, damageType, location, isCrit ? DEFAULT_CRIT_MULTIPLIER : 1, isUlt);
    }

    /**
     * 엔티티의 피격 효과를 재생한다.
     */
    private void playHitEffect() {
        if (CooldownUtil.getCooldown(this, COOLDOWN_ID) == 0) {
            CooldownUtil.setCooldown(this, COOLDOWN_ID, 6);
            WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

            packet.setEntityID(combatEntity.getEntity().getEntityId());
            packet.setEntityStatus((byte) 2);

            packet.broadcastPacket();
        }
    }
}
