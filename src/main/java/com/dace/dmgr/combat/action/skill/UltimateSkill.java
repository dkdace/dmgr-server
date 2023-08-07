package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Sound;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class UltimateSkill extends Skill {
    protected UltimateSkill(int number, CombatUser combatUser, SkillInfo skillInfo, int slot) {
        super(number, combatUser, skillInfo, slot);
    }

    /**
     * 필요 충전량을 반환한다.
     *
     * @return 필요 충전량
     */
    public abstract int getCost();

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
     * 사용 이벤트를 호출한다.
     *
     * @param actionKey 상호작용 키
     */
    protected abstract void onUseUltimateSkill(ActionKey actionKey);
}
