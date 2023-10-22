package com.dace.dmgr.combat.entity;

import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

/**
 * 일시적인 엔티티 클래스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
public abstract class TemporalEntity<T extends LivingEntity> extends CombatEntityBase<T> {
    /** 최대 체력 */
    protected final int maxHealth;

    /**
     * 일시적 엔티티 인스턴스를 생성한다.
     *
     * <p>{@link CombatEntityBase#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param entity    대상 엔티티
     * @param name      이름
     * @param maxHealth 최대 체력
     * @param hitbox    히트박스
     */
    protected TemporalEntity(T entity, String name, int maxHealth, Hitbox... hitbox) {
        super(entity, name, hitbox);
        this.maxHealth = maxHealth;
    }

    @Override
    public void init() {
        super.init();

        EntityInfoRegistry.addTemporalEntity(getEntity(), this);
        abilityStatusManager.getAbilityStatus(Ability.SPEED).setBaseValue(entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .getBaseValue());
    }

    /**
     * 엔티티를 제거한다.
     */
    public void remove() {
        EntityInfoRegistry.removeTemporalEntity(entity);
        entity.remove();
    }

    @Override
    public void onDeath(CombatEntity attacker) {
        remove();
    }
}
