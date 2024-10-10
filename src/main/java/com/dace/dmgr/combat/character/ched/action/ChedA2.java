package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public final class ChedA2 extends ActiveSkill {
    public ChedA2(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.SPACE};
    }

    @Override
    public long getDefaultCooldown() {
        return ChedA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(ChedP1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();

        Location location = combatUser.getEntity().getLocation();
        location.setPitch(0);

        SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_A2_USE, location);
        ParticleUtil.play(Particle.EXPLOSION_NORMAL, location.clone().add(0, 0.5, 0), 20,
                0.4, 0.1, 0.4, 0.15);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getLocation();

            if (location.distance(loc) > 0) {
                location.setY(loc.getY());
                Vector vec = ((location.distance(loc) == 0) ? location.getDirection() :
                        LocationUtil.getDirection(location, loc)).multiply(ChedA2Info.PUSH_SIDE);
                vec.setY(ChedA2Info.PUSH_UP);

                combatUser.getMoveModule().push(vec, true);

                return false;
            }

            return true;
        }, isCancelled -> {
            if (isCancelled)
                return;

            Vector vec = location.getDirection().multiply(ChedA2Info.PUSH_SIDE);
            vec.setY(ChedA2Info.PUSH_UP);

            combatUser.getMoveModule().push(vec, true);
        }, 1, 2));

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, combatUser.getEntity().getLocation(), 1, 0, 0, 0, 0.05);
            return true;
        }, 1, 10));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
