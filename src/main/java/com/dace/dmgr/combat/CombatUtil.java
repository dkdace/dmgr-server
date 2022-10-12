package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class CombatUtil {
    private static void sendPacket(Player player, float yaw, float pitch) {
        WrapperPlayServerPosition packet = new WrapperPlayServerPosition();

        packet.setX(0);
        packet.setY(0);
        packet.setZ(0);
        packet.setYaw(yaw);
        packet.setPitch(pitch);
        packet.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.values())));

        packet.sendPacket(player);
    }

    public static void sendRecoil(CombatUser combatUser, float up, float side, float upSpread, float sideSpread, int ticks, float firstMultiplier) {
        final float _upSpread = (float) (upSpread * (Math.random() - Math.random()) * 0.5);
        final float _sideSpread = (float) (sideSpread * (Math.random() - Math.random()) * 0.5);
        final boolean first = CooldownManager.getCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY) == 0;
        CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY);

        int sum = 0;
        for (int i = 1; i <= ticks; i++) {
            sum += i;
        }
        final int _sum = sum;

        new TaskTimer(1, ticks) {
            @Override
            public boolean run(int i) {
                float _up = (up + _upSpread) / ((float) _sum / (ticks - i));
                float _side = (side + _sideSpread) / ((float) _sum / (ticks - i));
                if (first) {
                    _up *= firstMultiplier;
                    _side *= firstMultiplier;
                }
                sendPacket(combatUser.getEntity(), _side, -_up);

                return true;
            }
        };
    }

    public static void applyBulletSpread(CombatUser combatUser, float increment, float recovery, float max) {
        if (combatUser.getBulletSpread() == 0) {
            combatUser.addBulletSpread(increment, max);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    if (combatUserMap.get(combatUser.getEntity()) == null)
                        return false;

                    if (CooldownManager.getCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY) == 0) {
                        if (combatUser.getBulletSpread() == 0)
                            return false;
                        combatUser.addBulletSpread(-recovery, max);
                    }
                    return true;
                }
            };
        } else
            combatUser.addBulletSpread(increment, max);
    }
}
