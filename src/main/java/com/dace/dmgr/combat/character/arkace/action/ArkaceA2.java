package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class ArkaceA2 extends ActiveSkill {
    public ArkaceA2(CombatUser combatUser) {
        super(2, combatUser, ArkaceA2Info.getInstance(), 2);
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
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
    public void onUse(ActionKey actionKey) {
        setDuration();
        playUseSound(combatUser.getEntity().getLocation());

        TaskManager.addTask(this, new TaskTimer(1, ArkaceA2Info.DURATION) {
            @Override
            public boolean onTimerTick(int i) {
                Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
                loc.setPitch(0);
                playTickEffect(i, loc);

                int amount = (int) (ArkaceA2Info.HEAL / ArkaceA2Info.DURATION);
                if (i == 0)
                    amount += (int) (ArkaceA2Info.HEAL % ArkaceA2Info.DURATION);
                combatUser.getDamageModule().heal(combatUser, amount, true);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                if (cancelled)
                    setDuration(0);
            }
        });
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, location, 1.5F, 0.9F);
        SoundUtil.play(Sound.ITEM_ARMOR_EQUIP_DIAMOND, location, 1.5F, 1.4F);
        SoundUtil.play(Sound.ITEM_ARMOR_EQUIP_DIAMOND, location, 1.5F, 1.2F);
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i        인덱스
     * @param location 사용 위치
     */
    private void playTickEffect(int i, Location location) {
        Vector vector = VectorUtil.getRollAxis(location);
        Vector axis = VectorUtil.getYawAxis(location);

        Vector vec1 = VectorUtil.getRotatedVector(vector, axis, i * 10);
        Vector vec2 = VectorUtil.getRotatedVector(vector, axis, i * 10 + 120);
        Vector vec3 = VectorUtil.getRotatedVector(vector, axis, i * 10 + 240);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().add(vec1), 3,
                0, 0.4F, 0, 220, 255, 36);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().add(vec2), 3,
                0, 0.4F, 0, 190, 255, 36);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().add(vec3), 3,
                0, 0.4F, 0, 160, 255, 36);
    }
}
