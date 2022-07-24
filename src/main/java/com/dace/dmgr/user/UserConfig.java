package com.dace.dmgr.user;

import com.dace.dmgr.util.YamlModel;
import org.bukkit.entity.Player;

public class UserConfig extends YamlModel {
    private String chatSound = "new.block.note_block.pling";
    private boolean koreanChat = false;
    private boolean nightVision = false;

    public UserConfig(Player player) {
        super("UserConfig", player.getUniqueId().toString());
        this.koreanChat = loadValue("koreanChat", koreanChat);
        this.chatSound = loadValue("chatSound", chatSound);
        this.nightVision = loadValue("nightVision", nightVision);
        saveConfig();
    }

    private void saveConfig() {
        saveValue("chatSound", chatSound);
        saveValue("koreanChat", koreanChat);
        saveValue("nightVision", nightVision);
    }

    public String getChatSound() {
        return chatSound;
    }

    public void setChatSound(String chatSound) {
        this.chatSound = chatSound;
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
