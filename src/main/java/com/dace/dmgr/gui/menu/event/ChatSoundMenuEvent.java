package com.dace.dmgr.gui.menu.event;

import com.dace.dmgr.gui.menu.ChatSoundMenu;
import com.dace.dmgr.user.ChatSound;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static com.dace.dmgr.system.EntityList.userList;

public class ChatSoundMenuEvent extends MenuEvent {
    private static final ChatSoundMenuEvent instance = new ChatSoundMenuEvent();

    private ChatSoundMenuEvent() {
        super("채팅 효과음 설정");
    }

    public static ChatSoundMenuEvent getInstance() {
        return instance;
    }

    @Override
    public void onMenuClick(InventoryClickEvent event, Player player, ItemStack clickItem, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
            User user = userList.get(player.getUniqueId());

            ChatSound chatSound = user.getUserConfig().getChatSound();
            switch (clickItemName) {
                case "음소거":
                    chatSound = ChatSound.MUTE;
                    break;
                case "플링 (기본값)":
                    chatSound = ChatSound.PLING;
                    break;
                case "하프":
                    chatSound = ChatSound.HARP;
                    break;
                case "더블 베이스":
                    chatSound = ChatSound.DOUBLE_BASS;
                    break;
                case "기타":
                    chatSound = ChatSound.GUITAR;
                    break;
                case "벨":
                    chatSound = ChatSound.BELL;
                    break;
                case "차임벨":
                    chatSound = ChatSound.CHIMEBELL;
                    break;
                case "카우벨":
                    chatSound = ChatSound.COWBELL;
                    break;
                case "플룻":
                    chatSound = ChatSound.FLUTE;
                    break;
                case "실로폰":
                    chatSound = ChatSound.XYLOPHONE;
                    break;
                case "철 실로폰":
                    chatSound = ChatSound.IRON_XYLOPHONE;
                    break;
                case "디저리두":
                    chatSound = ChatSound.DIDGERIDOO;
                    break;
                case "비트":
                    chatSound = ChatSound.BIT;
                    break;
                case "벤조":
                    chatSound = ChatSound.BANJO;
                    break;
                case "이전":
                    super.playClickSound(player);
                    player.performCommand("설정");
                    return;
            }

            SoundPlayer.play(chatSound.getSound(), player, 1F, 1.414F);
            user.getUserConfig().setChatSound(chatSound);
            new ChatSoundMenu(player).open(player);
        }
    }
}
