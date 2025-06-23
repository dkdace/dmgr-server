package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarDisplay;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Getter(AccessLevel.PACKAGE)
public final class NeaceUlt extends UltimateSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-NeaceUltInfo.READY_SLOW);
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public NeaceUlt(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceUltInfo.getInstance(), NeaceUltInfo.DURATION, NeaceUltInfo.COST);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getActionManager().getSkill(NeaceA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(Timespan.MAX);

        combatUser.setGlobalCooldown(NeaceUltInfo.READY_DURATION);
        combatUser.getMoveModule().addModifier(MODIFIER);

        NeaceUltInfo.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> NeaceUltInfo.Effects.playUseTick(i, combatUser.getLocation()), () -> {
            cancel();

            isEnabled = true;

            setDuration();
            combatUser.getHealModule().heal(combatUser, combatUser.getDamageModule().getMaxHealth(), false);

            NeaceUltInfo.Effects.playUseReady(combatUser.getLocation());

            addActionTask(new IntervalTask(i -> {
                Location loc = combatUser.getEntity().getEyeLocation();
                new NeaceUltArea().emit(loc);

                NeaceUltInfo.Effects.playTick(i, combatUser.getLocation());
            }, 1, NeaceUltInfo.DURATION.toTicks()));
        }, 1, NeaceUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();
        isEnabled = false;
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().removeModifier(MODIFIER);
    }

    private final class NeaceUltArea extends Area<Healable> {
        private NeaceUltArea() {
            super(combatUser, NeaceWeaponInfo.Heal.MAX_DISTANCE, EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Healable target) {
            ((NeaceWeapon) combatUser.getActionManager().getWeapon()).healTarget(target);
            return true;
        }
    }
}
