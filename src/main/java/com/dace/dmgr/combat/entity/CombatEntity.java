package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.system.HashMapList;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
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
     * @see Combat#attack(CombatUser, CombatEntity, int, String, boolean, boolean)
     */
    public boolean isDamageable() {
        return true;
    }

    /**
     * 엔티티가 피해를 입었을 때 실행될 작업
     *
     * @param attacker 공격자
     * @param damage   피해량
     * @param type     타입
     * @param isCrit   치명타 여부
     * @param isUlt    궁극기 충전 여부
     * @see Combat#attack(CombatUser, CombatEntity, int, String, boolean, boolean)
     */
    public void onDamage(CombatUser attacker, int damage, String type, boolean isCrit, boolean isUlt) {
    }

    /**
     * 엔티티가 죽었을 때 실행될 작업
     *
     * @param attacker 공격자
     * @see Combat#kill(CombatUser, CombatEntity)
     */
    public void onDeath(CombatUser attacker) {
    }
}
