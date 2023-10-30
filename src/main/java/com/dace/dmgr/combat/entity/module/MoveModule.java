package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 움직일 수 있는 엔티티의 모듈 클래스.
 *
 * <p>엔티티가 {@link Movable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Movable
 */
@RequiredArgsConstructor
public class MoveModule implements CombatEntityModule {
    /** 엔티티 객체 */
    protected final Movable combatEntity;

    @Override
    @MustBeInvokedByOverriders
    public void onTick(int i) {
        combatEntity.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).addModifier("JagerT1", -combatEntity.getPropertyManager()
                .getValue(Property.FREEZE));
        if (CooldownManager.getCooldown(combatEntity, Cooldown.JAGER_FREEZE_VALUE_DURATION) == 0)
            combatEntity.getPropertyManager().setValue(Property.FREEZE, 0);
    }

    /**
     * 엔티티가 움직일 수 있는 지 확인한다.
     *
     * @return 이동 가능 여부
     */
    public final boolean canMove() {
        return !combatEntity.hasStatusEffect(StatusEffectType.STUN) && !combatEntity.hasStatusEffect(StatusEffectType.SNARE);
    }
}
