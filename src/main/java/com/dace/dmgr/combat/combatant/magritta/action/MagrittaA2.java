package com.dace.dmgr.combat.combatant.magritta.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class MagrittaA2 extends ActiveSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(MagrittaA2Info.SPEED);

    public MagrittaA2(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return MagrittaA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return MagrittaA2Info.DURATION;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.getWeapon().setVisible(false);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.getStatusEffectModule().apply(Invulnerable.getInstance(), combatUser, Timespan.ofTicks(MagrittaA2Info.DURATION));
        combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                (int) MagrittaA2Info.DURATION, 2, false, false), true);

        MagrittaA2Info.SOUND.USE.play(combatUser.getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (combatUser.isDead())
                return false;

            Location loc = combatUser.getLocation().add(0, 0.1, 0);
            MagrittaA2Info.PARTICLE.TICK_CORE.play(loc);
            MagrittaA2Info.PARTICLE.TICK_DECO.play(combatUser.getCenterLocation());

            return true;
        }, isCancelled -> {
            combatUser.getWeapon().setVisible(true);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
            ((MagrittaWeapon) combatUser.getWeapon()).getReloadModule().setRemainingAmmo(MagrittaWeaponInfo.CAPACITY);

            MagrittaA2Info.SOUND.USE.play(combatUser.getLocation());
        }, 1, MagrittaA2Info.DURATION));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
