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

/**
 * 전투 시스템에 사용되는 기능을 제공하는 클래스.
 */
public class CombatUtil {
    /**
     * 지정한 플레이어에게 이동 패킷을 전송한다.
     *
     * @param player 대상 플레이어
     * @param yaw    변경할 yaw
     * @param pitch  변경할 pitch
     */
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

    /**
     * 지정한 플레이어에게 화면 반동 효과를 전송한다. 총기 반동에 사용된다.
     *
     * @param combatUser      대상 플레이어
     * @param up              수직 반동
     * @param side            수평 반동
     * @param upSpread        수직 반동 분산도
     * @param sideSpread      수평 반동 분산도
     * @param ticks           반동 진행 시간
     * @param firstMultiplier 초탄 반동 계수
     */
    public static void sendRecoil(CombatUser combatUser, float up, float side, float upSpread, float sideSpread, int ticks, float firstMultiplier) {
        final float finalUpSpread = (float) (upSpread * (Math.random() - Math.random()) * 0.5);
        final float finalSideSpread = (float) (sideSpread * (Math.random() - Math.random()) * 0.5);
        final boolean first = CooldownManager.getCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY) == 0;
        CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_FIRST_RECOIL_DELAY);

        int sum = 0;
        for (int i = 1; i <= ticks; i++) {
            sum += i;
        }

        final int finalSum = sum;

        new TaskTimer(1, ticks) {
            @Override
            public boolean run(int i) {
                float finalUp = (up + finalUpSpread) / ((float) finalSum / (ticks - i));
                float finalSide = (side + finalSideSpread) / ((float) finalSum / (ticks - i));
                if (first) {
                    finalUp *= firstMultiplier;
                    finalSide *= firstMultiplier;
                }
                sendPacket(combatUser.getEntity(), finalSide, -finalUp);

                return true;
            }
        };
    }

    /**
     * 지정한 플레이어에게 총기 탄퍼짐 시스템을 적용한다.
     *
     * @param combatUser 대상 플레이어
     * @param increment  탄퍼짐 증가량
     * @param recovery   탄퍼짐 회복량
     * @param max        탄퍼짐 최대치
     */
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
