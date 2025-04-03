package com.dace.dmgr.item;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Predicate;

/**
 * 고정적으로 사용되는 사전 정의된 아이템을 관리하는 클래스.
 */
public class DefinedItem {
    /** 기본 아이템 인스턴스 */
    private final ItemStack itemStack;
    /** 클릭 유형별 이벤트 처리기 목록 (클릭 유형 : 클릭 이벤트 처리기) */
    private final EnumMap<ClickType, DefinedItem.ClickHandler> clickHandlerMap = new EnumMap<>(ClickType.class);

    /**
     * 사전 정의된 아이템 인스턴스를 생성한다.
     *
     * @param itemStack     대상 아이템
     * @param clickHandlers 클릭 이벤트 처리기 목록
     */
    public DefinedItem(@NonNull ItemStack itemStack, @NonNull DefinedItem.ClickHandler @NonNull ... clickHandlers) {
        this.itemStack = new ItemBuilder(itemStack).editItemMeta(itemMeta -> {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS);
            itemMeta.setUnbreakable(true);
        }).build();

        for (ClickHandler clickHandler : clickHandlers)
            this.clickHandlerMap.put(clickHandler.clickType, clickHandler);
    }

    /**
     * 사전 정의된 아이템 인스턴스를 생성한다.
     *
     * @param itemStack 대상 아이템
     */
    public DefinedItem(@NonNull ItemStack itemStack) {
        this(itemStack, new ClickHandler[0]);
    }

    /**
     * 사전 정의된 아이템의 기본 아이템 인스턴스를 반환한다.
     *
     * @return 기본 아이템 인스턴스
     */
    @NonNull
    public final ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * 지정한 클릭 유형에 해당하는 이벤트 처리기를 반환한다.
     *
     * @param clickType 클릭 유형
     * @return 클릭 이벤트 처리기. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public ClickHandler getClickHandler(@NonNull ClickType clickType) {
        return clickHandlerMap.get(clickType);
    }

    /**
     * 아이템의 클릭 이벤트 처리기 클래스.
     */
    @AllArgsConstructor
    public static final class ClickHandler {
        /** 클릭 유형 */
        @NonNull
        public final ClickType clickType;
        /**
         * 아이템을 클릭했을 때 실행할 작업.
         *
         * <p>클릭 성공 여부를 반환해야 함</p>
         */
        @NonNull
        public final Predicate<Player> onClick;
    }
}
