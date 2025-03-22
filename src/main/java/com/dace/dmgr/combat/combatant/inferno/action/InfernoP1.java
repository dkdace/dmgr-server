package com.dace.dmgr.combat.combatant.inferno.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Area;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public final class InfernoP1 extends AbstractSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(InfernoP1Info.DEFENSE_INCREMENT);
    /** 활성화 가능 여부 */
    private boolean canActivate = false;

    public InfernoP1(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoP1Info.getInstance(), Timespan.ZERO, InfernoP1Info.DURATION);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && canActivate();
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @return 활성화 조건
     */
    private boolean canActivate() {
        canActivate = false;
        new InfernoP1Area().emit(combatUser.getLocation().add(0, 0.1, 0));

        return canActivate;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getDamageModule().getDefenseMultiplierStatus().addModifier(MODIFIER);
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();
        combatUser.getDamageModule().getDefenseMultiplierStatus().removeModifier(MODIFIER);
    }

    @Override
    public boolean isCancellable() {
        return combatUser.isDead();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class InfernoP1Area extends Area<Damageable> {
        private InfernoP1Area() {
            super(combatUser, InfernoP1Info.DETECT_RADIUS, CombatUtil.EntityCondition.enemy(combatUser)
                    .and(combatEntity -> combatEntity.getStatusEffectModule().hasType(StatusEffectType.BURNING)));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            canActivate = true;
            return true;
        }
    }
}
