package com.dace.dmgr.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * 게임에서 사용하는 팀의 목록.
 */
@AllArgsConstructor
@Getter
public enum Team {
    NONE("없음", ChatColor.WHITE),
    RED("레드", ChatColor.RED),
    BLUE("블루", ChatColor.BLUE);

    /** 팀 이름 */
    private final String name;
    /** 팀 색 */
    private final ChatColor color;
}
