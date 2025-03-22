package com.dace.dmgr.item;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * 고정적으로 사용되는 사전 정의된 아이템을 관리하는 클래스.
 */
public class DefinedItem {
    /** 기본 아이템 인스턴스 */
    private final ItemStack itemStack;
    /** 아이템을 클릭했을 때 실행할 작업 */
    @NonNull
    @Getter
    private final OnClick onClick;

    /**
     * 사전 정의된 아이템 인스턴스를 생성한다.
     *
     * @param itemStack 대상 아이템
     * @param onClick   아이템을 클릭했을 때 실행할 작업
     */
    public DefinedItem(@NonNull ItemStack itemStack, @NonNull OnClick onClick) {
        this.itemStack = new ItemBuilder(itemStack).editItemMeta(itemMeta -> {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS);
            itemMeta.setUnbreakable(true);
        }).build();

        this.onClick = onClick;
    }

    /**
     * 사전 정의된 아이템 인스턴스를 생성한다.
     *
     * @param itemStack 대상 아이템
     */
    public DefinedItem(@NonNull ItemStack itemStack) {
        this(itemStack, (clickType, player) -> false);
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
     * 아이템을 클릭했을 때 실행할 작업.
     */
    @FunctionalInterface
    public interface OnClick {
        /**
         * 아이템을 클릭했을 때 실행할 작업.
         *
         * @param clickType 클릭 유형
         * @param player    클릭한 플레이어
         * @return 클릭 성공 여부
         */
        boolean apply(@NonNull ClickType clickType, @NonNull Player player);
    }
}
