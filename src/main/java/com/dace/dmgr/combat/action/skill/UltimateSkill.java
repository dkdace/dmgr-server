package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Sound;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
public abstract class UltimateSkill extends ActiveSkill {
    protected UltimateSkill(int number, CombatUser combatUser, UltimateSkillInfo ultimateSkillInfo) {
        super(number, combatUser, ultimateSkillInfo, 3);
    }

    @Override
    public final ActionKey[] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_4};
    }

    @Override
    public final long getDefaultCooldown() {
        return -1;
    }

    @Override
    protected void playCooldownFinishSound() {
        SoundUtil.play(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2F, combatUser.getEntity());
    }

    @Override
    public final void onUse(ActionKey actionKey) {
        combatUser.setUltGaugePercent(0);
        playUseSound();

        onUseUltimateSkill(actionKey);
    }

    /**
     * 사용 효과음을 재생한다.
     */
    private void playUseSound() {
        SoundUtil.play(Sound.ENTITY_WITHER_SPAWN, combatUser.getEntity().getLocation(), 10F, 2F);
    }

    /**
     * 필요 충전량을 반환한다.
     *
     * @return 필요 충전량
     */
    public abstract int getCost();

    /**
     * @see Action#onUse(ActionKey)
     */
    protected abstract void onUseUltimateSkill(ActionKey actionKey);
}
