package com.dace.dmgr.combat;

import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.action.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskTimer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class CombatTick {
    public static final int IDLE_ULT_CHARGE = 10;
    public static final float BASE_SPEED = 0.23F;

    public static void run(CombatUser combatUser) {
        Player player = combatUser.getEntity();

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatUserMap.get(player) == null)
                    return false;

                if (player.getPotionEffect(PotionEffectType.WATER_BREATHING) == null)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                            99999, 0, false, false));

                if (i % 10 == 0) {
                    UltimateSkill ultimateSkill = combatUser.getCharacter().getUltimate();
                    SkillController skillController = new SkillController(combatUser, ultimateSkill, 3);

                    combatUser.addUlt((float) IDLE_ULT_CHARGE / ultimateSkill.getCost() / 2);
                }

                float speedMultiplier = combatUser.getCharacter().getSpeed() * (100 + combatUser.getSpeedIncrement()) / 100;
                float speed = BASE_SPEED * speedMultiplier;

                if (combatUser.getEntity().isSprinting())
                    speed *= 0.88;
                else
                    speed *= speed / BASE_SPEED;
                combatUser.getEntity().setWalkSpeed(speed);

                return true;
            }
        };
    }
}
