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

import java.util.Iterator;
import java.util.Set;

/**
 * 차단 목록 GUI 클래스.
 */
public final class BlockList extends Gui {
    /** 차단된 플레이어 GUI 아이템 객체 */
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

        Set<UserData> blockedPlayers = userData.getBlockedPlayers();
        new AsyncTask<Void>((onFinish, onError) -> {
            int i = 0;
            for (Iterator<UserData> iterator = blockedPlayers.iterator(); i < blockedPlayers.size() && iterator.hasNext(); i++) {
                UserData blockedPlayer = iterator.next();
                guiController.set(i, playerInto, itemBuilder -> itemBuilder.setName(blockedPlayer.getDisplayName())
                        .setSkullOwner(Bukkit.getOfflinePlayer(blockedPlayer.getPlayerUUID())));
            }
        });
    }
}
