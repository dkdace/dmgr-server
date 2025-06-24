package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.LongConsumer;

public final class MagrittaA2 extends ActiveSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(MagrittaA2Info.SPEED);

    public MagrittaA2(@NonNull CombatUser combatUser, @NonNull MagrittaA2Info skillInfo) {
        super(combatUser, skillInfo, MagrittaA2Info.COOLDOWN, MagrittaA2Info.DURATION);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_2, ActionKey.RIGHT_CLICK);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getAbilityManager().getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getMoveModule().addModifier(MODIFIER);
        combatUser.getStatusEffectModule().apply(Invulnerable.getInstance(), MagrittaA2Info.DURATION);
        combatUser.getMoveModule().setJumpStrength(3);

        Weapon weapon = combatUser.getAbilityManager().getWeapon();
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

        MagrittaWeapon weapon = (MagrittaWeapon) combatUser.getAbilityManager().getWeapon();
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
