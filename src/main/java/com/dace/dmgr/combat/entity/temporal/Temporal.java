package com.dace.dmgr.combat.entity.temporal;

import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskManager;
import org.bukkit.attribute.Attribute;

/**
 * 전투 시스템의 일시적인 엔티티 인터페이스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @see Summonable
 */
public interface Temporal extends CombatEntity {
    @Override
    default void onInit() {
        EntityInfoRegistry.addTemporalEntity(getEntity(), this);
        getAbilityStatusManager().getAbilityStatus(Ability.SPEED).setBaseValue(getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .getBaseValue());
        onInitTemporal();
    }

    /**
     * @see CombatEntity#onInit()
     */
    void onInitTemporal();

    @Override
    default void onRemove() {
        EntityInfoRegistry.removeTemporalEntity(getEntity());
        TaskManager.clearTask(this);
        getEntity().remove();
        onRemoveTemporal();
    }

    /**
     * @see CombatEntity#onRemove()
     */
    void onRemoveTemporal();
}
