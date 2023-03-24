package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerWorldBorder;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.combat.action.Reloadable;
import com.dace.dmgr.combat.action.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringFormUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.StringJoiner;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

/**
 * 전투 스케쥴러를 제공하는 클래스.
 */
public class CombatTick {
    /** 초당 궁극기 충전량 */
    public static final int IDLE_ULT_CHARGE = 10;
    /** 기본 이동속도 */
    public static final float BASE_SPEED = 0.24F;

    /**
     * 플레이어별 전투 스케쥴러를 실행한다.
     *
     * @param combatUser 대상 플레이어
     */
    public static void run(CombatUser combatUser) {
        Player player = combatUser.getEntity();

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatUserMap.get(player) == null)
                    return false;

                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                        99999, 0, false, false), true);

                combatUser.allowSprint(canSprint(combatUser));

                if (canJump(combatUser))
                    player.removePotionEffect(PotionEffectType.JUMP);
                else
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
                            9999, -6, false, false), true);

                if (i % 10 == 0) {
                    UltimateSkill ultimateSkill = combatUser.getCharacter().getUltimate();

                    combatUser.addUlt((float) IDLE_ULT_CHARGE / ultimateSkill.getCost() / 2);
                }

                if (combatUser.getHealth() <= combatUser.getMaxHealth() / 4) {
                    combatUser.playBleedingEffect(1);
                    playLowHealthScreenEffect(player, true);
                } else
                    playLowHealthScreenEffect(player, false);

                float speedMultiplier = combatUser.getCharacter().getSpeed() * (100 + combatUser.getSpeedIncrement()) / 100;
                float speed = BASE_SPEED * speedMultiplier;

                if (combatUser.getEntity().isSprinting())
                    speed *= 0.88;
                else
                    speed *= speed / BASE_SPEED;
                if (!canMove(combatUser))
                    speed = 0.0001F;

                if (combatUser.getWeaponController().isAiming())
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                            99999, 5, false, false), true);
                else
                    player.removePotionEffect(PotionEffectType.SLOW);

                combatUser.getEntity().setWalkSpeed(speed);

                showActionbar(combatUser);

                return true;
            }
        };
    }

    /**
     * 엔티티가 움직일 수 있는 지 확인한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 이동 가능 여부
     */
    private static boolean canMove(ICombatEntity combatEntity) {
        if (CooldownManager.getCooldown(combatEntity, Cooldown.STUN) > 0 || CooldownManager.getCooldown(combatEntity, Cooldown.SNARE) > 0)
            return false;

        return true;
    }

    /**
     * 플레이어가 달리기를 할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 달리기 가능 여부
     */
    private static boolean canSprint(CombatUser combatUser) {
        if (CooldownManager.getCooldown(combatUser, Cooldown.STUN) > 0 || CooldownManager.getCooldown(combatUser, Cooldown.SNARE) > 0 ||
                CooldownManager.getCooldown(combatUser, Cooldown.GROUNDING) > 0)
            return false;
        if (CooldownManager.getCooldown(combatUser, Cooldown.NO_SPRINT) > 0)
            return false;

        return true;
    }

    /**
     * 엔티티가 점프할 수 있는 지 확인한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 점프 가능  여부
     */
    private static boolean canJump(ICombatEntity combatEntity) {
        if (CooldownManager.getCooldown(combatEntity, Cooldown.STUN) > 0 || CooldownManager.getCooldown(combatEntity, Cooldown.SNARE) > 0 ||
                CooldownManager.getCooldown(combatEntity, Cooldown.GROUNDING) > 0)
            return false;

        return true;
    }

    /**
     * 플레이어에게 치명상 화면 효과를 표시한다.
     *
     * @param player 대상 플레이어
     * @param toggle 활성화 여부
     */
    public static void playLowHealthScreenEffect(Player player, boolean toggle) {
        WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();

        packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
        packet.setWarningDistance(toggle ? 999999999 : 0);

        packet.sendPacket(player);
    }

    /**
     * 플레이어에게 전체 액션바를 표시한다.
     *
     * @param combatUser 대상 플레이어
     */
    private static void showActionbar(CombatUser combatUser) {
        if (combatUser.getCharacter().getWeapon() instanceof Reloadable &&
                CooldownManager.getCooldown(combatUser, Cooldown.ACTION_BAR) == 0) {
            int capacity = combatUser.getWeaponController().getRemainingAmmo();
            int maxCapacity = ((Reloadable) combatUser.getCharacter().getWeapon()).getCapacity();

            StringJoiner text = new StringJoiner("    ");

            String ammo = getActionbarProgressBar(TextIcon.CAPACITY, capacity, maxCapacity, maxCapacity, '|');

            text.add(ammo);

            combatUser.sendActionBar(text.toString());
        }
    }

    /**
     * 액션바에 사용되는 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>[아이콘] ■■■■■□□□□□ [5/10]</pre>
     *
     * @param icon    아이콘
     * @param current 현재 값
     * @param max     최대 값
     * @param length  막대 길이 (글자 수)
     * @param symbol  막대 기호
     * @return 액션바 진행 막대 문자열
     */
    private static String getActionbarProgressBar(char icon, int current, int max, int length, char symbol) {
        ChatColor color;
        if (current <= max / 4)
            color = ChatColor.RED;
        else if (current <= max / 2)
            color = ChatColor.YELLOW;
        else
            color = ChatColor.WHITE;

        String currentDisplay = String.format("%" + (int) (Math.log10(max) + 1) + "d", current);
        String maxDisplay = Integer.toString(max);

        return new StringJoiner(" §f")
                .add(String.valueOf(icon))
                .add(StringFormUtil.getProgressBar(current, max, color, length, symbol))
                .add(new StringJoiner("§f/", "[", "]")
                        .add(color + currentDisplay)
                        .add(maxDisplay)
                        .toString())
                .toString();
    }
}
