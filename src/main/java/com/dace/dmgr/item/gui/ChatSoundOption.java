package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * 메뉴 - 채팅 효과음 설정 GUI 클래스.
 */
public final class ChatSoundOption extends Gui {
    @Getter
    private static final ChatSoundOption instance = new ChatSoundOption();

    public ChatSoundOption() {
        super(2, "§8채팅 효과음 설정");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        UserData.Config userConfig = UserData.fromPlayer(player).getConfig();

        ChatSound[] chatSounds = ChatSound.values();

        guiController.fillRow(2, DisplayItem.EMPTY.getGuiItem());
        for (int i = 0; i < chatSounds.length; i++) {
            int index = i;
            guiController.set(i, chatSounds[i].guiItem, itemBuilder -> {
                if (userConfig.getChatSound() == chatSounds[index]) itemBuilder.addLore("§a§l선택됨");
            });
        }

        guiController.set(17, ButtonItem.LEFT.getGuiItem());
    }

    @Override
    public void onClick(InventoryClickEvent event, @NonNull Player player, @NonNull GuiItem<?> guiItem) {
        if (event.getClick() != ClickType.LEFT)
            return;

        if (guiItem == ButtonItem.LEFT.getGuiItem()) {
            player.performCommand("설정");
            return;
        }

        if (guiItem.getIdentifier() instanceof ChatSound) {
            ChatSound chatSound = (ChatSound) guiItem.getIdentifier();
            UserData.Config userConfig = UserData.fromPlayer(player).getConfig();

            SoundUtil.play(chatSound.getSound(), player, 1, Math.sqrt(2));
            userConfig.setChatSound(chatSound);
            open(player);
        }
    }

    /**
     * 채팅 효과음 목록.
     */
    public enum ChatSound {
        /** 음소거 */
        MUTE("음소거", "", Material.BARRIER),
        /** 플링 */
        PLING("플링", "new.block.note_block.pling", Material.GLOWSTONE),
        /** 하프 */
        HARP("하프", "new.block.note_block.harp", Material.GRASS),
        /** 더블 베이스 */
        DOUBLE_BASS("더블 베이스", "new.block.note_block.bass", Material.WOOD),
        /** 기타 */
        GUITAR("기타", "new.block.note_block.guitar", Material.WOOL),
        /** 벨 */
        BELL("벨", "new.block.note_block.bell", Material.GOLD_BLOCK),
        /** 차임벨 */
        CHIMEBELL("차임벨", "new.block.note_block.chime", Material.PACKED_ICE),
        /** 카우벨 */
        COWBELL("카우벨", "new.block.note_block.cow_bell", Material.SOUL_SAND),
        /** 플룻 */
        FLUTE("플룻", "new.block.note_block.flute", Material.CLAY),
        /** 실로폰 */
        XYLOPHONE("실로폰", "new.block.note_block.xylophone", Material.QUARTZ_BLOCK),
        /** 철 실로폰 */
        IRON_XYLOPHONE("철 실로폰", "new.block.note_block.iron_xylophone", Material.IRON_BLOCK),
        /** 디저리두 */
        DIDGERIDOO("디저리두", "new.block.note_block.didgeridoo", Material.PUMPKIN),
        /** 비트 */
        BIT("비트", "new.block.note_block.bit", Material.EMERALD_BLOCK),
        /** 벤조 */
        BANJO("벤조", "new.block.note_block.banjo", Material.HAY_BLOCK);

        /** 이름 */
        @Getter
        private final String name;
        /** 효과음 */
        @Getter
        private final String sound;
        /** GUI 아이템 객체 */
        private final GuiItem<ChatSound> guiItem;

        ChatSound(String name, String sound, Material material) {
            ItemBuilder itemBuilder = new ItemBuilder(material).setName("§e§l" + name);
            this.name = name;
            this.sound = sound;

            this.guiItem = new GuiItem<ChatSound>(this, itemBuilder.build()) {
                @Override
                public Gui getGui() {
                    return instance;
                }

                @Override
                public boolean isClickable() {
                    return true;
                }
            };
        }
    }
}
