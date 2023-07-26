package com.dace.dmgr.gui;

import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * 인벤토리(GUI) 기능을 제공하는 클래스.
 *
 * <p>GUI를 구현하려면 해당 클래스를 상속받아 구현할 수 있다.</p>
 */
public abstract class Gui implements Listener {
    /** 행 크기 */
    private final int rowSize;
    /** GUI 이름 */
    private final String name;

    /**
     * 행 크기와 이름을 지정하여 메뉴 인스턴스를 생성한다.
     *
     * @param rowSize 행 크기. {@code 1 ~ 6} 사이의 값
     * @param name    GUI 이름
     */
    protected Gui(int rowSize, String name) {
        if (rowSize > 6) rowSize = 6;
        if (rowSize < 1) rowSize = 1;
        this.rowSize = rowSize;
        this.name = name;
    }

    /**
     * 플레이어에게 GUI 인벤토리를 표시한다.
     *
     * @param player 대상 플레이어
     */
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, rowSize * 9, name);
        player.openInventory(inventory);
        onOpen(player, inventory);
    }

    /**
     * 지정한 아이템의 표시 이름을 반환한다.
     *
     * @param item 대상 아이템
     * @return 아이템 표시 이름
     */
    private String getItemName(ItemStack item) {
        return ChatColor.stripColor(item.getItemMeta().getDisplayName());
    }

    /**
     * 지정한 아이템을 클릭할 수 있는 지 확인한다.
     *
     * @param item 확인할 아이템
     * @return 클릭 가능 여부
     */
    private boolean isClickable(ItemStack item) {
        return item.getType() != Material.AIR && !getItemName(item).isEmpty();
    }

    @EventHandler
    public void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().equals(name)) {
            event.setCancelled(true);

            if (isClickable(event.getCurrentItem())) {
                SoundUtil.play(Sound.UI_BUTTON_CLICK, 1F, 1F, player);

                onClick(event, player, getItemName(event.getCurrentItem()));
            }
        }
    }

    /**
     * {@link Gui#open(Player)} 호출 시 호출되는 이벤트.
     *
     * @param player    대상 플레이어
     * @param inventory 해당 인벤토리
     */
    protected abstract void onOpen(Player player, Inventory inventory);

    /**
     * GUI 아이템 클릭 시 호출되는 이벤트.
     *
     * @param event         이벤트 객체
     * @param player        클릭한 플레이어
     * @param clickItemName 클릭한 아이템의 이름
     */
    protected abstract void onClick(InventoryClickEvent event, Player player, String clickItemName);
}
