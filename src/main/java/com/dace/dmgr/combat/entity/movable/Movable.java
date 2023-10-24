package com.dace.dmgr.combat.entity.movable;

import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;

/**
 * 움직일 수 있는 엔티티의 인터페이스.
 *
 * @see Jumpable
 */
public interface Movable extends CombatEntity {
    @Override
    default void onTick(int i) {
        getAbilityStatusManager().getAbilityStatus(Ability.SPEED).addModifier("JagerT1", -getPropertyManager().getValue(Property.FREEZE));
        if (CooldownManager.getCooldown(this, Cooldown.JAGER_FREEZE_VALUE_DURATION) == 0)
            getPropertyManager().setValue(Property.FREEZE, 0);

        onTickMovable(i);
    }

    /**
     * @see CombatEntity#onTick(int)
     */
    void onTickMovable(int i);

    /**
     * 엔티티가 움직일 수 있는 지 확인한다.
     *
     * @return 이동 가능 여부
     */
    default boolean canMove() {
        return !hasStatusEffect(StatusEffectType.STUN) && !hasStatusEffect(StatusEffectType.SNARE);
    }
}
