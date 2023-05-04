package com.kiwi.dmgr.game;

import org.bukkit.DyeColor;

/**
 * 팀 정보를 담는 클래스
 */
public enum Team {
    RED("레드", "§c", DyeColor.RED),
    BLUE("블루", "§2", DyeColor.BLUE),
    NONE("없음", "§e", DyeColor.YELLOW);

    /* 팀 이름 */
    final String name;

    /* 팀 색깔 */
    final String color;

    /* 팀 스폰 경계 양털블럭 색깔 */
    final DyeColor teamSpawnBorderColor;

    Team(String name, String color, DyeColor teamSpawnBorderColor) {
        this.name = name;
        this.color = color;
        this.teamSpawnBorderColor = teamSpawnBorderColor;
    }
}
