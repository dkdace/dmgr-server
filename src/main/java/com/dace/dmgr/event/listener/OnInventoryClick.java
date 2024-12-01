package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.DefinedSound;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class OnInventoryClick implements Listener {
    /** GUI 클릭 효과음 */
    private static final DefinedSound GUI_CLICK_SOUND = new DefinedSound(
            new DefinedSound.SoundEffect(Sound.UI_BUTTON_CLICK, 1, 1));

    @EventHandler
    public static void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
        ItemStack itemStack = event.getCurrentItem();

        if (combatUser != null)
            event.setCancelled(true);
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        StaticItem staticItem = StaticItem.fromItemStack(itemStack);
        if (staticItem != null) {
            event.setCancelled(true);

            if (staticItem instanceof GuiItem && event.getClick() != ClickType.DOUBLE_CLICK &&
                    ((GuiItem) staticItem).onClick(event.getClick(), itemStack, player))
                GUI_CLICK_SOUND.play(player);
        }
    }
}
