package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class ArkaceA2 extends ActiveSkill {
    ArkaceA2(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceA2Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return ArkaceA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return ArkaceA2Info.DURATION;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        SoundUtil.playNamedSound(NamedSound.COMBAT_ARKACE_A2_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            int amount = (int) (ArkaceA2Info.HEAL / ArkaceA2Info.DURATION);
            if (i == 0)
                amount += (int) (ArkaceA2Info.HEAL % ArkaceA2Info.DURATION);
            combatUser.getDamageModule().heal(combatUser, amount, true);

            playTickEffect(i);

            return true;
        }, 1, ArkaceA2Info.DURATION));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        long angle = i * 10;

        Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle);
        Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle + 120);
        Vector vec3 = VectorUtil.getRotatedVector(vector, axis, angle + 240);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec1), 3,
                0, 0.4, 0, 220, 255, 36);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec2), 3,
                0, 0.4, 0, 190, 255, 36);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec3), 3,
                0, 0.4, 0, 160, 255, 36);
    }
}
