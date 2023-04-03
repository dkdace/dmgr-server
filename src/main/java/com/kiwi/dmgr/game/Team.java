package com.kiwi.dmgr.game;

import org.bukkit.DyeColor;

/**
 * 팀 정보를 담는 클래스
 */
public enum Team {
    RED("§c", DyeColor.RED),
    BLUE("§2", DyeColor.BLUE),
    NONE("§e", DyeColor.YELLOW);

    /* 채팅 색깔 */
    final String chatColor;

    /* 팀 스폰 경계 양털블럭 색깔 */
    final DyeColor teamSpawnBorderColor;

    Team(String chatColor, DyeColor teamSpawnBorderColor) {
        this.chatColor = chatColor;
        this.teamSpawnBorderColor = teamSpawnBorderColor;
    }
}
