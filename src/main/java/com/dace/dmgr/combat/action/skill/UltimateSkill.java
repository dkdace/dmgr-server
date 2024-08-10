package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
public abstract class UltimateSkill extends ActiveSkill {
    /**
     * 궁극기 스킬 인스턴스를 생성한다.
     *
     * @param combatUser        대상 플레이어
     * @param ultimateSkillInfo 궁극기 정보 객체
     */
    protected UltimateSkill(@NonNull CombatUser combatUser, @NonNull UltimateSkillInfo<? extends UltimateSkill> ultimateSkillInfo) {
        super(combatUser, ultimateSkillInfo, 3);
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
    @MustBeInvokedByOverriders
    protected void onCooldownFinished() {
        super.onCooldownFinished();
        SoundUtil.playNamedSound(NamedSound.COMBAT_ULTIMATE_SKILL_READY, combatUser.getEntity());
    }

    @Override
    @MustBeInvokedByOverriders
    public void onUse(@NonNull ActionKey actionKey) {
        Validate.notNull(combatUser.getCharacterType());

        combatUser.setUltGaugePercent(0);
        SoundUtil.playNamedSound(NamedSound.COMBAT_ULTIMATE_SKILL_USE, combatUser.getEntity().getLocation());

        GameUser gameUser = GameUser.fromUser(combatUser.getUser());
        if (gameUser != null)
            gameUser.sendMessage("§l" + combatUser.getCharacterType().getCharacter().getUltUseMent(), false);
    }

    /**
     * 필요 충전량을 반환한다.
     *
     * @return 필요 충전량
     */
    public abstract int getCost();
}
