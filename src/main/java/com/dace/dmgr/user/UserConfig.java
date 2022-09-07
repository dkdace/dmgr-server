package com.dace.dmgr.user;

import com.dace.dmgr.util.YamlModel;
import org.bukkit.entity.Player;

public class UserConfig extends YamlModel {
    private String chatSound = ChatSound.PLING.toString();
    private boolean koreanChat = false;
    private boolean nightVision = false;

    public UserConfig(Player player) {
        super("UserConfig", player.getUniqueId().toString());
        this.koreanChat = loadValue("koreanChat", koreanChat);
        this.nightVision = loadValue("nightVision", nightVision);
        this.chatSound = loadValue("chatSound", chatSound);
        saveConfig();
    }

    private void saveConfig() {
        saveValue("koreanChat", koreanChat);
        saveValue("nightVision", nightVision);
        saveValue("chatSound", chatSound);
    }

    public ChatSound getChatSound() {
        return ChatSound.valueOf(chatSound);
    }

    public void setChatSound(ChatSound chatSound) {
        this.chatSound = chatSound.toString();
        saveValue("chatSound", this.chatSound);
    }

    public boolean isKoreanChat() {
        return koreanChat;
    }

    public void setKoreanChat(boolean koreanChat) {
        this.koreanChat = koreanChat;
        saveValue("koreanChat", this.koreanChat);
    }

    public boolean isNightVision() {
        return nightVision;
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
        saveValue("nightVision", this.nightVision);
    }
}
