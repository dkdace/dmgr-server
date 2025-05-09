package com.dace.dmgr.menu;

import com.dace.dmgr.command.BlockCommand;
import com.dace.dmgr.item.ChestGUI;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * 차단 목록 GUI 클래스.
 *
 * @see BlockCommand
 */
public final class BlockList extends ChestGUI {
    /**
     * 차단 목록 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public BlockList(@NonNull Player player) {
        super(3, "§8차단 목록", player);

        Iterator<UserData> iterator = UserData.fromPlayer(player).getBlockedPlayers().iterator();

        for (int i = 0; iterator.hasNext() && i < inventory.getSize(); i++) {
            int index = i;
            UserData blockedPlayer = iterator.next();

            blockedPlayer.getProfileItem().onFinish((Consumer<ItemStack>) itemStack ->
                    set(index, new DefinedItem(new ItemBuilder(itemStack)
                            .setLore("§f클릭 시 차단을 해제합니다.")
                            .build(),
                            new DefinedItem.ClickHandler(ClickType.LEFT, target -> {
                                target.performCommand("차단 " + blockedPlayer.getPlayerName());
                                target.closeInventory();

                                return true;
                            }))));
        }
    }
}