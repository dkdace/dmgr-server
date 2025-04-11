package com.dace.dmgr;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.keenant.tabbed.util.Skin;
import com.keenant.tabbed.util.Skins;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.skinsrestorer.api.property.IProperty;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

/**
 * 플레이어의 스킨 정보를 나타내는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class PlayerSkin {
    /** 스킨을 불러올 때 사용하는 토큰의 접두사 */
    private static final String SKIN_TOKEN_PREFIX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";

    /** 프로퍼티 인스턴스 */
    @NonNull
    private final IProperty property;

    /**
     * 지정한 스킨 이름으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skinName 스킨 이름
     * @throws NullPointerException 해당 이름의 스킨이 존재하지 않으면 발생
     */
    @NonNull
    public static PlayerSkin fromName(@NonNull String skinName) {
        IProperty property = Validate.notNull(DMGR.getSkinsRestorerAPI().getSkinData(skinName), "스킨 %s이 존재하지 않음", skinName);
        return new PlayerSkin(property);
    }

    /**
     * 지정한 Tabbed Skin으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skin Tabbed Skin 인스턴스
     */
    @NonNull
    private static PlayerSkin fromSkin(@NonNull Skin skin) {
        WrappedSignedProperty signedProperty = skin.getProperty();
        return new PlayerSkin(DMGR.getSkinsRestorerAPI().createProperty(signedProperty.getName(), signedProperty.getValue(), signedProperty.getSignature()));
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
     * 지정한 플레이어 이름으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param playerName 플레이어 이름
     */
    @NonNull
    public static PlayerSkin fromPlayerName(@NonNull String playerName) {
        return fromSkin(Skins.getPlayer(playerName));
    }

    /**
     * 지정한 스킨 URL 값으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skinUrl 스킨 URL 값
     */
    @NonNull
    public static PlayerSkin fromURL(@NonNull String skinUrl) {
        return new PlayerSkin(DMGR.getSkinsRestorerAPI().createProperty("", SKIN_TOKEN_PREFIX + skinUrl, ""));
    }
}
