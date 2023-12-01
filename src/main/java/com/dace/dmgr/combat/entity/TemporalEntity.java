package com.dace.dmgr.combat.entity;

import com.dace.dmgr.system.task.TaskManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 전투 시스템의 일시적인 엔티티 클래스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 * @see SummonEntity
 */
public abstract class TemporalEntity<T extends LivingEntity> extends CombatEntityBase<T> {
    /**
     * 일시적 엔티티 인스턴스를 생성한다.
     *
     * <p>{@link CombatEntityBase#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param entity 대상 엔티티
     * @param name   이름
     * @param hitbox 히트박스 목록
     */
    protected TemporalEntity(T entity, String name, Hitbox... hitbox) {
        super(entity, name, hitbox);
    }

    @Override
    @MustBeInvokedByOverriders
    public void init() {
        super.init();

        if (getAbilityStatusManager().getAbilityStatus(Ability.SPEED).getBaseValue() == 0)
            getAbilityStatusManager().getAbilityStatus(Ability.SPEED).setBaseValue(getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                    .getBaseValue());
    }

    @Override
    @MustBeInvokedByOverriders
    public void remove() {
        super.remove();

        TaskManager.clearTask(this);
        getEntity().remove();
    }

    @Override
    public boolean canBeTargeted() {
        return true;
    }

    @Override
    public String getTaskIdentifier() {
        return toString();
    }
}
