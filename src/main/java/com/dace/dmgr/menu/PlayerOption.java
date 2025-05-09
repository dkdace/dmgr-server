package com.dace.dmgr.menu;

import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.item.ChestGUI;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.GUIItem;
import com.dace.dmgr.item.ItemBuilder;
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
    PlayerOption(@NonNull Player player) {
        super(2, "§8설정", player);

        UserData.Config userConfig = UserData.fromPlayer(player).getConfig();

        fillRow(1, GUIItem.EMPTY);
        set(0, 0, PlayerOptionItem.NIGHT_VISION.definedItem, itemBuilder ->
                itemBuilder.addLore(userConfig.isNightVision() ? "§a§l켜짐" : "§c§l꺼짐"));
        set(1, 0, userConfig.isNightVision() ? GUIItem.ENABLED : GUIItem.DISABLED);
        set(0, 1, PlayerOptionItem.CROSSHAIR.definedItem);
        set(0, 2, PlayerOptionItem.CHAT_SOUND.definedItem);
        set(1, 7, new GUIItem.Previous(Menu::new));
        set(1, 8, GUIItem.EXIT);
    }

    /**
     * 설정의 아이템 목록.
     */
    private enum PlayerOptionItem {
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
            this.definedItem = new DefinedItem(new ItemBuilder(PlayerSkin.fromURL(skinUrl))
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                        action.accept(player);
                        return true;
                    }));
        }
    }
}
