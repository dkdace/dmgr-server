package com.dace.dmgr.item;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.MessageFormat;

/**
 * 고정적으로 사용되는 정적 아이템을 관리하는 클래스.
 *
 * @param <E> 식별용 Enum 타입
 */
public class StaticItem<E extends Enum<E>> {
    /** 아이템 식별자 */
    @NonNull
    @Getter
    private final E identifier;
    /** 기본 아이템 객체 */
    @NonNull
    private final ItemStack itemStack;

    /**
     * 정적 아이템 인스턴스를 생성한다.
     *
     * @param identifier 아이템 식별자
     * @param itemStack  대상 아이템
     * @throws IllegalStateException 해당 {@code identifier}의 StaticItem이 이미 존재하면 발생
     */
    public StaticItem(@NonNull E identifier, @NonNull ItemStack itemStack) {
        String fullIdentifier = identifier.getClass().getSimpleName() + identifier;

        StaticItem<?> staticItem = StaticItemRegistry.getInstance().get(fullIdentifier);
        if (staticItem != null)
            throw new IllegalStateException(MessageFormat.format("식별자 {0}의 StaticItem이 이미 생성됨", identifier));

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLocalizedName(fullIdentifier);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);
        this.identifier = identifier;
        this.itemStack = itemStack;

        StaticItemRegistry.getInstance().add(fullIdentifier, this);
    }

    /**
     * 지정한 아이템의 정적 아이템 인스턴스를 반환한다.
     *
     * @param itemStack 대상 아이템
     * @return 정적 아이템 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    public static StaticItem<?> fromItemStack(@NonNull ItemStack itemStack) {
        return StaticItemRegistry.getInstance().get(itemStack.getItemMeta().getLocalizedName());
    }

    /**
     * @return 기본 아이템 객체
     */
    @NonNull
    public final ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * 현재 아이템으로 아이템 빌더를 생성하여 반환한다.
     *
     * @return 아이템 빌더
     */
    @NonNull
    public final ItemBuilder toItemBuilder() {
        return new ItemBuilder(itemStack);
    }
}
