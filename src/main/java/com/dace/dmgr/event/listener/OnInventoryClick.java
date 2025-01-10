package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnInventoryClick extends EventListener<InventoryClickEvent> {
    @Getter
    private static final OnInventoryClick instance = new OnInventoryClick();
    /** GUI 클릭 효과음 */
    private static final SoundEffect GUI_CLICK_SOUND = new SoundEffect(SoundEffect.SoundInfo.builder(Sound.UI_BUTTON_CLICK).build());

    @Override
    @EventHandler
    protected void onEvent(@NonNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser != null)
            event.setCancelled(true);

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        StaticItem staticItem = StaticItem.fromItemStack(itemStack);
        if (staticItem == null)
            return;

        event.setCancelled(true);

        if (staticItem instanceof GuiItem && event.getClick() != ClickType.DOUBLE_CLICK
                && ((GuiItem) staticItem).onClick(event.getClick(), itemStack, player))
            GUI_CLICK_SOUND.play(player);
    }
}
