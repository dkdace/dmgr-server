package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.SkillBase;
import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskTimer;

public final class JagerP1 extends SkillBase {
    public JagerP1(CombatUser combatUser) {
        super(1, combatUser, JagerP1Info.getInstance());
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && canActivate();
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @return 활성화 조건
     */
    private boolean canActivate() {
        JagerA1 skill1 = (JagerA1) combatUser.getSkill(JagerA1Info.getInstance());

        if (!skill1.isDurationFinished() && skill1.getSummonEntity() != null) {
            JagerA1Entity jagerA1Entity = skill1.getSummonEntity();

            return jagerA1Entity.getHitboxes()[0].isInHitbox(combatUser.getEntity().getLocation(), JagerP1Info.DETECT_RADIUS);
        }

        return false;
    }

    @Override
    public void onUse(ActionKey actionKey) {
        setDuration();
        combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).addModifier("JagerP1", JagerP1Info.SPEED);

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                return canActivate();
            }

            @Override
            public void onEnd(boolean cancelled) {
                setDuration(0);
                combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).removeModifier("JagerP1");
            }
        };
    }
}
