package com.dace.dmgr.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 티어 목록.
 */
@AllArgsConstructor
@Getter
public enum Tier {
    NONE("없음", Integer.MIN_VALUE, -1, "§8[ 등급 없음 ]"),
    STONE("스톤", Integer.MIN_VALUE, -1, "§7§l[ 스톤 ]"),
    IRON("아이언", 0, 249, "§f§l[ 아이언 ]"),
    GOLD("골드", 250, 499, "§e§l[ 골드 ]"),
    REDSTONE("레드스톤", 500, 749, "§c§l[ 레드스톤 ]"),
    EMERALD("에메랄드", 750, 999, "§a§l[ 에메랄드 ]"),
    DIAMOND("다이아몬드", 1000, Integer.MAX_VALUE, "§b§l[ 다이아몬드 ]"),
    NETHERITE("네더라이트", 1000, Integer.MAX_VALUE, "§5§l[ 네더라이트 ]");

    /** 티어의 이름 */
    private final String name;
    /** 최소 랭크 점수 */
    private final int minScore;
    /** 최대 랭크 점수 */
    private final int maxScore;
    /** 칭호 */
    private final String prefix;
}
