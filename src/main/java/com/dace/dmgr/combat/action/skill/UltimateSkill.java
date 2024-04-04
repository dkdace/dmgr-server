package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
public abstract class UltimateSkill extends ActiveSkill {
    protected UltimateSkill(int number, @NonNull CombatUser combatUser, @NonNull UltimateSkillInfo ultimateSkillInfo) {
        super(number, combatUser, ultimateSkillInfo, 3);
    }

    @Override
    @NonNull
    public final ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_4};
    }

    @Override
    public final long getDefaultCooldown() {
        return -1;
    }

    @Override
    protected void onCooldownFinished() {
        super.onCooldownFinished();
        SoundUtil.playNamedSound(NamedSound.COMBAT_ULTIMATE_SKILL_READY, combatUser.getEntity());
    }

    @Override
    @MustBeInvokedByOverriders
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.setUltGaugePercent(0);
        SoundUtil.playNamedSound(NamedSound.COMBAT_ULTIMATE_SKILL_USE, combatUser.getEntity().getLocation());
    }

    /**
     * 필요 충전량을 반환한다.
     *
     * @return 필요 충전량
     */
    public abstract int getCost();
}
