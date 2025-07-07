package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class ArkaceUlt extends UltimateSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public ArkaceUlt(@NonNull CombatUser combatUser, @NonNull ArkaceUltInfo skillInfo) {
        super(combatUser, skillInfo, ArkaceUltInfo.DURATION, ArkaceUltInfo.COST);
        this.bonusScoreModule = new BonusScoreModule(this);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onSlot() {
        setDuration();

        ArkaceWeapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.cancel();
        weapon.getReloadModule().resetRemainingAmmo();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    @NonNull
    public CombatScore getCombatScore() {
        return ArkaceUltInfo.KILL_SCORE;
    }
}
