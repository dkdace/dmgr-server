package com.dace.dmgr.user;

import com.dace.dmgr.util.YamlUtil;
import org.bukkit.entity.Player;

public class UserConfig {
    private final YamlUtil yamlUtil;
    private String chatSound = ChatSound.PLING.toString();
    private boolean koreanChat = false;
    private boolean nightVision = false;

    public UserConfig(Player player) {
        this.yamlUtil = new YamlUtil("UserConfig", player.getUniqueId().toString());
        this.koreanChat = yamlUtil.loadValue("koreanChat", koreanChat);
        this.nightVision = yamlUtil.loadValue("nightVision", nightVision);
        this.chatSound = yamlUtil.loadValue("chatSound", chatSound);
    }

    public ChatSound getChatSound() {
        return ChatSound.valueOf(chatSound);
    }

    public void setChatSound(ChatSound chatSound) {
        this.chatSound = chatSound.toString();
        yamlUtil.saveValue("chatSound", this.chatSound);
    }

    public boolean isKoreanChat() {
        return koreanChat;
    }

    public void setKoreanChat(boolean koreanChat) {
        this.koreanChat = koreanChat;
        yamlUtil.saveValue("koreanChat", this.koreanChat);
    }

    public boolean isNightVision() {
        return nightVision;
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
        yamlUtil.saveValue("nightVision", this.nightVision);
    }
}
