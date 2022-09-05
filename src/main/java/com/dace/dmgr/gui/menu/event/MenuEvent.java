package com.dace.dmgr.gui.menu.event;

import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class MenuEvent {
    private final String menuName;

    protected MenuEvent(String menuName) {
        this.menuName = menuName;
    }

    private String getItemName(ItemStack item) {
        return ChatColor.stripColor(item.getItemMeta().getDisplayName());
    }

    private String getInventoryName(InventoryClickEvent event) {
        return ChatColor.stripColor(event.getClickedInventory().getName());
    }

    private boolean isClickable(ItemStack item) {
        return item.getType() != Material.AIR && !getItemName(item).isEmpty();
    }

    public void event(InventoryClickEvent event, Player player) {
        if (event.getClickedInventory() != null) {
            if (getInventoryName(event).equals(menuName)) {
                event.setCancelled(true);

                if (isClickable(event.getCurrentItem()))
                    onMenuClick(event, player, event.getCurrentItem(), getItemName(event.getCurrentItem()));
            }
        }
    }

    protected void playClickSound(Player player) {
        SoundPlayer.play(Sound.UI_BUTTON_CLICK, player, 1F, 1F);
    }

    protected abstract void onMenuClick(InventoryClickEvent event, Player player, ItemStack clickItem, String clickItemName);
}
