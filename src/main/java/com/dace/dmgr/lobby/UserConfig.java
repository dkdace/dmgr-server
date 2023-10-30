package com.dace.dmgr.lobby;

import com.dace.dmgr.system.YamlFile;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * 유저 설정 정보를 관리하는 클래스.
 *
 * @see User
 */
public final class UserConfig {
    /** 설정파일 관리를 위한 객체 */
    private final YamlFile yamlFile;
    /** 채팅 효과음 */
    private String chatSound = ChatSound.PLING.toString();
    /** 한글 채팅 여부 */
    @Getter
    private boolean koreanChat = false;
    /** 야간 투시 여부 */
    @Getter
    private boolean nightVision = false;

    /**
     * 유저 설정 인스턴스를 생성한다.
     *
     * <p>{@link User}가 생성될 때 호출되어야 한다.</p>
     *
     * @param player 대상 플레이어
     */
    public UserConfig(Player player) {
        this.yamlFile = new YamlFile("UserConfig/" + player.getUniqueId().toString());
        this.koreanChat = yamlFile.get("koreanChat", koreanChat);
        this.nightVision = yamlFile.get("nightVision", nightVision);
        this.chatSound = yamlFile.get("chatSound", chatSound);
    }

    public ChatSound getChatSound() {
        return ChatSound.valueOf(chatSound);
    }

    public void setChatSound(ChatSound chatSound) {
        this.chatSound = chatSound.toString();
        yamlFile.set("chatSound", this.chatSound);
    }

    public void setKoreanChat(boolean koreanChat) {
        this.koreanChat = koreanChat;
        yamlFile.set("koreanChat", this.koreanChat);
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
        yamlFile.set("nightVision", this.nightVision);
    }
}
