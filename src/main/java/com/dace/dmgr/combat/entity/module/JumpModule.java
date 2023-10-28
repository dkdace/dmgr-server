package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.Jumpable;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 점프가 가능한 엔티티의 모듈 클래스.
 *
 * <p>엔티티가 {@link Jumpable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Jumpable
 */
public final class JumpModule extends MoveModule {
    public JumpModule(Jumpable combatEntity) {
        super(combatEntity);
    }

    @Override
    public void onTick(int i) {
        super.onTick(i);

        if (canJump())
            combatEntity.getEntity().removePotionEffect(PotionEffectType.JUMP);
        else
            combatEntity.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999, -6,
                    false, false), true);
    }

    /**
     * 엔티티가 움직일 수 있는 지 확인한다.
     *
     * @return 이동 가능 여부
     */
    public boolean canJump() {
        if (combatEntity.hasStatusEffect(StatusEffectType.STUN) || combatEntity.hasStatusEffect(StatusEffectType.SNARE) ||
                combatEntity.hasStatusEffect(StatusEffectType.GROUNDING))
            return false;
        return combatEntity.getPropertyManager().getValue(Property.FREEZE) < JagerT1Info.NO_JUMP;
    }
}
