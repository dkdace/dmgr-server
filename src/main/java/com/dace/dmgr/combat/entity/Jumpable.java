package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 점프가 가능한 엔티티의 인터페이스.
 */
public interface Jumpable extends Movable {
    @Override
    default void onTick(int i) {
        Movable.super.onTick(i);

        if (canJump())
            getEntity().removePotionEffect(PotionEffectType.JUMP);
        else
            getEntity().addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                    9999, -6, false, false), true);
    }

    /**
     * 엔티티가 점프할 수 있는 지 확인한다.
     *
     * @return 점프 가능 여부
     */
    default boolean canJump() {
        if (hasStatusEffect(StatusEffectType.STUN) || hasStatusEffect(StatusEffectType.SNARE) || hasStatusEffect(StatusEffectType.GROUNDING))
            return false;
        return getPropertyManager().getValue(Property.FREEZE) < JagerT1Info.NO_JUMP;
    }
}
