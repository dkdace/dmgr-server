package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.HashMapList;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.RegionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import static com.dace.dmgr.system.HashMapList.combatEntityMap;

/**
 * 전투 시스템의 엔티티 정보를 관리하는 클래스.
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
@Getter
public abstract class CombatEntity<T extends LivingEntity> {
    /** 히트박스 객체 */
    private final Hitbox hitbox;
    /** 치명타 히트박스 객체 */
    private final Hitbox critHitbox;
    /** 고정 여부 */
    private final boolean isFixed;
    /** 엔티티 객체 */
    protected T entity;
    /** 이름 */
    private String name;
    /** 팀 */
    @Setter
    private String team = "";
    /** 이동속도 증가량 */
    private int speedIncrement = 0;

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성하고 {@link HashMapList#temporalEntityMap}에 추가한다.
     *
     * <p>플레이어의 경우 전투 입장 시 호출해야 하며, 퇴장 시 {@link HashMapList#combatEntityMap}
     * 에서 제거해야 한다.</p>
     *
     * @param entity     대상 엔티티
     * @param name       이름
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     * @param isFixed    위치 고정 여부
     * @see HashMapList#combatEntityMap
     */
    protected CombatEntity(T entity, String name, Hitbox hitbox, Hitbox critHitbox, boolean isFixed) {
        this.entity = entity;
        this.name = name;
        this.hitbox = hitbox;
        this.critHitbox = critHitbox;
        this.isFixed = isFixed;
        init();
    }

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성한다.
     *
     * <p>아직 소환되지 않은 엔티티를 위한 생성자이며, 소환 후 {@link CombatEntity#init()}을
     * 호출해야 한다.</p>
     *
     * @param name       이름
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     * @param isFixed    위치 고정 여부
     * @see CombatEntity#init()
     */
    protected CombatEntity(String name, Hitbox hitbox, Hitbox critHitbox, boolean isFixed) {
        this.name = name;
        this.hitbox = hitbox;
        this.critHitbox = critHitbox;
        this.isFixed = isFixed;
    }

    /**
     * 엔티티를 초기화한다.
     *
     * <p>엔티티를 {@link HashMapList#temporalEntityMap}에 추가하며, 엔티티 소멸 시
     * {@link HashMapList#combatEntityMap}에서 제거해야 한다.</p>
     *
     * @see HashMapList#combatEntityMap
     */
    protected void init() {
        if (entity == null)
            return;

        combatEntityMap.put(entity, this);
        hitbox.setCenter(entity.getLocation());
        critHitbox.setCenter(entity.getLocation());
        if (!isFixed)
            runHitboxTick();
    }

    /**
     * 엔티티의 히트박스 위치를 갱신하는 스케쥴러를 실행한다.
     *
     * <p>넷코드 문제를 해결하기 위해 사용하며, 고정된 엔티티는 사용하지 않는다.</p>
     */
    private void runHitboxTick() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatEntityMap.get(entity) == null)
                    return false;

                Location oldLoc = entity.getLocation();

                new TaskWait(2) {
                    @Override
                    public void run() {
                        hitbox.setCenter(oldLoc);
                        critHitbox.setCenter(oldLoc);
                    }
                };

                return true;
            }
        };
    }

    public void setName(String name) {
        entity.setCustomName(name);
        this.name = name;
    }

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public int getHealth() {
        return (int) (Math.round(entity.getHealth() * 50 * 100) / 100);
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public void setHealth(int health) {
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
    public int getMaxHealth() {
        return (int) (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 50);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    public void setMaxHealth(int health) {
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
    }

    /**
     * 엔티티의 이동속도 증가량을 설정한다.
     *
     * @param speedIncrement 이동속도 증가량. 최소 값은 {@code -100}, 최대 값은 {@code 100}
     */
    public void addSpeedIncrement(int speedIncrement) {
        this.speedIncrement += speedIncrement;
        if (this.speedIncrement < -100) this.speedIncrement = -100;
        if (this.speedIncrement > 100) this.speedIncrement = 100;
    }

    /**
     * 지정한 대상 엔티티를 공격한다.
     *
     * @param target 공격 대상
     * @param damage 피해량
     * @param type   타입
     * @param isCrit 치명타 여부
     * @param isUlt  궁극기 충전 여부
     * @see CombatEntity#heal(CombatEntity, int, boolean)
     * @see CombatEntity#kill(CombatEntity)
     */
    public void attack(CombatEntity<?> target, int damage, String type, boolean isCrit, boolean isUlt) {
        LivingEntity victimEntity = target.getEntity();
        boolean killed = false;

        if (victimEntity.isDead())
            return;
        if (!target.isDamageable())
            return;

        if (victimEntity.getType() != EntityType.ZOMBIE && victimEntity.getType() != EntityType.PLAYER)
            isCrit = false;

        int rdamage = damage;
        damage = getFinalDamage(target, damage, isCrit);

        onAttack(target, damage, type, isCrit, isUlt);
        target.onDamage(this, damage, type, isCrit, isUlt);
        target.playHitEffect();

        if (target.getHealth() - damage <= 0) {
            if (isKillable(target))
                killed = true;
            else
                target.setHealth(1);
        } else
            target.setHealth(target.getHealth() - damage);

        if (killed)
            kill(target);
    }

    /**
     * 지정한 대상 엔티티를 치유한다.
     *
     * @param target 치유 대상
     * @param amount 치유량
     * @param isUlt  궁극기 충전 여부
     * @see CombatEntity#onDamage(CombatEntity, int, String, boolean, boolean)
     */
    public void heal(CombatEntity<?> target, int amount, boolean isUlt) {
        if (target.getHealth() == target.getMaxHealth())
            return;

        int bonus = 0;

        amount = amount * (100 + bonus) / 100;
        onHeal(target, amount, isUlt);
        target.playHealEffect(amount);

        target.setHealth(target.getHealth() + amount);
    }

    /**
     * 지정한 대상 엔티티를 처치한다.
     *
     * @param target 공격 대상
     */
    public void kill(CombatEntity<?> target) {
        onKill(target);
        target.onDeath(this);
    }

    /**
     * 각종 변수를 계산하여 최종 피해량을 반환한다.
     *
     * @param target 공격 대상
     * @param damage 피해량
     * @param isCrit 치명타 여부
     * @return 최종 피해량
     */
    private int getFinalDamage(CombatEntity<?> target, int damage, boolean isCrit) {
        if (isCrit)
            damage *= 1.5;

        int atkBonus = 0;
        int defBonus = 0;

        return damage * (100 + atkBonus - defBonus) / 100;
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
    public boolean isDamageable() {
        return true;
    }

    /**
     * 지정한 대상을 죽일 수 있는 지 확인한다.
     *
     * <p>Condition:</p>
     *
     * <p>- 훈련장에 있을 때는 죽일 수 없다.</p>
     *
     * @param target 대상
     * @return 대상을 죽일 수 있으면 {@code true} 반환
     */
    private boolean isKillable(CombatEntity<?> target) {
        if (RegionUtil.isInRegion(entity, "BattleTrain"))
            return false;

        return true;
    }

    /**
     * 피격 효과를 재생한다.
     */
    private void playHitEffect() {
        if (CooldownManager.getCooldown(this, Cooldown.DAMAGE_ANIMATION) == 0) {
            CooldownManager.setCooldown(this, Cooldown.DAMAGE_ANIMATION);
            CombatUtil.sendDamagePacket(entity);
        }
    }

    /**
     * 치유 효과를 재생한다.
     *
     * @param amount 치유량
     */
    private void playHealEffect(int amount) {
        if (amount > 100)
            ParticleUtil.play(Particle.HEART, LocationUtil.getLocationFromOffset(entity.getLocation(),
                            0, entity.getHeight() + 0.3, 0), (int) Math.ceil(amount / 100F),
                    0.3F, 0.1F, 0.3F, 0);
        else if (amount / 100F > Math.random()) {
            ParticleUtil.play(Particle.HEART, LocationUtil.getLocationFromOffset(entity.getLocation(),
                    0, entity.getHeight() + 0.3, 0), 1, 0.3F, 0.1F, 0.3F, 0);
        }
    }

    /**
     * 엔티티가 다른 엔티티를 공격했을 때 실행될 작업
     *
     * @param victim 피격자
     * @param damage 피해량
     * @param type   타입
     * @param isCrit 치명타 여부
     * @param isUlt  궁극기 충전 여부
     */
    protected void onAttack(CombatEntity<?> victim, int damage, String type, boolean isCrit, boolean isUlt) {
    }

    /**
     * 엔티티가 피해를 입었을 때 실행될 작업
     *
     * @param attacker 공격자
     * @param damage   피해량
     * @param type     타입
     * @param isCrit   치명타 여부
     * @param isUlt    궁극기 충전 여부
     */
    protected void onDamage(CombatEntity<?> attacker, int damage, String type, boolean isCrit, boolean isUlt) {
    }

    /**
     * 엔티티가 다른 엔티티를 치유했을 때 실행될 작업
     *
     * @param victim 피격자
     * @param amount 치유량
     * @param isUlt  궁극기 충전 여부
     */
    protected void onHeal(CombatEntity<?> victim, int amount, boolean isUlt) {
    }

    /**
     * 엔티티가 다른 엔티티를 죽였을 때 실행될 작업
     *
     * @param victim 피격자
     */
    protected void onKill(CombatEntity<?> victim) {
    }

    /**
     * 엔티티가 죽었을 때 실행될 작업
     *
     * @param attacker 공격자
     */
    protected void onDeath(CombatEntity<?> attacker) {
    }
}
