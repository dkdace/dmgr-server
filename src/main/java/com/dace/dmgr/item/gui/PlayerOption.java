package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * 메뉴 - 설정 GUI 클래스.
 */
public final class PlayerOption extends Gui {
    @Getter
    private static final PlayerOption instance = new PlayerOption();

    private PlayerOption() {
        super(2, "§8설정");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        UserData.Config userConfig = UserData.fromPlayer(player).getConfig();

        guiController.fillRow(2, DisplayItem.EMPTY.getStaticItem());
        guiController.set(0, PlayerOptionItem.KOREAN_CHAT.guiItem, itemBuilder ->
                itemBuilder.addLore(userConfig.isKoreanChat() ? "§a§l켜짐" : "§c§l꺼짐"));
        guiController.setToggleState(9, userConfig.isKoreanChat());
        guiController.set(1, PlayerOptionItem.NIGHT_VISION.guiItem, itemBuilder ->
                itemBuilder.addLore(userConfig.isNightVision() ? "§a§l켜짐" : "§c§l꺼짐"));
        guiController.setToggleState(10, userConfig.isNightVision());
        guiController.set(2, PlayerOptionItem.CROSSHAIR.guiItem);
        guiController.set(3, PlayerOptionItem.CHAT_SOUND.guiItem);
        guiController.set(16, PlayerOptionItem.LEFT.guiItem);
        guiController.set(17, ButtonItem.Exit.getInstance());
    }

    @AllArgsConstructor
    private enum PlayerOptionItem {
        KOREAN_CHAT("YjAyYWYzY2EyZDVhMTYwY2ExMTE0MDQ4Yjc5NDc1OTQyNjlhZmUyYjFiNWVjMjU1ZWU3MmI2ODNiNjBiOTliOSJ9fX0=\\",
                "한글 자동 변환", "채팅 자동 한글 변환을 활성화합니다.",
                player -> {
                    UserData.Config userConfig = UserData.fromPlayer(player).getConfig();

                    userConfig.setKoreanChat(!userConfig.isKoreanChat());
                    player.performCommand(userConfig.isKoreanChat() ? "kakc chmod 2" : "kakc chmod 0");

                    PlayerOption.getInstance().open(player);
                }),
        NIGHT_VISION("N2VmNGU1NzM1NDkwZmI5MDMyZDUxOTMwMWMzMGU1NTkxNjY2ZTg4YjZmY2I2MmM1M2Q5ZmM3Nzk2YTZmMDZhNyJ9fX0=",
                "야간 투시", "야간 투시를 활성화합니다.",
                player -> {
                    UserData.Config userConfig = UserData.fromPlayer(player).getConfig();
                    userConfig.setNightVision(!userConfig.isNightVision());

                    PlayerOption.getInstance().open(player);
                }),
        CROSSHAIR("NzNjM2E5YmRjOGM0MGM0MmQ4NDFkYWViNzFlYTllN2QxYzU0YWIzMWEyM2EyZDkyNjU5MWQ1NTUxNDExN2U1ZCJ9fX0=",
                "조준선 설정", "조준선을 변경합니다.", player -> {
        }),
        CHAT_SOUND("OWIxZTIwNDEwYmI2YzdlNjk2OGFmY2QzZWM4NTU1MjBjMzdhNDBkNTRhNTRlOGRhZmMyZTZiNmYyZjlhMTkxNSJ9fX0=\\",
                "채팅 효과음 설정", "채팅 효과음을 변경하거나 끕니다.",
                player -> ChatSoundOption.getInstance().open(player)),
        LEFT(new ButtonItem.Left("PlayerOptionLeft") {
            @Override
            public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                player.performCommand("메뉴");
                return true;
            }
        });

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        PlayerOptionItem(String skinUrl, String name, String lore, Consumer<Player> action) {
            this.guiItem = new GuiItem("PlayerOption" + this, new ItemBuilder(Material.SKULL_ITEM)
                    .setDamage((short) 3)
                    .setSkullOwner(skinUrl)
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build()) {
                @Override
                public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                    if (clickType != ClickType.LEFT)
                        return false;

                    action.accept(player);
                    return true;
                }
            };
        }
    }
}
