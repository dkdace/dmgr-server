package com.dace.dmgr;

import com.comphenix.packetwrapper.WrapperPlayServerHeldItemSlot;
import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.comphenix.packetwrapper.WrapperPlayServerRespawn;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.ReflectionUtil;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.Initializable;
import com.keenant.tabbed.util.Skin;
import com.keenant.tabbed.util.Skins;
import lombok.*;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

/**
 * 플레이어의 스킨 정보를 나타내는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class PlayerSkin {
    /** 스킨을 불러올 때 사용하는 토큰의 접두사 */
    private static final String SKIN_TOKEN_PREFIX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";
    /** Yaml 파일 경로의 디렉터리 이름 */
    private static final String DIRECTORY_NAME = "Skin";

    /** Tabbed Skin 인스턴스 */
    @NonNull
    private final Skin skin;

    /**
     * 지정한 Tabbed Skin으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skin Tabbed Skin 인스턴스
     * @return {@link PlayerSkin}
     */
    @NonNull
    public static PlayerSkin fromSkin(@NonNull Skin skin) {
        return new PlayerSkin(skin);
    }

    /**
     * 지정한 스킨 이름으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skinName 스킨 이름
     * @return {@link Async}
     * @throws NullPointerException 해당 이름의 스킨을 불러올 수 없으면 발생
     */
    @NonNull
    public static PlayerSkin.Async fromName(@NonNull String skinName) {
        return new Async(new AsyncTask<>((onFinish, onError) -> {
            try {
                List<String> lines = Files.readAllLines(DMGR.getPlugin().getDataFolder().toPath()
                        .resolve(DIRECTORY_NAME)
                        .resolve(skinName.toLowerCase() + ".skin"));

                onFinish.accept(fromSkin(new Skin(lines.get(0), lines.get(1))));
            } catch (Exception ex) {
                ConsoleLogger.severe("스킨을 불러올 수 없음 : {0}", ex, skinName);
                onError.accept(ex);
            }
        }));
    }

    /**
     * 지정한 UUID로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param uuid UUID
     * @return {@link Async}
     */
    @NonNull
    public static PlayerSkin.Async fromUUID(@NonNull UUID uuid) {
        return new Async(new AsyncTask<>((onFinish, onError) -> onFinish.accept(fromSkin(Skins.getPlayer(uuid)))));
    }

    /**
     * 지정한 스킨 URL 값으로 스킨 인스턴스를 생성하여 반환한다.
     *
     * @param skinUrl 스킨 URL 값
     * @return {@link PlayerSkin}
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
    @SuppressWarnings("deprecation")
    public void applySkin(@NonNull Player player) {
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", skin.getProperty());

        new DelayTask(() -> {
            if (!player.isOnline())
                return;

            try {
                EnumWrappers.NativeGameMode nativeGameMode = EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode());

                WrapperPlayServerPlayerInfo[] packets = new WrapperPlayServerPlayerInfo[2];
                Arrays.setAll(packets, i -> {
                    WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();

                    packet.setData(Collections.singletonList(new PlayerInfoData(
                            gameProfile,
                            User.fromPlayer(player).getPing(),
                            nativeGameMode,
                            WrappedChatComponent.fromText(player.getName()))));

                    return packet;
                });

                packets[0].setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
                packets[1].setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);

                packets[0].broadcastPacket();
                packets[1].broadcastPacket();

                Location loc = player.getLocation();

                WrapperPlayServerRespawn packet1 = new WrapperPlayServerRespawn();
                packet1.setDimension(player.getWorld().getEnvironment().getId());
                packet1.setDifficulty(EnumWrappers.Difficulty.valueOf(player.getWorld().getDifficulty().toString()));
                packet1.setGamemode(nativeGameMode);
                packet1.setLevelType(player.getWorld().getWorldType());
                packet1.sendPacket(player);

                WrapperPlayServerPosition packet2 = new WrapperPlayServerPosition();
                packet2.setX(loc.getX());
                packet2.setY(loc.getY());
                packet2.setZ(loc.getZ());
                packet2.setYaw(loc.getYaw());
                packet2.setPitch(loc.getPitch());
                packet2.setFlags(new HashSet<>());
                packet2.sendPacket(player);

                WrapperPlayServerHeldItemSlot packet3 = new WrapperPlayServerHeldItemSlot();
                packet3.setSlot(player.getInventory().getHeldItemSlot());
                packet3.sendPacket(player);

                Method getHandleMethod = ReflectionUtil.getMethod(player.getClass(), "getHandle");
                Object nmsPlayer = getHandleMethod.invoke(player);

                ReflectionUtil.getMethod(nmsPlayer.getClass(), "updateAbilities").invoke(nmsPlayer);
                ReflectionUtil.getMethod(player.getClass(), "updateScaledHealth").invoke(player);
                ReflectionUtil.getMethod(nmsPlayer.getClass(), "triggerHealthUpdate").invoke(nmsPlayer);

                player.updateInventory();

                Bukkit.getOnlinePlayers().forEach(target -> target.hidePlayer(DMGR.getPlugin(), player));
                Bukkit.getOnlinePlayers().forEach(target -> target.showPlayer(DMGR.getPlugin(), player));
            } catch (Exception ex) {
                ConsoleLogger.severe("{0}의 스킨 적용 실패", ex, player.getName());
            }
        }, 1);
    }

    /**
     * 비동기적으로 불러오는 스킨을 나타내는 클래스.
     *
     * @see PlayerSkin#fromName(String)
     * @see PlayerSkin#fromUUID(UUID)
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Async implements Initializable<PlayerSkin> {
        /** 플레이어 스킨을 불러오는 태스크 */
        @NonNull
        private final AsyncTask<PlayerSkin> playerSkinTask;
        /** 플레이어 스킨 */
        @Nullable
        private PlayerSkin playerSkin;

        /**
         * 플레이어 스킨을 불러온다.
         *
         * @return 플레이어 스킨
         */
        @Override
        @NonNull
        public AsyncTask<PlayerSkin> init() {
            if (isInitialized())
                throw new IllegalStateException("인스턴스가 이미 초기화됨");

            return new AsyncTask<>((onFinish, onError) ->
                    playerSkinTask.onFinish(targetskin -> {
                        playerSkin = targetskin;
                        onFinish.accept(get());
                    }));
        }

        @Override
        public boolean isInitialized() {
            return playerSkin != null;
        }

        /**
         * {@link Async#init()}에서 불러온 플레이어 스킨을 반환한다.
         *
         * @return 플레이어 스킨
         * @throws NullPointerException 현재 스킨을 불러오지 않았으면 발생
         */
        @NonNull
        public PlayerSkin get() {
            validate();
            return Validate.notNull(playerSkin, "아직 스킨을 불러오지 않았음");
        }
    }
}
