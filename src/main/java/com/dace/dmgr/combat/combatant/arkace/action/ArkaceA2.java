package com.dace.dmgr.combat.combatant.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class ArkaceA2 extends ActiveSkill {
    public ArkaceA2(@NonNull CombatUser combatUser) {
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        ArkaceA2Info.SOUND.USE.play(combatUser.getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (combatUser.getDamageModule().heal(combatUser, (double) ArkaceA2Info.HEAL / ArkaceA2Info.DURATION, true))
                combatUser.addScore("회복", (double) ArkaceA2Info.HEAL_SCORE / ArkaceA2Info.DURATION);

            playTickEffect(i);
        }, 1, ArkaceA2Info.DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
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
        Location loc = combatUser.getLocation().add(0, 1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        long angle = i * 10;
        for (int j = 0; j < 3; j++) {
            angle += 120;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            ArkaceA2Info.PARTICLE.TICK.play(loc.clone().add(vec), j / 2.0);
        }
    }
}
