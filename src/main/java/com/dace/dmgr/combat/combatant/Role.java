package com.dace.dmgr.combat.combatant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;

/**
 * 전투원 역할군의 종류.
 */
@AllArgsConstructor
public enum Role {
    SCUFFLER("근접", ChatColor.RED, "근거리에서 적을 처치하는 것에 특화된 역할입니다."),
    MARKSMAN("사격", ChatColor.YELLOW, "원거리에서 적을 처치하는 것에 특화된 역할입니다."),
    VANGUARD("돌격", ChatColor.GREEN, "전선을 돌파하여 적진을 무너뜨리는 것에 특화된 역할입니다."),
    GUARDIAN("수호", ChatColor.AQUA, "전선을 유지하고 아군을 보호하는 것에 특화된 역할입니다."),
    SUPPORT("지원", ChatColor.BLUE, "아군을 치유하고 지원하는 것에 특화된 역할입니다."),
    CONTROLLER("제어", ChatColor.LIGHT_PURPLE, "아군을 보조하고 넓은 범위를 통제하는 것에 특화된 역할입니다.");

    /** 이름 */
    @NonNull
    @Getter
    private final String name;
    /** 상징 색 */
    @NonNull
    @Getter
    private final ChatColor color;
    /** 설명 */
    private final String description;

    /**
     * @return 설명
     */
    @NonNull
    public String getDescription() {
        return "§f" + description;
    }
}
