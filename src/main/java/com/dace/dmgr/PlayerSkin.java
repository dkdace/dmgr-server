package com.dace.dmgr;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.dace.dmgr.util.task.AsyncTask;
import com.keenant.tabbed.util.Skin;
import com.keenant.tabbed.util.Skins;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.property.IProperty;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 플레이어의 스킨 정보를 나타내는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class PlayerSkin {
    /** SkinsRestorer API 인스턴스 */
    private static final SkinsRestorerAPI API = SkinsRestorerAPI.getApi();
    /** 스킨을 불러올 때 사용하는 토큰의 접두사 */
    private static final String SKIN_TOKEN_PREFIX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";

    /** Tabbed Skin 인스턴스 */
    @NonNull
    private final Skin skin;

    /**
     * 지정한 스킨 이름으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skinName 스킨 이름
     * @throws NullPointerException 해당 이름의 스킨이 존재하지 않으면 발생
     */
    @NonNull
    public static PlayerSkin fromName(@NonNull String skinName) {
        IProperty property = Validate.notNull(API.getSkinData(skinName), "스킨 %s이 존재하지 않음", skinName);
        return new PlayerSkin(new Skin(property.getValue(), property.getSignature()));
    }

    /**
     * 지정한 Tabbed Skin으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skin Tabbed Skin 인스턴스
     */
    @NonNull
    public static PlayerSkin fromSkin(@NonNull Skin skin) {
        return new PlayerSkin(skin);
    }

    /**
     * 지정한 UUID로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param uuid UUID
     */
    @NonNull
    public static PlayerSkin fromUUID(@NonNull UUID uuid) {
        return fromSkin(Skins.getPlayer(uuid));
    }

    /**
     * 지정한 스킨 URL 값으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skinUrl 스킨 URL 값
     * @apiNote 플레이어 머리 아이템에만 사용할 수 있음
     */
    @NonNull
    public static PlayerSkin fromURL(@NonNull String skinUrl) {
        return fromSkin(new Skin(SKIN_TOKEN_PREFIX + skinUrl, ""));
    }

    /**
     * 지정한 플레이어에게 스킨을 적용한다.
     *
     * @param player 적용할 플레이어
     */
    @NonNull
    public AsyncTask<Void> applySkin(@NonNull Player player) {
        return new AsyncTask<>((onFinish, onError) -> {
            try {
                API.applySkin(new PlayerWrapper(player), toProperty());
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("{0}의 스킨 적용 실패", ex, player.getName());
                onError.accept(ex);
            }
        });
    }

    /**
     * 플레이어 스킨을 SkinsRestorer API의 프로퍼티 인스턴스로 바꿔 반환한다.
     *
     * @return 프로퍼티 인스턴스
     */
    @NonNull
    public IProperty toProperty() {
        WrappedSignedProperty property = skin.getProperty();
        return API.createProperty(property.getName(), property.getValue(), property.getSignature());
    }
}
