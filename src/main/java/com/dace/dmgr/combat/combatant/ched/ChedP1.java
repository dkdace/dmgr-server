package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.skill.WallClimbSkill;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class ChedP1 extends WallClimbSkill {
    /** 매달리기 남은 시간 (tick) */
    private long hangTick = ChedP1Info.HANG_DURATION.toTicks();
    /** 매달리기 활성화 여부 */
    @Getter(AccessLevel.PACKAGE)
    private boolean isHanging = false;

    public ChedP1(@NonNull CombatUser combatUser, @NonNull ChedP1Info skillInfo) {
        super(combatUser, skillInfo, ChedP1Info.USE_COUNT);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar(Timespan.ofTicks(hangTick), ChedP1Info.HANG_DURATION).build();
    }

    @Override
    protected double getSpeed() {
        return ChedP1Info.PUSH;
    }

    @Override
    protected void onWallClimbStart() {
        combatUser.getAbilityManager().getWeapon().setVisible(false);

        addActionTask(new IntervalTask(i -> {
            if (hangTick <= 0)
                return false;
            if (!combatUser.getEntity().isSneaking())
                return true;

            if (!isHanging) {
                setHanging(true);
                ChedP1Info.Effects.HANG_ON.play(combatUser.getLocation());
            }

            hangTick--;

            combatUser.getMoveModule().push(new Vector(), true);

            ChedP1Info.Effects.playHangTick(combatUser.getLocation());
            return true;
        }, 1));
    }

    @Override
    protected boolean canWallClimb() {
        return !combatUser.getEntity().isSneaking() || hangTick <= 0;
    }

    @Override
    protected void onWallClimbTick() {
        if (isHanging)
            setHanging(false);

        AbilityManager abilityManager = combatUser.getAbilityManager();

        ((ChedWeapon) abilityManager.getWeapon()).setCanShoot(false);
        abilityManager.getAbility(ChedA3Info.getInstance()).cancel();
        abilityManager.getAbility(ChedUltInfo.getInstance()).cancel();

        ChedP1Info.Effects.USE.play(combatUser.getLocation());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if (isHanging) {
            setHanging(false);

            ChedP1Info.Effects.HANG_OFF.play(combatUser.getLocation());
            return;
        }

        combatUser.getAbilityManager().getWeapon().setVisible(true);
    }

    @Override
    protected void onLand() {
        hangTick = ChedP1Info.HANG_DURATION.toTicks();
    }

    /**
     * 매달리기 상태를 설정한다.
     *
     * @param isEnabled 활성화 여부
     */
    private void setHanging(boolean isEnabled) {
        isHanging = isEnabled;

        combatUser.getEntity().setGravity(!isHanging);

        if (!isDurationFinished())
            combatUser.getAbilityManager().getWeapon().setVisible(isHanging);
    }
}
