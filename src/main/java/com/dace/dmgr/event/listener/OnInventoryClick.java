package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class OnInventoryClick implements Listener {
    @EventHandler
    public static void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser != null)
            event.setCancelled(true);
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        StaticItem staticItem = StaticItem.fromItemStack(itemStack);
        if (!(staticItem instanceof GuiItem))
            return;

        event.setCancelled(true);
        if (event.getClick() == ClickType.DOUBLE_CLICK)
            return;

        if (((GuiItem) staticItem).onClick(event.getClick(), itemStack, player))
            SoundUtil.playNamedSound(NamedSound.GENERAL_GUI_CLICK, player);
    }
}
