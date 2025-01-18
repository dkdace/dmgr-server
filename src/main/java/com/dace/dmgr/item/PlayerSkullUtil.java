package com.dace.dmgr.item;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.item.gui.GUI;
import com.dace.dmgr.util.task.AsyncTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 플레이어 머리 아이템 생성 기능을 제공하는 클래스.
 */
@UtilityClass
public final class PlayerSkullUtil {
    /** 머리 스킨을 불러올 때 사용하는 토큰의 접두사 */
    private static final String SKIN_TOKEN_PREFIX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";
    /** 아이템 메타의 프로필 필드 인스턴스 */
    private static Field profileField;

    /**
     * 플레이어 머리 아이템을 반환한다.
     *
     * @param skullMetaConverter 머리 아이템 메타 편집에 실행할 작업
     * @return 플레이어 머리 아이템
     */
    @NonNull
    private static ItemStack get(@NonNull Consumer<@NonNull SkullMeta> skullMetaConverter) {
        return new ItemBuilder(Material.SKULL_ITEM)
                .setDamage((short) 3)
                .editItemMeta(itemMeta -> skullMetaConverter.accept((SkullMeta) itemMeta))
                .build();
    }

    /**
     * 지정한 플레이어의 머리 아이템을 반환한다.
     *
     * <p>이 아이템은 {@link GUI}에 배치할 때 상당한 지연이 발생하므로 {@link AsyncTask}와 함께 사용하는 것을 권장한다.</p>
     *
     * @param player 대상 플레이어
     * @return 플레이어 머리 아이템
     */
    @NonNull
    public static ItemStack fromPlayer(@NonNull OfflinePlayer player) {
        return get(skullMeta -> skullMeta.setOwningPlayer(player));
    }

    /**
     * 지정한 프로퍼티 이름으로 소유자를 지정한 머리 아이템을 반환한다.
     *
     * @param propertyName 프로퍼티 이름
     * @return 플레이어 머리 아이템
     */
    @NonNull
    private static ItemStack fromPropertyName(@NonNull String propertyName) {
        return get(skullMeta -> {
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);

            gameProfile.getProperties().put("textures", new Property("textures", propertyName));

            try {
                if (profileField == null) {
                    profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                }
                profileField.set(skullMeta, gameProfile);
            } catch (Exception ex) {
                ConsoleLogger.severe("아이템 메타 지정 실패", ex);
            }
        });
    }

    /**
     * 지정한 스킨 URL의 플레이어 머리 아이템을 반환한다.
     *
     * @param skinUrl 스킨 URL
     * @return 플레이어 머리 아이템
     */
    @NonNull
    public static ItemStack fromURL(@NonNull String skinUrl) {
        return fromPropertyName(SKIN_TOKEN_PREFIX + skinUrl);
    }

    /**
     * 지정한 전투원의 플레이어 머리 아이템을 반환한다.
     *
     * @param characterType 전투원 종류
     * @return 플레이어 머리 아이템
     */
    @NonNull
    public static ItemStack fromCharacter(@NonNull CharacterType characterType) {
        return fromPropertyName(DMGR.getSkinsRestorerAPI().getSkinData(characterType.getCharacter().getSkinName()).getValue());
    }
}
