package com.dace.dmgr.combat;

import com.dace.dmgr.DMGR;
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

                if (combatUserList.get(combatUser.getPlayer().getUniqueId()) == null)
                    cancel();

                if (combatUser.getPlayer().getPotionEffect(PotionEffectType.WATER_BREATHING) == null)
                    combatUser.getPlayer().addPotionEffect(
                            new PotionEffect(PotionEffectType.WATER_BREATHING, 99999, 0, false, false));

                if (i % 20 == 0) {
                    System.out.println(combatUser.getPlayer().getPotionEffect(PotionEffectType.WATER_BREATHING));
                    combatUser.getPlayer().sendMessage("foo!");
                }
            }
        }.runTaskTimer(DMGR.getPlugin(), 1, 1);
    }
}
