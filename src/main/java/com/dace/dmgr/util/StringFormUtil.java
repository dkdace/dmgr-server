package com.dace.dmgr.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;

import java.util.Arrays;

/**
 * 자주 쓰이는 문자열 형식을 제공하는 클래스.
 */
@UtilityClass
public final class StringFormUtil {
    /** 추가 접두사 */
    public static final String ADD_PREFIX = "§f§l[§a§l+§f§l] §b";
    /** 제거 접두사 */
    public static final String REMOVE_PREFIX = "§f§l[§6§l-§f§l] §b";
    /** 구분선 */
    public static final String BAR = "§7========================================";
    /** 진행 막대의 기본 기호 */
    private static final char PROGRESS_DEFAULT_SYMBOL = '■';

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // <흰색>*****<검정색>*****
     * StringFormUtil.getProgressBar(50, 100, ChatColor.WHITE, 10, '*');
     * </code></pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @param length  막대 길이 (글자 수). 1 이상의 값
     * @param symbol  막대 기호
     * @return 진행 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static String getProgressBar(double current, double max, @NonNull ChatColor color, int length, char symbol) {
        Validate.isTrue(length >= 1, "length >= 1 (%d)", length);

        if (current < 0)
            current = 0;

        char[] filler = new char[length];
        Arrays.fill(filler, symbol);
        StringBuilder bar = new StringBuilder(new String(filler));

        if (current < max) {
            int index = Math.max(0, (int) Math.floor(current / max * length));
            bar.insert(index, ChatColor.BLACK);
        }
        bar.insert(0, color);

        return bar.toString();
    }

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // <흰색>■■■■■<검정색>■■■■■
     * StringFormUtil.getProgressBar(50, 100, ChatColor.WHITE, 10);
     * </code></pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @param length  막대 길이 (글자 수)
     * @return 진행 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static String getProgressBar(double current, double max, @NonNull ChatColor color, int length) {
        return getProgressBar(current, max, color, length, PROGRESS_DEFAULT_SYMBOL);
    }

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // <흰색>■■■■■<검정색>■■■■■
     * StringFormUtil.getProgressBar(50, 100, ChatColor.WHITE);
     * </code></pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @return 진행 막대 문자열
     */
    @NonNull
    public static String getProgressBar(double current, double max, @NonNull ChatColor color) {
        return getProgressBar(current, max, color, 10, PROGRESS_DEFAULT_SYMBOL);
    }
}
