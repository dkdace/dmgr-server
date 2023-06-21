package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasDuration;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class ArkaceA2 extends Skill implements HasDuration {
    public ArkaceA2(CombatUser combatUser) {
        super(2, combatUser, ArkaceA2Info.getInstance(), 2);
    }

    @Override
    public long getCooldown() {
        return ArkaceA2Info.COOLDOWN;
    }

    @Override
    public long getDuration() {
        return ArkaceA2Info.DURATION;
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (!isUsing()) {
            use();

            Location location = combatUser.getEntity().getLocation();
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, location, 1.5F, 0.9F);
            SoundUtil.play(Sound.ITEM_ARMOR_EQUIP_DIAMOND, location, 1.5F, 1.4F);
            SoundUtil.play(Sound.ITEM_ARMOR_EQUIP_DIAMOND, location, 1.5F, 1.2F);

            new TaskTimer(1, ArkaceA2Info.DURATION) {
                @Override
                public boolean run(int i) {
                    Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
                    loc.setPitch(0);
                    Vector vector = VectorUtil.getRollAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec1 = VectorUtil.getRotatedVector(vector, axis, i * 10);
                    Vector vec2 = VectorUtil.getRotatedVector(vector, axis, i * 10 + 120);
                    Vector vec3 = VectorUtil.getRotatedVector(vector, axis, i * 10 + 240);
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec1), 3,
                            0, 0.4F, 0, 220, 255, 36);
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec2), 3,
                            0, 0.4F, 0, 190, 255, 36);
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec3), 3,
                            0, 0.4F, 0, 160, 255, 36);

                    int amount = (int) (ArkaceA2Info.HEAL / ArkaceA2Info.DURATION);
                    if (i == 0)
                        amount += (int) (ArkaceA2Info.HEAL % ArkaceA2Info.DURATION);
                    combatUser.heal(combatUser, amount, true);
                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    if (cancelled)
                        use();
                }
            };
        }
    }
}
