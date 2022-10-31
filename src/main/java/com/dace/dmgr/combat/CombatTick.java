package com.dace.dmgr.combat;

import com.dace.dmgr.combat.action.Reloadable;
import com.dace.dmgr.combat.action.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.StringJoiner;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class CombatTick {
    public static final int IDLE_ULT_CHARGE = 10;
    public static final float BASE_SPEED = 0.24F;

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

                if (CooldownManager.getCooldown(combatUser, Cooldown.NO_SPRINT) == 0 &&
                        CooldownManager.getCooldown(combatUser, Cooldown.WEAPON_RELOAD) == 0)
                    combatUser.allowSprint(true);
                else
                    combatUser.allowSprint(false);

                if (i % 10 == 0) {
                    UltimateSkill ultimateSkill = combatUser.getCharacter().getUltimate();

                    combatUser.addUlt((float) IDLE_ULT_CHARGE / ultimateSkill.getCost() / 2);
                }

                float speedMultiplier = combatUser.getCharacter().getSpeed() * (100 + combatUser.getSpeedIncrement()) / 100;
                float speed = BASE_SPEED * speedMultiplier;

                if (combatUser.getEntity().isSprinting())
                    speed *= 0.88;
                else
                    speed *= speed / BASE_SPEED;
                combatUser.getEntity().setWalkSpeed(speed);

                showActionbar(combatUser);

                return true;
            }
        };
    }

    private static void showActionbar(CombatUser combatUser) {
        if (combatUser.getCharacter().getWeapon() instanceof Reloadable) {
            int capacity = combatUser.getWeaponController().getRemainingAmmo();
            int maxCapacity = ((Reloadable) combatUser.getCharacter().getWeapon()).getCapacity();

            StringJoiner text = new StringJoiner("    ");

            String ammo = getContext(TextIcon.CAPACITY, capacity, maxCapacity, maxCapacity, '|');

            text.add(ammo);

            combatUser.getEntity().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(text.toString()));
        }
    }

    private static String getContext(char icon, int current, int max, int length, char symbol) {
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
                .add(StringUtil.getBar(current, max, color, length, symbol))
                .add(new StringJoiner("§f/", "[", "]")
                        .add(color + currentDisplay)
                        .add(maxDisplay)
                        .toString())
                .toString();
    }
}
