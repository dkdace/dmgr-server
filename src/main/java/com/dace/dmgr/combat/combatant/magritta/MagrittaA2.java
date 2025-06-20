package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongConsumer;

public final class MagrittaA2 extends ActiveSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(MagrittaA2Info.SPEED);

    public MagrittaA2(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaA2Info.getInstance(), MagrittaA2Info.COOLDOWN, MagrittaA2Info.DURATION, 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.RIGHT_CLICK};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getActionManager().getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getMoveModule().addModifier(MODIFIER);
        combatUser.getStatusEffectModule().apply(Invulnerable.getInstance(), MagrittaA2Info.DURATION);
        combatUser.getMoveModule().setJumpStrength(3);

        Weapon weapon = combatUser.getActionManager().getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        MagrittaA2Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask((LongConsumer) i -> MagrittaA2Info.Effects.playTick(combatUser.getLocation()), 1,
                MagrittaA2Info.DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        combatUser.getMoveModule().removeModifier(MODIFIER);
        combatUser.getMoveModule().setJumpStrength(0);

        MagrittaWeapon weapon = (MagrittaWeapon) combatUser.getActionManager().getWeapon();
        weapon.setVisible(true);
        weapon.getReloadModule().resetRemainingAmmo();

        MagrittaA2Info.Effects.USE.play(combatUser.getLocation());
    }

    @Override
    public boolean isCancellable() {
        return combatUser.isDead();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }
}
