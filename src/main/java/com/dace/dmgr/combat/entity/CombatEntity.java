package com.dace.dmgr.combat.entity;

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
public class CombatEntity<T extends LivingEntity> implements ICombatEntity {
    /** 엔티티 객체 */
    protected final T entity;
    /** 히트박스 객체 */
    private final Hitbox hitbox;
    /** 치명타 히트박스 객체 */
    private final Hitbox critHitbox;
    /** 이름 */
    private String name;
    /** 팀 */
    @Setter
    private String team = "";
    /** 이동속도 증가량 */
    private int speedIncrement = 0;

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성하고 {@link HashMapList#combatEntityMap}에 추가한다.
     *
     * <p>일반 엔티티의 경우 생성 시 호출, 플레이어의 경우 전투 입장 시 호출해야 하며,
     * 소멸 또는 퇴장 시 {@link HashMapList#combatEntityMap}에서 제거해야 한다.</p>
     *
     * @param entity 대상 엔티티
     * @param name   이름
     * @param hitbox 히트박스
     */
    protected CombatEntity(T entity, String name, Hitbox hitbox) {
        this(entity, name, hitbox, null);
    }

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성하고 {@link HashMapList#combatEntityMap}에 추가한다.
     *
     * <p>일반 엔티티의 경우 생성 시 호출, 플레이어의 경우 전투 입장 시 호출해야 하며,
     * 소멸 또는 퇴장 시 {@link HashMapList#combatEntityMap}에서 제거해야 한다.</p>
     *
     * @param entity     대상 엔티티
     * @param name       이름
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     */
    protected CombatEntity(T entity, String name, Hitbox hitbox, Hitbox critHitbox) {
        this.entity = entity;
        this.hitbox = hitbox;
        this.critHitbox = critHitbox;
        this.name = name;
        combatEntityMap.put(entity, this);
    }

    /**
     * 엔티티의 히트박스 위치를 갱신하는 스케쥴러를 실행한다.
     *
     * <p>넷코드 문제를 해결하기 위해 사용하며, 고정된 엔티티는 사용하지 않는다.</p>
     */
    @Override
    public void updateHitboxTick() {
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
                    }
                };

                return true;
            }
        };
    }

    @Override
    public void setName(String name) {
        entity.setCustomName(name);
        this.name = name;
    }

    /**
     * 엔티티의 체력을 반환한다.
     *
     * @return 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    @Override
    public int getHealth() {
        return (int) (Math.round(entity.getHealth() * 50 * 100) / 100);
    }

    /**
     * 엔티티의 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    @Override
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
    @Override
    public int getMaxHealth() {
        return (int) (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 50);
    }

    /**
     * 엔티티의 최대 체력을 설정한다.
     *
     * @param health 실제 체력×50 (체력 1줄 기준 {@code 1000})
     */
    @Override
    public void setMaxHealth(int health) {
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
    }

    @Override
    public void addSpeedIncrement(int speedIncrement) {
        this.speedIncrement += speedIncrement;
        if (this.speedIncrement < -100) this.speedIncrement = -100;
        if (this.speedIncrement > 100) this.speedIncrement = 100;
    }
}
