package com.dace.dmgr.combat;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class CombatTick {
    public static final int IDLE_ULT_CHARGE = 10;

    public static void run(CombatUser combatUser) {
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                i++;

                if (combatUserList.get(combatUser.getEntity().getUniqueId()) == null)
                    cancel();

                if (combatUser.getEntity().getPotionEffect(PotionEffectType.WATER_BREATHING) == null)
                    combatUser.getEntity().addPotionEffect(
                            new PotionEffect(PotionEffectType.WATER_BREATHING, 99999, 0, false, false));

                if (i % 10 == 0)
                    combatUser.addUlt((float) IDLE_ULT_CHARGE / combatUser.getCharacter().getStats().getUltimate().getCost() / 2);
            }
        }.runTaskTimer(DMGR.getPlugin(), 1, 1);
    }
}
