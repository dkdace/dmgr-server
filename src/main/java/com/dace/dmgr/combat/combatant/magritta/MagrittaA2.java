package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public final class MagrittaA2 extends ActiveSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(MagrittaA2Info.SPEED);

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

        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.getStatusEffectModule().apply(Invulnerable.getInstance(), MagrittaA2Info.DURATION);
        combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                (int) MagrittaA2Info.DURATION.toTicks(), 2, false, false), true);

        Weapon weapon = combatUser.getActionManager().getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        MagrittaA2Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation().add(0, 0.1, 0);

            MagrittaA2Info.Particles.TICK_CORE.play(loc);
            MagrittaA2Info.Particles.TICK_DECO.play(combatUser.getCenterLocation());
        }, 1, MagrittaA2Info.DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);

        MagrittaWeapon weapon = (MagrittaWeapon) combatUser.getActionManager().getWeapon();
        weapon.setVisible(true);
        weapon.getReloadModule().resetRemainingAmmo();

        MagrittaA2Info.Sounds.USE.play(combatUser.getLocation());
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
