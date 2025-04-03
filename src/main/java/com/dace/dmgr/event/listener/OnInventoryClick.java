package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.gui.ChestGUI;
import com.dace.dmgr.item.gui.GUI;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnInventoryClick extends EventListener<InventoryClickEvent> {
    @Getter
    private static final OnInventoryClick instance = new OnInventoryClick();
    /** GUI 클릭 성공 효과음 */
    private static final SoundEffect GUI_CLICK_PASS_SOUND = new SoundEffect(SoundEffect.SoundInfo.builder(Sound.UI_BUTTON_CLICK).build());
    /** GUI 클릭 실패 효과음 */
    private static final SoundEffect GUI_CLICK_FAIL_SOUND = new SoundEffect(SoundEffect.SoundInfo.builder("new.block.note_block.bit").pitch(0.7).build());

    @Override
    @EventHandler
    protected void onEvent(@NonNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        User user = User.fromPlayer(player);
        CombatUser combatUser = CombatUser.fromUser(user);

        if (combatUser != null)
            event.setCancelled(true);

        Inventory inventory = event.getClickedInventory();
        ItemStack itemStack = event.getCurrentItem();
        if (inventory == null || itemStack == null || itemStack.getType() == Material.AIR)
            return;

        GUI gui = inventory == player.getInventory() ? user.getGui() : ChestGUI.fromInventory(inventory);
        if (gui == null)
            return;

        DefinedItem definedItem = gui.get(event.getSlot());
        if (definedItem == null)
            return;

        event.setCancelled(true);

        DefinedItem.ClickHandler clickHandler = definedItem.getClickHandler(event.getClick());
        if (clickHandler != null)
            (clickHandler.onClick.test(player) ? GUI_CLICK_PASS_SOUND : GUI_CLICK_FAIL_SOUND).play(player);
    }
}
