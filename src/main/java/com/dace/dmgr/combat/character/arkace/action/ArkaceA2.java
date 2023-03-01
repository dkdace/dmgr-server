package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.ActiveSkill;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class ArkaceA2 extends ActiveSkill {
    /** 쿨타임 */
    public static final int COOLDOWN = 12 * 20;
    /** 치유량 */
    public static final int HEAL = 350;
    /** 지속시간 */
    public static final long DURATION = (long) (2.5 * 20);
    private static final ArkaceA2 instance = new ArkaceA2();

    public ArkaceA2() {
        super(2, "생체 회복막",
                "",
                "§6" + TextIcon.DURATION + " 지속시간§f동안 회복막을 활성화하여 §a" + TextIcon.HEAL + " 회복§f합니다.",
                "",
                "§6" + TextIcon.DURATION + "§f 2.5초",
                "§a" + TextIcon.HEAL + "§f 350",
                "§f" + TextIcon.COOLDOWN + "§f 12초",
                "",
                "§7§l[3] §f사용");
    }

    public static ArkaceA2 getInstance() {
        return instance;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public void use(CombatUser combatUser, SkillController skillController, ActionKey actionKey) {
        if (!skillController.isUsing()) {
            skillController.setDuration(DURATION);

            Location location = combatUser.getEntity().getLocation();
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, location, 1.5F, 0.9F);
            SoundUtil.play(Sound.ITEM_ARMOR_EQUIP_DIAMOND, location, 1.5F, 1.4F);
            SoundUtil.play(Sound.ITEM_ARMOR_EQUIP_DIAMOND, location, 1.5F, 1.2F);

            new TaskTimer(1, DURATION) {
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

                    int amount = (int) (HEAL / DURATION);
                    if (i == 0)
                        amount += (int) (HEAL % DURATION);
                    Combat.heal(combatUser, combatUser, amount, true);
                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    skillController.setCooldown();
                }
            };
        }
    }
}
