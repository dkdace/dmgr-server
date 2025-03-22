package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 사전 정의된 아이템과 상호작용할 수 있는 GUI 기능을 제공하는 클래스.
 *
 * @see DefinedItem
 */
public class GUI {
    /** 인벤토리 인스턴스 */
    protected final Inventory inventory;
    /** 내부 아이템 목록 */
    @Nullable
    private final DefinedItem[] definedItems;

    /**
     * GUI 인스턴스를 생성한다.
     *
     * @param inventory 대상 인벤토리
     */
    public GUI(@NonNull Inventory inventory) {
        this.inventory = inventory;
        this.definedItems = new DefinedItem[inventory.getSize()];
    }

    /**
     * 지정한 번호의 칸에 있는 아이템을 반환한다.
     *
     * @param index 칸 번호 (인덱스). 0 이상의 값
     * @return 사전 정의된 아이템. 존재하지 않으면 {@code null} 반환
     * @throws IndexOutOfBoundsException {@code index}가 유효하지 않거나 인벤토리 크기 이상이면 발생
     */
    @Nullable
    public final DefinedItem get(int index) {
        validateIndex(index);
        return definedItems[index];
    }

    /**
     * 지정한 번호의 칸에 사전 정의된 아이템을 배치한다.
     *
     * @param index       칸 번호 (인덱스). 0 이상의 값
     * @param definedItem 사전 정의된 아이템
     * @throws IndexOutOfBoundsException {@code index}가 유효하지 않거나 인벤토리 크기 이상이면 발생
     */
    public final void set(int index, @NonNull DefinedItem definedItem) {
        validateIndex(index);

        definedItems[index] = definedItem;
        inventory.setItem(index, definedItem.getItemStack());
    }

    /**
     * 지정한 번호의 칸에 사전 정의된 아이템을 편집하여 배치한다.
     *
     * @param index         칸 번호 (인덱스). 0 이상의 값
     * @param definedItem   사전 정의된 아이템
     * @param itemConverter 아이템 편집에 실행할 작업. {@link ItemBuilder}를 인자로 받아 아이템 변환에 사용
     * @throws IndexOutOfBoundsException {@code index}가 유효하지 않거나 인벤토리 크기 이상이면 발생
     */
    public final void set(int index, @NonNull DefinedItem definedItem, @NonNull Consumer<@NonNull ItemBuilder> itemConverter) {
        set(index, definedItem);

        ItemBuilder itemBuilder = new ItemBuilder(definedItem.getItemStack());

        itemConverter.accept(itemBuilder);
        inventory.setItem(index, itemBuilder.build());
    }

    /**
     * GUI의 모든 칸을 지정한 아이템으로 채운다.
     *
     * @param definedItem 사전 정의된 아이템
     */
    public final void fillAll(@NonNull DefinedItem definedItem) {
        for (int i = 0; i < inventory.getSize(); i++)
            set(i, definedItem);
    }

    /**
     * 지정한 번호의 칸에 있는 아이템을 제거한다.
     *
     * @param index 칸 번호 (인덱스). 0 이상의 값
     * @throws IndexOutOfBoundsException {@code index}가 유효하지 않거나 인벤토리 크기 이상이면 발생
     */
    public final void remove(int index) {
        validateIndex(index);

        definedItems[index] = null;
        inventory.clear(index);
    }

    /**
     * GUI의 모든 칸에 있는 아이템을 제거한다.
     */
    public final void clear() {
        for (int i = 0; i < inventory.getSize(); i++)
            remove(i);
    }

    private void validateIndex(int index) {
        Validate.validIndex(definedItems, index, "%d > index >= 0 (%d)", definedItems.length, index);
    }
}
