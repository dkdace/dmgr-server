package com.dace.dmgr.item;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.util.task.AsyncTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 아이템 생성 기능을 제공하는 빌더 클래스.
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * // 이름이 'Test item', 설명이 'Test lore', 수량이 3개인 막대기 생성
 * new ItemBuilder(Material.STICK)
 *     .setName("Test item")
 *     .setLore("Test lore")
 *     .setAmount(3)
 *     .build();
 * }</pre>
 */
public final class ItemBuilder {
    /** 플레이어 머리 생성에 사용하는 필드 객체 */
    private static Field profileField;
    /** 생성할 아이템 객체 */
    private final ItemStack itemStack;
    /** 생성할 아이템의 정보 객체 */
    private final ItemMeta itemMeta;

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 생성한다.
     *
     * <p>최종적으로 {@link ItemBuilder#build()}를 호출하여 아이템 객체를 생성할 수 있다.</p>
     *
     * @param itemStack 대상 아이템
     */
    public ItemBuilder(@NonNull ItemStack itemStack) {
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
    public ItemBuilder(@NonNull Material material) {
        this(new ItemStack(material));
    }

    /**
     * 플레이어 머리 아이템의 지정 플레이어를 설정한다.
     *
     * @param player 대상 플레이어
     * @return {@link ItemBuilder}
     * @throws IllegalStateException 아이템이 플레이어 머리가 아니면 발생
     * @apiNote 비동기로 실행하지 않음. {@link AsyncTask}와 함께 사용하는 것을 권장
     */
    public ItemBuilder setSkullOwner(@NonNull OfflinePlayer player) {
        if (!(itemMeta instanceof SkullMeta))
            throw new IllegalStateException("아이템이 플레이어 머리가 아님");

        ((SkullMeta) itemMeta).setOwningPlayer(player);
        return this;
    }

    /**
     * 플레이어 머리 아이템의 지정 플레이어를 지정한 스킨 URL로 설정한다.
     *
     * @param skinUrl 스킨 URL
     * @return {@link ItemBuilder}
     * @throws IllegalStateException 아이템이 플레이어 머리가 아니면 발생
     */
    public ItemBuilder setSkullOwner(@NonNull String skinUrl) {
        if (!(itemMeta instanceof SkullMeta))
            throw new IllegalStateException("아이템이 플레이어 머리가 아님");

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", skinUrl));

        try {
            if (profileField == null) {
                profileField = ((SkullMeta) itemMeta).getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
            }
            profileField.set(itemMeta, gameProfile);
        } catch (Exception ex) {
            ConsoleLogger.severe("머리 아이템 생성 실패", ex);
        }

        return this;
    }

    /**
     * 아이템의 수량을 설정한다.
     *
     * @param amount 수량
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * 아이템의 내구도를 설정한다.
     *
     * @param damage 내구도
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder setDamage(short damage) {
        itemStack.setDurability(damage);
        return this;
    }

    /**
     * 아이템의 이름을 설정한다.
     *
     * @param name 이름
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder setName(@NonNull String name) {
        itemMeta.setDisplayName(name);
        return this;
    }

    /**
     * 아이템의 이름에 포맷을 적용한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 이름에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 최종 이름 : '[!] Test item: STONE'
     * new ItemBuilder(Material.STONE)
     *     .setName("{0} Test item: {1}")
     *     .formatName("[!]", "STONE")
     *     .build();
     * }</pre>
     *
     * @param arguments 포맷에 사용할 인자 목록
     * @return {@link ItemBuilder}
     * @throws IllegalStateException 아이템 이름이 설정되지 않았을 때 발생
     */
    @NonNull
    public ItemBuilder formatName(@NonNull Object @NonNull ... arguments) {
        if (!itemMeta.hasDisplayName())
            throw new IllegalStateException("아이템의 이름이 아직 설정되지 않음");

        setName(MessageFormat.format(itemMeta.getDisplayName(), arguments));
        return this;
    }

    /**
     * 아이템의 설명을 설정한다.
     *
     * @param lore 설명 ('\n'으로 줄바꿈)
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder setLore(@NonNull String lore) {
        itemMeta.setLore(Arrays.asList(lore.split("\n")));
        return this;
    }

    /**
     * 아이템의 설명을 설정한다.
     *
     * @param lores 설명 목록
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder setLore(@NonNull String @NonNull ... lores) {
        itemMeta.setLore(Arrays.asList(lores));
        return this;
    }

    /**
     * 아이템의 설명을 추가한다.
     *
     * @param lore 추가할 설명 ('\n'으로 줄바꿈)
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder addLore(@NonNull String lore) {
        if (!itemMeta.hasLore()) {
            setLore(lore);
            return this;
        }

        List<String> fullLore = itemMeta.getLore();
        fullLore.addAll(Arrays.asList(lore.split("\n")));
        itemMeta.setLore(fullLore);
        return this;
    }

    /**
     * 아이템의 설명을 추가한다.
     *
     * @param lores 추가할 설명 목록
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder addLore(@NonNull String @NonNull ... lores) {
        if (!itemMeta.hasLore()) {
            setLore(lores);
            return this;
        }

        List<String> fullLore = itemMeta.getLore();
        fullLore.addAll(Arrays.asList(lores));
        itemMeta.setLore(fullLore);
        return this;
    }

    /**
     * 아이템의 설명에 포맷을 적용한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 설명에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // 최종 설명 :
     * // 'First lore : ONE'
     * // 'Second lore : TWO'
     * new ItemBuilder(Material.STONE)
     *     .setLore("First lore : {0}", "Second lore : {1}")
     *     .formatLore("ONE", "TWO")
     *     .build();
     * }</pre>
     *
     * @param arguments 포맷에 사용할 인자 목록
     * @return {@link ItemBuilder}
     * @throws IllegalStateException 아이템 설명이 설정되지 않았을 때 발생
     */
    @NonNull
    public ItemBuilder formatLore(@NonNull Object... arguments) {
        if (!itemMeta.hasLore())
            throw new IllegalStateException("아이템의 설명이 아직 설정되지 않음");

        String fullLore = MessageFormat.format(String.join("\n", itemMeta.getLore()), arguments);
        return setLore(fullLore);
    }

    /**
     * 아이템에 발광 효과(마법부여)를 적용한다.
     *
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder setGlowing() {
        itemMeta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * 아이템에 플래그 속성 정보를 추가한다.
     *
     * @param itemFlags 추가할 아이템 플래그
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder addItemFlags(@NonNull ItemFlag @NonNull ... itemFlags) {
        itemMeta.addItemFlags(itemFlags);
        return this;
    }

    /**
     * 아이템의 플래그 속성 정보를 제거한다.
     *
     * @param itemFlags 제거할 아이템 플래그
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder removeItemFlags(@NonNull ItemFlag @NonNull ... itemFlags) {
        itemMeta.removeItemFlags(itemFlags);
        return this;
    }

    /**
     * 아이템 객체를 생성하여 반환한다.
     *
     * @return 해당 아이템
     */
    @NonNull
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
