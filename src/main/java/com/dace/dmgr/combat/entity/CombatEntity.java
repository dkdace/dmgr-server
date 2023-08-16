package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 전투 시스템의 엔티티 정보를 관리하는 클래스.
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
@Getter
public abstract class CombatEntity<T extends LivingEntity> {
    /** 엔티티 객체 */
    protected final T entity;
    /** 능력치 목록 관리 객체 */
    protected final AbilityStatusManager abilityStatusManager = new AbilityStatusManager();
    /** 속성 목록 관리 객체 */
    protected final PropertyManager propertyManager = new PropertyManager();
    /** 히트박스 객체 */
    private final Hitbox hitbox;
    /** 치명타 히트박스 객체 */
    private final Hitbox critHitbox;
    /** 고정 여부 */
    private final boolean isFixed;
    /** 이름 */
    protected String name;
    /** 팀 */
    @Setter
    protected String team = "";

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성한다.
     *
     * <p>{@link CombatEntity#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param entity     대상 엔티티
     * @param name       이름
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     * @param isFixed    위치 고정 여부
     */
    protected CombatEntity(T entity, String name, Hitbox hitbox, Hitbox critHitbox, boolean isFixed) {
        this.entity = entity;
        this.name = name;
        this.hitbox = hitbox;
        this.critHitbox = critHitbox;
        this.isFixed = isFixed;
    }

    /**
     * 엔티티를 초기화하고 틱 스케쥴러를 실행한다.
     */
    public final void init() {
        hitbox.setCenter(entity.getLocation());
        critHitbox.setCenter(entity.getLocation());
        onInit();

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatEntity(entity) == null)
                    return false;

                onTick(i);

                return true;
            }
        };
    }

    /**
     * {@link CombatEntity#init()} 호출 시 실행할 작업.
     */
    protected abstract void onInit();

    public void setName(String name) {
        entity.setCustomName(name);
        this.name = name;
    }

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final int getHealth() {
        return (int) (Math.round(entity.getHealth() * 50 * 100) / 100);
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final void setHealth(int health) {
        if (health < 0) health = 0;
        if (health > getMaxHealth()) health = getMaxHealth();
        double realHealth = health / 50.0;
        entity.setHealth(realHealth);
    }

    /**
     * 엔티티의 최대 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final int getMaxHealth() {
        return (int) (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 50);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public final void setMaxHealth(int health) {
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
    }

    /**
     * 엔티티에게 피해를 입힌다.
     *
     * @param attacker 공격자
     * @param damage   피해량
     * @param type     타입
     * @param isCrit   치명타 여부
     * @param isUlt    궁극기 충전 여부
     * @see CombatEntity#heal(CombatEntity, int, boolean)
     */
    public final void damage(CombatEntity<?> attacker, int damage, String type, boolean isCrit, boolean isUlt) {
        if (entity.isDead())
            return;
        if (!canTakeDamage())
            return;

        damage = CombatUtil.getFinalDamage(attacker, this, damage, isCrit);

        attacker.onAttack(this, damage, type, isCrit, isUlt);
        onDamage(attacker, damage, type, isCrit, isUlt);
        playDamageEffect();

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
     * 엔티티의 피격 효과를 재생한다.
     */
    private void playDamageEffect() {
        if (CooldownManager.getCooldown(this, Cooldown.DAMAGE_ANIMATION) == 0) {
            CooldownManager.setCooldown(this, Cooldown.DAMAGE_ANIMATION);
            WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

            packet.setEntityID(entity.getEntityId());
            packet.setEntityStatus((byte) 2);

            packet.broadcastPacket();
        }
    }

    /**
     * 엔티티를 치유한다.
     *
     * @param attacker 공격자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     * @see CombatEntity#onDamage(CombatEntity, int, String, boolean, boolean)
     */
    public final void heal(CombatEntity<?> attacker, int amount, boolean isUlt) {
        if (getHealth() == getMaxHealth())
            return;
        if (!canTakeHeal())
            return;

        attacker.onGiveHeal(this, amount, isUlt);

        setHealth(getHealth() + amount);
    }

    /**
     * 엔티티가 공격당했을 때 공격자에게 궁극기 게이지를 제공하는 지 확인한다.
     *
     * <p>기본값은 {@code false}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 궁극기 제공 여부
     */
    public boolean isUltProvider() {
        return false;
    }

    /**
     * 엔티티가 피해를 받을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 피격 가능 여부
     */
    protected boolean canTakeDamage() {
        return true;
    }

    /**
     * 엔티티가 치유를 받을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 피격 가능 여부
     */
    protected boolean canTakeHeal() {
        return true;
    }

    /**
     * 엔티티가 죽을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 죽을 수 있으면 {@code true} 반환
     */
    protected boolean canDie() {
        return true;
    }

    /**
     * 엔티티가 움직일 수 있는 지 확인한다.
     *
     * @return 이동 가능 여부
     */
    public final boolean canMove() {
        if (CooldownManager.getCooldown(this, Cooldown.STUN) > 0 || CooldownManager.getCooldown(this, Cooldown.SNARE) > 0)
            return false;

        return true;
    }

    /**
     * 엔티티가 점프할 수 있는 지 확인한다.
     *
     * @return 점프 가능  여부
     */
    public final boolean canJump() {
        if (CooldownManager.getCooldown(this, Cooldown.STUN) > 0 || CooldownManager.getCooldown(this, Cooldown.SNARE) > 0 ||
                CooldownManager.getCooldown(this, Cooldown.GROUNDING) > 0)
            return false;
        if (propertyManager.getValue(Property.FREEZE) >= JagerT1Info.NO_JUMP)
            return false;

        return true;
    }

    /**
     * {@link CombatEntity#init()}에서 매 틱마다 실행될 작업.
     *
     * @param i 인덱스
     */
    public void onTick(int i) {
        if (!isFixed)
            updateHitboxTick();

        if (canJump())
            entity.removePotionEffect(PotionEffectType.JUMP);
        else
            entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                    9999, -6, false, false), true);

        abilityStatusManager.getAbilityStatus(Ability.SPEED).addModifier("JagerT1", -propertyManager.getValue(Property.FREEZE));
        if (CooldownManager.getCooldown(this, Cooldown.FREEZE_VALUE_DURATION) == 0)
            propertyManager.setValue(Property.FREEZE, 0);
    }

    /**
     * 엔티티의 히트박스를 업데이트한다.
     */
    private void updateHitboxTick() {
        Location oldLoc = entity.getLocation();

        new TaskWait(2) {
            @Override
            public void run() {
                hitbox.setCenter(oldLoc);
                critHitbox.setCenter(oldLoc);
            }
        };
    }

    /**
     * 엔티티가 다른 엔티티를 공격했을 때 실행될 작업.
     *
     * @param victim 피격자
     * @param damage 피해량
     * @param type   타입
     * @param isCrit 치명타 여부
     * @param isUlt  궁극기 충전 여부
     * @see CombatEntity#onDamage(CombatEntity, int, String, boolean, boolean)
     */
    public void onAttack(CombatEntity<?> victim, int damage, String type, boolean isCrit, boolean isUlt) {
    }

    /**
     * 엔티티가 피해를 입었을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @param damage   피해량
     * @param type     타입
     * @param isCrit   치명타 여부
     * @param isUlt    궁극기 충전 여부
     * @see CombatEntity#onAttack(CombatEntity, int, String, boolean, boolean)
     */
    public void onDamage(CombatEntity<?> attacker, int damage, String type, boolean isCrit, boolean isUlt) {
    }

    /**
     * 엔티티가 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param victim 피격자
     * @param amount 치유량
     * @param isUlt  궁극기 충전 여부
     * @see CombatEntity#onTakeHeal(CombatEntity, int, boolean)
     */
    public void onGiveHeal(CombatEntity<?> victim, int amount, boolean isUlt) {
    }

    /**
     * 엔티티가 치유를 받았을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     * @see CombatEntity#onGiveHeal(CombatEntity, int, boolean)
     */
    public void onTakeHeal(CombatEntity<?> attacker, int amount, boolean isUlt) {
    }

    /**
     * 엔티티가 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param victim 피격자
     * @see CombatEntity#onDeath(CombatEntity)
     */
    public void onKill(CombatEntity<?> victim) {
    }

    /**
     * 엔티티가 죽었을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @see CombatEntity#onKill(CombatEntity)
     */
    public void onDeath(CombatEntity<?> attacker) {
    }
}
