package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

@Getter
public final class SiliaP1 extends AbstractSkill {
    public SiliaP1(@NonNull CombatUser combatUser) {
        super(1, combatUser, SiliaP1Info.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SPACE};
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
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        Location location = combatUser.getEntity().getLocation();
        playUseSound(location);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getLocation();

            if (location.distance(loc) > 0) {
                location.setY(loc.getY());
                Vector vec = (location.distance(loc) == 0) ? new Vector(0, 0, 0) : LocationUtil.getDirection(location, loc).multiply(0.35);
                vec.setY(0.55);

                combatUser.push(vec, true);

                return false;
            }

            return true;
        }, isCancelled -> {
            TaskUtil.addTask(taskRunner, new IntervalTask(i -> !combatUser.getEntity().isOnGround(),
                    isCancelled2 -> setDuration(0), 1));
        }, 1, 2));
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.ENTITY_LLAMA_SWAG, location, 0.8, 1.2);
        SoundUtil.play(Sound.BLOCK_CLOTH_STEP, location, 0.8, 1.2);
    }
}
