package com.dace.dmgr.item.gui;

import com.dace.dmgr.event.EventUtil;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * GUI(인벤토리) 기능을 제공하는 클래스.
 *
 * <p>해당 클래스를 상속받아 GUI를 구현할 수 있다.</p>
 */
public abstract class Gui implements Listener {
    /** 행 크기 */
    private final int rowSize;
    /** GUI 이름 */
    @NonNull
    private final String name;

    /**
     * 행 크기와 이름을 지정하여 GUI 인스턴스를 생성한다.
     *
     * @param rowSize 행 크기. 1~6 사이의 값
     * @param name    GUI 이름
     * @throws IllegalArgumentException {@code rowSize}가 1~6 사이가 아니면 발생
     */
    protected Gui(int rowSize, @NonNull String name) {
        if (rowSize < 1 || rowSize > 6)
            throw new IllegalArgumentException("'rowSize'가 1에서 6 사이여야 함");
        this.rowSize = rowSize;
        this.name = name;

        EventUtil.registerListener(this);
    }

    /**
     * 플레이어에게 GUI 인벤토리를 표시한다.
     *
     * @param player 대상 플레이어
     */
    public final void open(@NonNull Player player) {
        Inventory inventory = Bukkit.createInventory(player, rowSize * 9, name);
        player.openInventory(inventory);
        onOpen(player, new GuiController(inventory));
    }

    @EventHandler
    public final void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        if (event.getInventory().getTitle().equals(name)) {
            event.setCancelled(true);

            StaticItem<?> staticItem = StaticItem.fromItemStack(itemStack);
            if (!(staticItem instanceof GuiItem) || event.getClick() == ClickType.DOUBLE_CLICK)
                return;
            if (((GuiItem<?>) staticItem).getGui() != null && ((GuiItem<?>) staticItem).getGui() != this)
                return;

            if (((GuiItem<?>) staticItem).isClickable()) {
                SoundUtil.playNamedSound(NamedSound.GENERAL_GUI_CLICK, player);

                onClick(event, player, (GuiItem<?>) staticItem);
            }
        }
    }

    /**
     * GUI를 열었을 때 실행할 작업.
     *
     * @param player        대상 플레이어
     * @param guiController GUI 컨트롤러 객체
     */
    protected abstract void onOpen(@NonNull Player player, @NonNull GuiController guiController);

    /**
     * GUI 아이템 클릭 시 실행할 작업.
     *
     * @param event   이벤트 객체
     * @param player  클릭한 플레이어
     * @param guiItem 클릭한 GUI 아이템
     */
    protected abstract void onClick(InventoryClickEvent event, @NonNull Player player, @NonNull GuiItem<?> guiItem);

    /**
     * GUI 내부의 아이템을 제어하는 클래스.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class GuiController {
        @NonNull
        private final Inventory inventory;

        /**
         * 지정한 칸에 GUI 아이템을 배치한다.
         *
         * @param index   칸 번호
         * @param guiItem GUI 아이템
         * @throws IndexOutOfBoundsException {@code index}가 인벤토리의 유효 범위를 초과하면 발생
         */
        public void set(int index, @NonNull GuiItem<?> guiItem) {
            if (index < 0 || index > inventory.getSize() - 1)
                throw new IndexOutOfBoundsException("'index'가 인벤토리의 유효 범위를 초과함");
            inventory.setItem(index, guiItem.getItemStack());
        }

        /**
         * 지정한 칸에 GUI 아이템을 배치한다.
         *
         * @param index         칸 번호
         * @param guiItem       GUI 아이템
         * @param itemConverter 아이템 변환에 실행할 작업. {@link GuiItem#toItemBuilder()}를 인자로 받아 아이템 빌더의 아이템으로 변환한다.
         * @throws IndexOutOfBoundsException {@code index}가 인벤토리의 유효 범위를 초과하면 발생
         */
        public void set(int index, @NonNull GuiItem<?> guiItem, @NonNull Consumer<ItemBuilder> itemConverter) {
            if (index < 0 || index > inventory.getSize() - 1)
                throw new IndexOutOfBoundsException("'index'가 인벤토리의 유효 범위를 초과함");
            ItemBuilder itemBuilder = guiItem.toItemBuilder();
            itemConverter.accept(itemBuilder);
            inventory.setItem(index, itemBuilder.build());
        }

        /**
         * GUI의 모든 칸을 지정한 아이템으로 채운다.
         *
         * @param guiItem GUI 아이템
         */
        public void fillAll(@NonNull GuiItem<?> guiItem) {
            for (int i = 0; i < inventory.getSize(); i++) {
                set(i, guiItem);
            }
        }

        /**
         * 지정한 행을 특정 아이템으로 채운다.
         *
         * @param row     행 번호. 1~6 사이의 값
         * @param guiItem GUI 아이템
         * @throws IllegalArgumentException {@code row}가 1~6 사이가 아니면 발생
         */
        public void fillRow(int row, @NonNull GuiItem<?> guiItem) {
            if (row < 1 || row > 6)
                throw new IllegalArgumentException("'row'가 1에서 6 사이여야 함");
            for (int i = 0; i < 9; i++)
                set((row - 1) * 9 + i, guiItem);
        }

        /**
         * 지정한 열을 특정 아이템으로 채운다.
         *
         * @param column  열 번호. 1~9 사이의 값
         * @param guiItem GUI 아이템
         * @throws IllegalArgumentException {@code column}가 1~9 사이가 아니면 발생
         */
        public void fillColumn(int column, @NonNull GuiItem<?> guiItem) {
            if (column < 1 || column > 9)
                throw new IllegalArgumentException("'column'이 1에서 9 사이여야 함");
            for (int i = 0; i < 6; i++)
                set((column - 1) + i * 9, guiItem);
        }

        /**
         * 지정한 칸에 활성화 여부를 표시하는 아이템을 배치한다.
         *
         * @param index     칸 번호
         * @param isEnabled 활성화 여부. {@code true}로 지정하면 {@link DisplayItem#ENABLED},
         *                  {@code false}로 지정하면 {@link DisplayItem#DISABLED}가 배치됨
         * @throws IndexOutOfBoundsException {@code index}가 인벤토리의 유효 범위를 초과하면 발생
         */
        public void setToggleState(int index, boolean isEnabled) {
            set(index, isEnabled ? DisplayItem.ENABLED.getGuiItem() : DisplayItem.DISABLED.getGuiItem());
        }
    }
}
