package com.dace.dmgr.combat.character;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * 전투원 역할군의 종류.
 */
@AllArgsConstructor
@Getter
public enum Role {
    ASSASSIN("암살", ChatColor.RED, new String[]{"§f역할군 설명"}),
    SCUFFLER("근접", ChatColor.YELLOW, new String[]{"§f역할군 설명"}),
    MARKSMAN("사격", ChatColor.GREEN, new String[]{"§f역할군 설명"}),
    VANGUARD("돌격", ChatColor.AQUA, new String[]{"§f역할군 설명"}),
    GUARDIAN("수호", ChatColor.BLUE, new String[]{"§f역할군 설명"}),
    SUPPORTER("지원", ChatColor.LIGHT_PURPLE, new String[]{"§f역할군 설명"});

    /** 이름 */
    private final String name;
    /** 상징 색 */
    private final ChatColor color;
    /** 설명 */
    private final String[] description;
}
