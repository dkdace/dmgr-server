package com.dace.dmgr.item.gui;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * 상자 형태의 GUI 기능을 제공하는 클래스.
 *
 * <p>플레이어가 GUI를 닫기 전까지 일시적으로 사용한다.</p>
 */
public class ChestGUI extends GUI implements Disposable {
    /** 인벤토리별 GUI 목록 (인벤토리 : 상자 GUI) */
    private static final HashMap<Inventory, ChestGUI> GUI_MAP = new HashMap<>();
    /** 행 크기 */
    private final int rowSize;

    /**
     * 행 크기와 이름을 지정하여 GUI 인스턴스를 생성하고 플레이어에게 표시한다.
     *
     * @param rowSize 행 크기. 1~6 사이의 값
     * @param name    GUI 이름
     * @param player  대상 플레이어
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected ChestGUI(int rowSize, @NonNull String name, @NonNull Player player) {
        super(Bukkit.createInventory(null, rowSize * 9, name));

        Validate.inclusiveBetween(1, 6, rowSize, "6 >= rowSize >= 1 (%d)", rowSize);

        this.rowSize = rowSize;

        player.openInventory(inventory);
        GUI_MAP.put(inventory, this);
    }

    /**
     * 지정한 인벤토리에 해당하는 GUI 인스턴스를 반환한다.
     *
     * @param inventory 대상 인벤토리
     * @return GUI 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static ChestGUI fromInventory(@NonNull Inventory inventory) {
        return GUI_MAP.get(inventory);
    }

    /**
     * 현재 GUI를 제거한다.
     */
    @Override
    public final void dispose() {
        validate();
        GUI_MAP.remove(inventory);
    }

    @Override
    public final boolean isDisposed() {
        return GUI_MAP.get(inventory) == null;
    }

    /**
     * 지정한 번호의 칸에 있는 아이템을 반환한다.
     *
     * @param row    행 번호. 0 이상이고 {@link ChestGUI#rowSize} 미만인 값
     * @param column 열 번호. 0~8 사이의 값
     * @return 사전 정의된 아이템. 존재하지 않으면 {@code null} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see GUI#get(int)
     */
    @Nullable
    public final DefinedItem get(int row, int column) {
        validateRow(row);
        validateColumn(column);

        return get(row * 9 + column);
    }

    /**
     * 지정한 번호의 칸에 사전 정의된 아이템을 배치한다.
     *
     * @param row         행 번호. 0 이상이고 {@link ChestGUI#rowSize} 미만인 값
     * @param column      열 번호. 0~8 사이의 값
     * @param definedItem 사전 정의된 아이템
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see GUI#set(int, DefinedItem)
     */
    public final void set(int row, int column, @NonNull DefinedItem definedItem) {
        validateRow(row);
        validateColumn(column);

        set(row * 9 + column, definedItem);
    }

    /**
     * 지정한 번호의 칸에 사전 정의된 아이템을 편집하여 배치한다.
     *
     * @param row           행 번호. 0 이상이고 {@link ChestGUI#rowSize} 미만인 값
     * @param column        열 번호. 0~8 사이의 값
     * @param definedItem   사전 정의된 아이템
     * @param itemConverter 아이템 편집에 실행할 작업. {@link ItemBuilder}를 인자로 받아 아이템 변환에 사용
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see GUI#set(int, DefinedItem, Consumer)
     */
    public final void set(int row, int column, @NonNull DefinedItem definedItem, @NonNull Consumer<@NonNull ItemBuilder> itemConverter) {
        set(row * 9 + column, definedItem, itemConverter);
    }

    /**
     * 지정한 행을 특정 아이템으로 채운다.
     *
     * @param row         행 번호. 0 이상이고 {@link ChestGUI#rowSize} 미만인 값
     * @param definedItem 사전 정의된 아이템
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final void fillRow(int row, @NonNull DefinedItem definedItem) {
        validateRow(row);

        for (int i = 0; i < 9; i++)
            set(row, i, definedItem);
    }

    /**
     * 지정한 열을 특정 아이템으로 채운다.
     *
     * @param column      열 번호. 0~8 사이의 값
     * @param definedItem 사전 정의된 아이템
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final void fillColumn(int column, @NonNull DefinedItem definedItem) {
        validateColumn(column);

        for (int i = 0; i < 6; i++)
            set(i, column, definedItem);
    }

    /**
     * 지정한 번호의 칸에 있는 아이템을 제거한다.
     *
     * @param row    행 번호. 0 이상이고 {@link ChestGUI#rowSize} 미만인 값
     * @param column 열 번호. 0~8 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see GUI#remove(int)
     */
    public final void remove(int row, int column) {
        validateRow(row);
        validateColumn(column);

        remove(row * 9 + column);
    }

    private void validateRow(int row) {
        Validate.inclusiveBetween(0, rowSize - 1, row, "%d > row >= 0 (%d)", rowSize, row);
    }

    private void validateColumn(int column) {
        Validate.inclusiveBetween(0, 8, column, "8 >= column >= 0 (%d)", column);
    }
}
