package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;

public final class JagerP1 extends AbstractSkill {
    public JagerP1(@NonNull CombatUser combatUser) {
        super(1, combatUser, JagerP1Info.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
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

        if (!skill1.isDurationFinished() && skill1.entity != null) {
            JagerA1Entity jagerA1Entity = skill1.entity;

            Damageable target = (Damageable) CombatUtil.getNearCombatEntity(combatUser.getGame(), combatUser.getEntity().getLocation(),
                    JagerP1Info.DETECT_RADIUS, combatEntity -> combatEntity == jagerA1Entity);

            return target != null;
        }

        return false;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getMoveModule().getSpeedStatus().addModifier("JagerP1", JagerP1Info.SPEED);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> canActivate(), isCancelled -> {
            setDuration(0);
            combatUser.getMoveModule().getSpeedStatus().removeModifier("JagerP1");
        }, 1));
    }
}
