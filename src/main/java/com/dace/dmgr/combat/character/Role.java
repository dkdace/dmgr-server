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
    SCUFFLER("근접", ChatColor.RED, new String[]{"§f역할군 설명"}),
    MARKSMAN("사격", ChatColor.YELLOW, new String[]{"§f역할군 설명"}),
    VANGUARD("돌격", ChatColor.GREEN, new String[]{"§f역할군 설명"}),
    GUARDIAN("수호", ChatColor.AQUA, new String[]{"§f역할군 설명"}),
    SUPPORT("지원", ChatColor.BLUE, new String[]{"§f역할군 설명"}),
    CONTROLLER("제어", ChatColor.LIGHT_PURPLE, new String[]{"§f역할군 설명"});

    /** 이름 */
    private final String name;
    /** 상징 색 */
    private final ChatColor color;
    /** 설명 */
    private final String[] description;
}
