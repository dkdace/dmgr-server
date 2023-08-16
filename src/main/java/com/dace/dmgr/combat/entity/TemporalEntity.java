package com.dace.dmgr.combat.entity;

import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

/**
 * 일시적인 엔티티 클래스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
public abstract class TemporalEntity<T extends LivingEntity> extends CombatEntity<T> {
    /** 최대 체력 */
    protected final int maxHealth;

    /**
     * 일시적 엔티티 인스턴스를 생성한다.
     *
     * <p>{@link CombatEntity#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param entity     대상 엔티티
     * @param name       이름
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     * @param isFixed    위치 고정 여부
     * @param maxHealth  최대 체력
     */
    protected TemporalEntity(T entity, String name, Hitbox hitbox, Hitbox critHitbox, boolean isFixed, int maxHealth) {
        super(entity, name, hitbox, critHitbox, isFixed);
        this.maxHealth = maxHealth;
    }

    @Override
    protected final void onInit() {
        EntityInfoRegistry.addTemporalEntity(getEntity(), this);
        setMaxHealth(maxHealth);
        setHealth(maxHealth);
        abilityStatusManager.getAbilityStatus(Ability.SPEED).setBaseValue(entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .getBaseValue());
        onInitTemporalEntity(entity.getLocation());
    }

    /**
     * 엔티티를 제거한다.
     */
    public void remove() {
        EntityInfoRegistry.removeTemporalEntity(entity);
        entity.remove();
    }

    @Override
    public void onTick(int i) {
        super.onTick(i);

        double speed = abilityStatusManager.getAbilityStatus(Ability.SPEED).getValue();
        if (!canMove())
            speed = 0.0001F;

        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void onDeath(CombatEntity<?> attacker) {
        remove();
    }

    /**
     * {@link TemporalEntity#init()} 호출 시 실행할 작업.
     *
     * @param location 소환된 위치
     */
    protected abstract void onInitTemporalEntity(Location location);
}
