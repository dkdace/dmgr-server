package com.dace.dmgr.item;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.util.ReflectionUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 아이템({@link ItemStack})의 생성 기능을 제공하는 빌더 클래스.
 *
 * <p>최종적으로 {@link ItemBuilder#build()}를 호출하여 아이템을 생성할 수 있다.</p>
 *
 * <p>Example:</p>
 *
 * <pre><code>
 * // 이름이 'Test item', 설명이 'Test lore', 수량이 3개인 막대기 생성
 * new ItemBuilder(Material.STICK)
 *     .setName("Test item")
 *     .setLore("Test lore")
 *     .setAmount(3)
 *     .build();
 * </code></pre>
 */
public final class ItemBuilder {
    /** 생성할 아이템 인스턴스 */
    private final ItemStack itemStack;
    /** 생성할 아이템 메타 인스턴스 */
    private final ItemMeta itemMeta;

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 생성한다.
     *
     * @param itemStack 대상 아이템
     */
    public ItemBuilder(@NonNull ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * 아이템을 생성하기 위한 빌더 인스턴스를 생성한다.
     *
     * @param material 아이템 타입
     */
    public ItemBuilder(@NonNull Material material) {
        this(new ItemStack(material));
    }

    /**
     * 플레이어 머리 아이템을 생성하기 위한 빌더 인스턴스를 생성한다.
     *
     * @param playerSkin 플레이어 스킨
     */
    public ItemBuilder(@NonNull PlayerSkin playerSkin) {
        this.itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        this.itemMeta = itemStack.getItemMeta();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", playerSkin.getSkin().getProperty().getValue()));

        try {
            ReflectionUtil.getField(itemMeta.getClass(), "profile").set(itemMeta, gameProfile);
        } catch (Exception ex) {
            ConsoleLogger.severe("아이템 메타 지정 실패", ex);
        }
    }

    /**
     * 아이템의 아이템 메타를 편집한다.
     *
     * @param itemMetaConverter 아이템 메타 편집에 실행할 작업
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder editItemMeta(@NonNull Consumer<@NonNull ItemMeta> itemMetaConverter) {
        itemMetaConverter.accept(this.itemMeta);
        return this;
    }

    /**
     * 아이템의 수량을 설정한다.
     *
     * @param amount 수량. 1 이상의 값
     * @return {@link ItemBuilder}
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public ItemBuilder setAmount(int amount) {
        Validate.isTrue(amount >= 1, "amount >= 1 (%d)", amount);

        itemStack.setAmount(amount);
        return this;
    }

    /**
     * 아이템의 내구도를 설정한다.
     *
     * @param damage 내구도. 0 이상의 값
     * @return {@link ItemBuilder}
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public ItemBuilder setDamage(short damage) {
        Validate.isTrue(damage >= 0, "damage >= 0 (%d)", damage);

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
     * <pre><code>
     * // 최종 이름 : '[!] Test item: STONE'
     * new ItemBuilder(Material.STONE)
     *     .setName("{0} Test item: {1}")
     *     .formatName("[!]", "STONE")
     *     .build();
     * </code></pre>
     *
     * @param arguments 포맷에 사용할 인자 목록
     * @return {@link ItemBuilder}
     * @throws IllegalStateException 아이템 이름이 설정되지 않았을 때 발생
     * @see ItemBuilder#setName(String)
     */
    @NonNull
    public ItemBuilder formatName(@NonNull Object @NonNull ... arguments) {
        Validate.validState(itemMeta.hasDisplayName(), "아이템 이름이 설정되지 않음");

        setName(MessageFormat.format(itemMeta.getDisplayName(), arguments));
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
     * 아이템의 설명을 설정한다.
     *
     * @param lore 설명 ('\n'으로 줄바꿈)
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder setLore(@NonNull String lore) {
        return setLore(lore.split("\n"));
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
     * 아이템의 설명을 추가한다.
     *
     * @param lore 추가할 설명 ('\n'으로 줄바꿈)
     * @return {@link ItemBuilder}
     */
    @NonNull
    public ItemBuilder addLore(@NonNull String lore) {
        return addLore(lore.split("\n"));
    }

    /**
     * 아이템의 설명에 포맷을 적용한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 설명에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // 최종 설명 :
     * // 'First lore : ONE'
     * // 'Second lore : TWO'
     * new ItemBuilder(Material.STONE)
     *     .setLore("First lore : {0}", "Second lore : {1}")
     *     .formatLore("ONE", "TWO")
     *     .build();
     * </code></pre>
     *
     * @param arguments 포맷에 사용할 인자 목록
     * @return {@link ItemBuilder}
     * @throws IllegalStateException 아이템 설명이 설정되지 않았을 때 발생
     */
    @NonNull
    public ItemBuilder formatLore(@NonNull Object @NonNull ... arguments) {
        Validate.validState(itemMeta.hasLore(), "아이템 설명이 설정되지 않음");

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
     * 아이템 인스턴스를 생성하여 반환한다.
     *
     * @return 해당 아이템
     */
    @NonNull
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
