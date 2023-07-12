package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Sound;

/**
 * 여러 번 사용할 수 있는 스택형 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class StackableSkill extends Skill {
    /** 스킬 스택 수 */
    protected int stack = 0;

    protected StackableSkill(int number, CombatUser combatUser, SkillInfo skillInfo, int slot) {
        super(number, combatUser, skillInfo, slot);
    }

    @Override
    public void setCooldown(long cooldown) {
        addStack(-1);
    }

    @Override
    protected void onCooldownTick() {
    }

    @Override
    protected void onCooldownFinished() {
        addStack(1);
        if (stack < getMaxStack())
            runCooldown(getDefaultCooldown());

        displayReady(stack);

        if (actionInfo instanceof UltimateSkillInfo)
            SoundUtil.play(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2F, combatUser.getEntity());
        else if (actionInfo instanceof ActiveSkillInfo)
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 2F, combatUser.getEntity());
    }

    @Override
    protected void onDurationTick() {
        displayUsing(stack);
    }

    @Override
    protected void onDurationFinished() {
        addStack(-1);
    }

    /**
     * 최대 스택 충전량을 반환한다.
     *
     * @return 최대 스택 충전량
     */
    public abstract int getMaxStack();

    /**
     * 지정한 양만큼 스킬의 스택 수를 증가시킨다.
     *
     * @param amount 스택 증가량
     */
    public void addStack(int amount) {
        int max = getMaxStack();

        stack += amount;
        if (stack > max)
            stack = max;
        if (stack <= 0) {
            stack = 0;
            displayCooldown(1);
        } else {
            if (isUsing())
                displayUsing(stack);
            else
                displayReady(stack);
        }
    }

    @Override
    public boolean canUse() {
        return stack > 0;
    }
}
