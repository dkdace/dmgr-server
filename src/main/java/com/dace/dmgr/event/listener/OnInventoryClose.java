package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.item.gui.ChestGUI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnInventoryClose extends EventListener<InventoryCloseEvent> {
    @Getter
    private static final OnInventoryClose instance = new OnInventoryClose();

    @Override
    @EventHandler
    protected void onEvent(@NonNull InventoryCloseEvent event) {
        ChestGUI gui = ChestGUI.fromInventory(event.getInventory());
        if (gui != null)
            gui.onClose();
    }
}
