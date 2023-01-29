package com.dace.dmgr.gui;

import com.dace.dmgr.gui.item.DisplayItem;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * 인벤토리(GUI) 기능을 제공하는 클래스.
 *
 * <p>GUI를 구현하려면 해당 클래스를 상속받아 구현할 수 있다.</p>
 */
public abstract class Gui implements Listener {
    /** 해당 GUI 인벤토리 객체 */
    private final Inventory inventory;

    /**
     * 행 크기와 이름을 지정하여 메뉴 인스턴스를 생성한다.
     *
     * @param rowSize 행 크기. {@code 1 ~ 6} 사이의 값
     * @param name    GUI 이름
     */
    public Gui(int rowSize, String name) {
        if (rowSize > 6) rowSize = 6;
        if (rowSize < 1) rowSize = 1;
        inventory = Bukkit.createInventory(null, rowSize * 9, name);
    }

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * 지정한 아이템으로 모든 칸을 채운다.
     *
     * @param itemStack 대상 아이템
     */
    protected void fillAll(ItemStack itemStack) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, itemStack);
        }
    }

    /**
     * 지정한 아이템으로 특정 행을 전부 채운다.
     *
     * @param row       행 번호
     * @param itemStack 대상 아이템
     */
    protected void fillRow(int row, ItemStack itemStack) {
        for (int i = 0; i < 9; i++) {
            inventory.setItem((row - 1) * 9 + i, itemStack);
        }
    }

    /**
     * 지정한 칸에 클릭하여 활성화할 수 있는 버튼 아이템을 배치한다.
     *
     * @param index     칸 번호
     * @param itemStack 대상 아이템
     * @param isEnabled 활성화 여부. {@code true}로 지정하면 아이템 설명에 '켜짐',
     *                  {@code false}면 '꺼짐'이 추가된다.
     */
    protected void setToggleButton(int index, ItemStack itemStack, boolean isEnabled) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        String text;
        if (isEnabled)
            text = "§a§l켜짐";
        else
            text = "§c§l꺼짐";

        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            lore.add(text);
            itemMeta.setLore(lore);
        } else
            itemMeta.setLore(Arrays.asList(text));
        itemStack.setItemMeta(itemMeta);

        inventory.setItem(index, itemStack);
    }

    /**
     * 지정한 칸에 클릭하여 활성화할 수 있는 버튼 아이템과 활성화 여부를 표시하는 아이템을 배치한다.
     *
     * @param index        칸 번호
     * @param itemStack    대상 아이템
     * @param isEnabled    활성화 여부. {@code true}로 지정하면 아이템 설명에 '켜짐',
     *                     {@code false}면 '꺼짐'이 추가된다.
     * @param displayIndex 활성화 여부를 표시할 칸 번호.
     */
    protected void setToggleButton(int index, ItemStack itemStack, boolean isEnabled, int displayIndex) {
        setToggleButton(index, itemStack, isEnabled);
        if (isEnabled)
            inventory.setItem(displayIndex, DisplayItem.ENABLED.getItemStack());
        else
            inventory.setItem(displayIndex, DisplayItem.DISABLED.getItemStack());
    }

    /**
     * 지정한 칸에 클릭하여 선택할 수 있는 아이템을 배치한다.
     *
     * @param index      칸 번호
     * @param itemStack  대상 아이템
     * @param isSelected 선택 여부. {@code true}로 지정하면 아이템 설명에 '선택됨'이 추가된다.
     */
    protected void setSelectButton(int index, ItemStack itemStack, boolean isSelected) {
        if (isSelected) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String text = "§a§l선택됨";

            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();
                lore.add(text);
                itemMeta.setLore(lore);
            } else
                itemMeta.setLore(Arrays.asList(text));
            itemStack.setItemMeta(itemMeta);
        }

        inventory.setItem(index, itemStack);
    }

    /**
     * 플레이어에게 GUI 인벤토리를 표시한다.
     *
     * @param player 대상 플레이어
     */
    public void open(Player player) {
        player.openInventory(inventory);
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
    private void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().equals(inventory.getTitle())) {
            event.setCancelled(true);

            if (isClickable(event.getCurrentItem())) {
                SoundUtil.play(Sound.UI_BUTTON_CLICK, 1F, 1F, player);

                onClick(event, player, getItemName(event.getCurrentItem()));
            }
        }
    }

    /**
     * GUI 아이템 클릭 시 호출되는 이벤트.
     *
     * @param event         이벤트 객체
     * @param player        클릭한 플레이어
     * @param clickItemName 클릭한 아이템의 이름
     */
    protected abstract void onClick(InventoryClickEvent event, Player player, String clickItemName);
}
