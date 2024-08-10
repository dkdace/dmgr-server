package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;

public final class ArkaceP1 extends AbstractSkill {
    /** 쿨타임 ID */
    public static final String COOLDOWN_ID = "ArkaceP1";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "ArkaceP1";

    public ArkaceP1(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceP1Info.getInstance());
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
        return super.canUse() && isDurationFinished() && !((ArkaceWeapon) combatUser.getWeapon()).getReloadModule().isReloading() &&
                CooldownUtil.getCooldown(combatUser, COOLDOWN_ID) == 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, ArkaceP1Info.SPRINT_SPEED);
        combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.SPRINT);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> combatUser.getEntity().isSprinting() &&
                !((ArkaceWeapon) combatUser.getWeapon()).getReloadModule().isReloading() && CooldownUtil.getCooldown(combatUser, COOLDOWN_ID) == 0,
                isCancelled -> onCancelled(), 1));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.DEFAULT);
    }
}
