package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * 차단 목록 GUI 클래스.
 */
public final class BlockList extends Gui {
    private static final GuiItem playerInto = new GuiItem("BlockListPlayer", new ItemBuilder(Material.SKULL_ITEM)
            .setDamage((short) 3)
            .setLore("§f클릭 시 차단을 해제합니다.")
            .build()) {
        @Override
        public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
            player.performCommand("차단 " + ((SkullMeta) clickItem.getItemMeta()).getOwningPlayer().getName());
            player.closeInventory();
            return true;
        }
    };
    @Getter
    private static final BlockList instance = new BlockList();

    private BlockList() {
        super(3, "§8차단 목록");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        UserData userData = UserData.fromPlayer(player);

        UserData[] blockedPlayers = userData.getBlockedPlayers();
        new AsyncTask<Void>((onFinish, onError) -> {
            for (int i = 0; i < blockedPlayers.length; i++) {
                int index = i;
                guiController.set(i, playerInto, itemBuilder -> ((SkullMeta) itemBuilder.setName(blockedPlayers[index].getDisplayName()).getItemMeta())
                        .setOwningPlayer(Bukkit.getOfflinePlayer(blockedPlayers[index].getPlayerUUID())));
            }
        });
    }
}
