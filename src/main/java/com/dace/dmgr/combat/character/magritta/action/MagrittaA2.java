package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class MagrittaA2 extends ActiveSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "MagrittaA2";

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
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, MagrittaA2Info.SPEED);
        combatUser.getStatusEffectModule().applyStatusEffect(combatUser, Invulnerable.getInstance(), MagrittaA2Info.DURATION);
        combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                (int) MagrittaA2Info.DURATION, 2, false, false), true);

        SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_A2_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (combatUser.isDead())
                return false;

            Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
            ParticleUtil.play(Particle.SMOKE_LARGE, loc, 6, 0.5, 0, 0.5, 0);
            ParticleUtil.play(Particle.FLAME, loc, 4, 0.4, 0, 0.4, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, combatUser.getCenterLocation(), 6,
                    1, 1.5, 1, 255, 70, 0);

            return true;
        }, isCancelled -> {
            combatUser.getWeapon().setVisible(true);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
            ((MagrittaWeapon) combatUser.getWeapon()).getReloadModule().setRemainingAmmo(MagrittaWeaponInfo.CAPACITY);

            SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_A2_USE, combatUser.getEntity().getLocation());
        }, 1, MagrittaA2Info.DURATION));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
