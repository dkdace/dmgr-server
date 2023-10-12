package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskTimer;

import java.util.Arrays;
import java.util.List;

public final class JagerP1 extends Skill {
    public JagerP1(CombatUser combatUser) {
        super(1, combatUser, JagerP1Info.getInstance());
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.PERIODIC_1);
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
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
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && canActivate();
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
