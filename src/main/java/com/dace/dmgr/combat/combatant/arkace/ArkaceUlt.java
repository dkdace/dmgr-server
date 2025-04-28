package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class ArkaceUlt extends UltimateSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public ArkaceUlt(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceUltInfo.getInstance(), ArkaceUltInfo.DURATION, ArkaceUltInfo.COST);
        this.bonusScoreModule = new BonusScoreModule(this, "궁극기 보너스", ArkaceUltInfo.KILL_SCORE);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();

        ArkaceWeapon weapon = (ArkaceWeapon) combatUser.getActionManager().getWeapon();
        weapon.cancel();
        weapon.getReloadModule().resetRemainingAmmo();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
