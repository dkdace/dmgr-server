package com.dace.dmgr.data.model;

import com.dace.dmgr.data.Model;
import org.bukkit.entity.Player;

public class UserConfig extends Model {
    private String chatSound = "new.block.note_block.pling";
    private boolean koreanChat;
    private boolean nightVision;

    public UserConfig(Player player) {
        super("UserConfig", player.getUniqueId().toString());
        super.initConfig();
        this.koreanChat = getConfigBoolean("koreanChat");
        this.chatSound = getConfigString("chatSound", this.chatSound);
        this.nightVision = getConfigBoolean("nightVision");
    }

    public String getChatSound() {
        return chatSound;
    }

    public void setChatSound(String chatSound) {
        this.chatSound = chatSound;
        setConfig("chatSound", this.chatSound);
    }

    public boolean isKoreanChat() {
        return koreanChat;
    }

    public void setKoreanChat(boolean koreanChat) {
        this.koreanChat = koreanChat;
        setConfig("koreanChat", this.koreanChat);
    }

    public boolean isNightVision() {
        return nightVision;
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
        setConfig("nightVision", this.nightVision);
    }
}
