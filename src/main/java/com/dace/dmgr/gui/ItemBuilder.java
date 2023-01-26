package com.dace.dmgr.gui;

import com.dace.dmgr.gui.slot.ISlotItem;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

/**
 * 아이템 생성 기능을 제공하는 빌더 클래스.
 */
public class ItemBuilder {
    /** 크랙샷 아이템을 생성하기 위한 크랙샷 객체 */
    public static final CSUtility csUtility = new CSUtility();
    /** 생성할 아이템 객체 */
    private final ItemStack itemStack;
    /** 생성할 아이템의 정보 객체 */
    private final ItemMeta itemMeta;
    /** 플레이어 머리 생성에 사용하는 필드 객체 */
    private static Field profileField;

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 생성한다.
     *
     * <p>최종적으로 {@link ItemBuilder#build()}를 호출하여 아이템 객체를 생성할 수 있다.</p>
     *
     * @param itemStack 대상 아이템
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        itemMeta = itemStack.getItemMeta();
    }

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 생성한다.
     *
     * <p>최종적으로 {@link ItemBuilder#build()}를 호출하여 아이템 객체를 생성할 수 있다.</p>
     *
     * @param material 아이템 타입
     */
    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 {@link ISlotItem}으로 생성한다.
     *
     * @param slotItem 슬롯 아이템
     * @return ItemBuilder
     */
    public static ItemBuilder fromSlotItem(ISlotItem slotItem) {
        ItemBuilder itemBuilder = new ItemBuilder(slotItem.getMaterial())
                .setDamage(slotItem.getDamage())
                .setName(slotItem.getName())
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemBuilder.getItemMeta().setUnbreakable(true);
        return itemBuilder;
    }

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 지정한 플레이어의 머리로 생성한다.
     *
     * @param player 대상 플레이어
     * @return ItemBuilder
     */
    public static ItemBuilder fromPlayerSkull(Player player) {
        ItemBuilder itemBuilder = new ItemBuilder(Material.SKULL_ITEM).setDamage((short) 3);
        ((SkullMeta) itemBuilder.getItemMeta()).setOwningPlayer(player);
        return itemBuilder;
    }

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 {@link SkullIcon}으로 생성한다.
     *
     * @param skullIcon 플레이어 머리 아이템
     * @return ItemBuilder
     */
    public static ItemBuilder fromSkullIcon(SkullIcon skullIcon) {
        ItemBuilder itemBuilder = new ItemBuilder(Material.SKULL_ITEM).setDamage((short) 3);
        SkullMeta skullMeta = ((SkullMeta) itemBuilder.getItemMeta());

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", skullIcon.getUrl()));

        try {
            if (profileField == null) {
                profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
            }
            profileField.set(skullMeta, gameProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemBuilder;
    }

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 크랙샷 아이템으로 생성한다.
     *
     * @param weaponName 크랙샷 무기 이름
     * @return ItemBuilder
     */
    public static ItemBuilder fromCSItem(String weaponName) {
        ItemStack weapon = csUtility.generateWeapon(weaponName);
        ItemBuilder itemBuilder = new ItemBuilder(weapon).addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemBuilder.getItemMeta().setUnbreakable(true);
        return itemBuilder;
    }

    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    /**
     * 아이템의 수량을 설정한다.
     *
     * @param amount 수량
     * @return ItemBuilder
     */
    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * 아이템의 내구도를 설정한다.
     *
     * @param damage 내구도
     * @return ItemBuilder
     */
    public ItemBuilder setDamage(short damage) {
        itemStack.setDurability(damage);
        return this;
    }

    /**
     * 아이템의 설명을 설정한다.
     *
     * @param lores 설명
     * @return ItemBuilder
     */
    public ItemBuilder setLore(String... lores) {
        itemMeta.setLore(Arrays.asList(lores));
        return this;
    }

    /**
     * 아이템의 이름을 설정한다.
     *
     * @param name 이름
     * @return ItemBuilder
     */
    public ItemBuilder setName(String name) {
        itemMeta.setDisplayName(name);
        return this;
    }

    /**
     * 아이템에 플래그 속성 정보를 추가한다.
     *
     * @param itemFlags 추가할 아이템 플래그
     * @return ItemBuilder
     */
    public ItemBuilder addItemFlags(ItemFlag... itemFlags) {
        itemMeta.addItemFlags(itemFlags);
        return this;
    }

    /**
     * 아이템의 플래그 속성 정보를 제거한다.
     *
     * @param itemFlags 제거할 아이템 플래그
     * @return ItemBuilder
     */
    public ItemBuilder removeItemFlags(ItemFlag... itemFlags) {
        itemMeta.removeItemFlags(itemFlags);
        return this;
    }

    /**
     * 아이템 객체를 반환한다.
     *
     * @return 해당 아이템
     */
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
