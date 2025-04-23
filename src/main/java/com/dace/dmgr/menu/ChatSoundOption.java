package com.dace.dmgr.menu;

import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.item.ChestGUI;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.GUIItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * 메뉴 - 채팅 효과음 설정 GUI 클래스.
 */
public final class ChatSoundOption extends ChestGUI {
    /**
     * 채팅 효과음 설정 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    ChatSoundOption(@NonNull Player player) {
        super(2, "§8채팅 효과음 설정", player);

        fillRow(1, GUIItem.EMPTY);

        UserData.Config userConfig = UserData.fromPlayer(player).getConfig();
        ChatSound[] chatSounds = ChatSound.values();

        for (int i = 0; i < chatSounds.length; i++) {
            ChatSound chatSound = chatSounds[i];

            set(i, chatSound.definedItem, itemBuilder -> {
                if (userConfig.getChatSound() == chatSound)
                    itemBuilder.addLore("§a§l선택됨");
            });
        }

        set(1, 8, new GUIItem.Previous(PlayerOption::new));
    }

    /**
     * 채팅 효과음 목록.
     */
    public enum ChatSound {
        /** 음소거 */
        MUTE("음소거", "", Material.BARRIER),
        /** 플링 */
        PLING("플링", "pling", Material.GLOWSTONE),
        /** 하프 */
        HARP("하프", "harp", Material.GRASS),
        /** 더블 베이스 */
        DOUBLE_BASS("더블 베이스", "bass", Material.WOOD),
        /** 기타 */
        GUITAR("기타", "guitar", Material.WOOL),
        /** 벨 */
        BELL("벨", "bell", Material.GOLD_BLOCK),
        /** 차임벨 */
        CHIMEBELL("차임벨", "chime", Material.PACKED_ICE),
        /** 카우벨 */
        COWBELL("카우벨", "cow_bell", Material.SOUL_SAND),
        /** 플룻 */
        FLUTE("플룻", "flute", Material.CLAY),
        /** 실로폰 */
        XYLOPHONE("실로폰", "xylophone", Material.QUARTZ_BLOCK),
        /** 철 실로폰 */
        IRON_XYLOPHONE("철 실로폰", "iron_xylophone", Material.IRON_BLOCK),
        /** 디저리두 */
        DIDGERIDOO("디저리두", "didgeridoo", Material.PUMPKIN),
        /** 비트 */
        BIT("비트", "bit", Material.EMERALD_BLOCK),
        /** 벤조 */
        BANJO("벤조", "banjo", Material.HAY_BLOCK);

        /** 효과음 */
        @NonNull
        @Getter
        private final SoundEffect sound;
        /** GUI 아이템 */
        private final DefinedItem definedItem;

        ChatSound(String name, String sound, Material material) {
            this.sound = new SoundEffect(SoundEffect.SoundInfo.builder("new.block.note_block." + sound).volume(1000).pitch(Math.sqrt(2)).build());

            this.definedItem = new DefinedItem(new ItemBuilder(material).setName("§e§l" + name).build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                        UserData.Config userConfig = UserData.fromPlayer(player).getConfig();
                        userConfig.setChatSound(this);

                        this.sound.play(player);

                        new ChatSoundOption(player);

                        return true;
                    }));
        }
    }
}
