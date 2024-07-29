package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

/**
 * GUI(상자 인벤토리) 기능을 제공하는 클래스.
 *
 * <p>해당 클래스를 상속받아 GUI를 구현할 수 있다.</p>
 *
 * @see StaticItem
 * @see GuiItem
 * @see DisplayItem
 * @see ButtonItem
 */
public abstract class Gui {
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
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Gui(int rowSize, @NonNull String name) {
        if (rowSize < 1 || rowSize > 6)
            throw new IllegalArgumentException("'rowSize'가 1에서 6 사이여야 함");

        this.rowSize = rowSize;
        this.name = name;
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

    /**
     * GUI를 열었을 때 실행할 작업.
     *
     * @param player        대상 플레이어
     * @param guiController GUI 컨트롤러 객체
     */
    protected abstract void onOpen(@NonNull Player player, @NonNull GuiController guiController);

    /**
     * GUI 내부의 아이템을 제어하는 클래스.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class GuiController {
        /** 현재 인벤토리 */
        @NonNull
        private final Inventory inventory;

        /**
         * 지정한 칸에 정적 아이템을 배치한다.
         *
         * @param index      칸 번호
         * @param staticItem 정적 아이템
         * @throws IndexOutOfBoundsException {@code index}가 인벤토리의 유효 범위를 초과하면 발생
         */
        public void set(int index, @NonNull StaticItem staticItem) {
            if (index < 0 || index > inventory.getSize() - 1)
                throw new IndexOutOfBoundsException("'index'가 인벤토리의 유효 범위를 초과함");

            inventory.setItem(index, staticItem.getItemStack());
        }

        /**
         * 지정한 칸에 정적 아이템을 배치한다.
         *
         * @param index         칸 번호
         * @param staticItem    정적 아이템
         * @param itemConverter 아이템 변환에 실행할 작업. {@link StaticItem#toItemBuilder()}를
         *                      인자로 받아 아이템 빌더의 아이템으로 변환한다.
         * @throws IndexOutOfBoundsException {@code index}가 인벤토리의 유효 범위를 초과하면 발생
         */
        public void set(int index, @NonNull StaticItem staticItem, @NonNull Consumer<@NonNull ItemBuilder> itemConverter) {
            if (index < 0 || index > inventory.getSize() - 1)
                throw new IndexOutOfBoundsException("'index'가 인벤토리의 유효 범위를 초과함");

            ItemBuilder itemBuilder = staticItem.toItemBuilder();
            itemConverter.accept(itemBuilder);
            inventory.setItem(index, itemBuilder.build());
        }

        /**
         * GUI의 모든 칸을 지정한 아이템으로 채운다.
         *
         * @param staticItem 정적 아이템
         */
        public void fillAll(@NonNull StaticItem staticItem) {
            for (int i = 0; i < inventory.getSize(); i++) {
                set(i, staticItem);
            }
        }

        /**
         * 지정한 행을 특정 아이템으로 채운다.
         *
         * @param row        행 번호. 1~6 사이의 값
         * @param staticItem 정적 아이템
         * @throws IndexOutOfBoundsException {@code row}가 1~6 사이가 아니면 발생
         */
        public void fillRow(int row, @NonNull StaticItem staticItem) {
            if (row < 1 || row > 6)
                throw new IndexOutOfBoundsException("'row'가 1에서 6 사이여야 함");

            for (int i = 0; i < 9; i++)
                set((row - 1) * 9 + i, staticItem);
        }

        /**
         * 지정한 열을 특정 아이템으로 채운다.
         *
         * @param column     열 번호. 1~9 사이의 값
         * @param staticItem 정적 아이템
         * @throws IndexOutOfBoundsException {@code column}이 1~9 사이가 아니면 발생
         */
        public void fillColumn(int column, @NonNull StaticItem staticItem) {
            if (column < 1 || column > 9)
                throw new IndexOutOfBoundsException("'column'이 1에서 9 사이여야 함");

            for (int i = 0; i < 6; i++)
                set((column - 1) + i * 9, staticItem);
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
            set(index, isEnabled ? DisplayItem.ENABLED.getStaticItem() : DisplayItem.DISABLED.getStaticItem());
        }
    }
}
