package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.PlayerSkullUtil;
import com.dace.dmgr.user.UserData;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.Consumer;

/**
 * 메뉴 - 설정 GUI 클래스.
 *
 * @see UserData.Config
 */
public final class PlayerOption extends ChestGUI {
    /**
     * 설정 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public PlayerOption(@NonNull Player player) {
        super(2, "§8설정", player);

        UserData.Config userConfig = UserData.fromPlayer(player).getConfig();

        fillRow(1, GUIItem.EMPTY);
        set(0, 0, PlayerOptionItem.KOREAN_CHAT.definedItem, itemBuilder ->
                itemBuilder.addLore(userConfig.isKoreanChat() ? "§a§l켜짐" : "§c§l꺼짐"));
        set(1, 0, userConfig.isKoreanChat() ? GUIItem.ENABLED : GUIItem.DISABLED);
        set(0, 1, PlayerOptionItem.NIGHT_VISION.definedItem, itemBuilder ->
                itemBuilder.addLore(userConfig.isNightVision() ? "§a§l켜짐" : "§c§l꺼짐"));
        set(1, 1, userConfig.isNightVision() ? GUIItem.ENABLED : GUIItem.DISABLED);
        set(0, 2, PlayerOptionItem.CROSSHAIR.definedItem);
        set(0, 3, PlayerOptionItem.CHAT_SOUND.definedItem);
        set(1, 7, new GUIItem.Previous(Menu::new));
        set(1, 8, GUIItem.EXIT);
    }

    /**
     * 설정의 아이템 목록.
     */
    private enum PlayerOptionItem {
        KOREAN_CHAT("YjAyYWYzY2EyZDVhMTYwY2ExMTE0MDQ4Yjc5NDc1OTQyNjlhZmUyYjFiNWVjMjU1ZWU3MmI2ODNiNjBiOTliOSJ9fX0=\\",
                "한글 자동 변환", "채팅 자동 한글 변환을 활성화합니다.",
                player -> {
                    UserData.Config userConfig = UserData.fromPlayer(player).getConfig();

                    userConfig.setKoreanChat(!userConfig.isKoreanChat());
                    player.performCommand("kakc chmod " + (userConfig.isKoreanChat() ? "2" : "0"));

                    new PlayerOption(player);
                }),
        NIGHT_VISION("N2VmNGU1NzM1NDkwZmI5MDMyZDUxOTMwMWMzMGU1NTkxNjY2ZTg4YjZmY2I2MmM1M2Q5ZmM3Nzk2YTZmMDZhNyJ9fX0=",
                "야간 투시", "야간 투시를 활성화합니다.",
                player -> {
                    UserData.Config userConfig = UserData.fromPlayer(player).getConfig();
                    userConfig.setNightVision(!userConfig.isNightVision());

                    new PlayerOption(player);
                }),
        CROSSHAIR("NzNjM2E5YmRjOGM0MGM0MmQ4NDFkYWViNzFlYTllN2QxYzU0YWIzMWEyM2EyZDkyNjU5MWQ1NTUxNDExN2U1ZCJ9fX0=",
                "조준선 설정", "조준선을 변경합니다.", player -> {
        }),
        CHAT_SOUND("OWIxZTIwNDEwYmI2YzdlNjk2OGFmY2QzZWM4NTU1MjBjMzdhNDBkNTRhNTRlOGRhZmMyZTZiNmYyZjlhMTkxNSJ9fX0=\\",
                "채팅 효과음 설정", "채팅 효과음을 변경하거나 끕니다.", ChatSoundOption::new);

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        PlayerOptionItem(String skinUrl, String name, String lore, Consumer<Player> action) {
            this.definedItem = new DefinedItem(new ItemBuilder(PlayerSkullUtil.fromURL(skinUrl))
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build(),
                    (clickType, player) -> {
                        if (clickType != ClickType.LEFT)
                            return false;

                        action.accept(player);
                        return true;
                    });
        }
    }
}
