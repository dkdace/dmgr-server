package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskTimer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.dace.dmgr.system.HashMapList.combatUserHashMap;

public class CombatTick {
    public static final int IDLE_ULT_CHARGE = 10;
    public static final float BASE_SPEED = 0.23F;

    public static void run(CombatUser combatUser) {
        Player player = combatUser.getEntity();

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatUserHashMap.get(player) == null)
                    return false;

                if (player.getPotionEffect(PotionEffectType.WATER_BREATHING) == null)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                            99999, 0, false, false));

                if (i % 10 == 0) {
                    combatUser.addUlt((float) IDLE_ULT_CHARGE / combatUser.getCharacter().getCharacterStats().getActive(4).getCost() / 2);

                    if (combatUser.getUlt() == 1 && !combatUser.getActiveSkillController(3).isUsing())
                        combatUser.getActiveSkillController(3).ultimateCharge();
                }

                float speedMultiplier = combatUser.getCharacter().getCharacterStats().getSpeed() * (100 + combatUser.getSpeedIncrement()) / 100;
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
