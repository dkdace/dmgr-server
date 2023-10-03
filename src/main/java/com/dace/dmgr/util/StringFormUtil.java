package com.dace.dmgr.util;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 자주 쓰이는 문자열 형식을 제공하는 클래스.
 */
public final class StringFormUtil {
    /** 진행 막대의 기본 기호 */
    private static final char DEFAULT_SYMBOL = '■';

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>■■■■■□□□□□</pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @param length  막대 길이 (글자 수)
     * @param symbol  막대 기호
     * @return 진행 막대 문자열
     */
    public static String getProgressBar(double current, double max, ChatColor color, int length, char symbol) {
        if (current < 0) current = 0;

        char[] filler = new char[length];
        Arrays.fill(filler, symbol);
        StringBuilder bar = new StringBuilder(new String(filler));

        double percent = current / max;

        if (current < max) {
            int index = ((int) Math.floor(percent * length));
            if (index < 0) index = 0;

            bar.insert(index, "§0");
        }
        bar.insert(0, color.toString());

        return bar.toString();
    }

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>■■■■■□□□□□</pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @param length  막대 길이 (글자 수)
     * @return 진행 막대 문자열
     */
    public static String getProgressBar(double current, double max, ChatColor color, int length) {
        return getProgressBar(current, max, color, length, DEFAULT_SYMBOL);
    }

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>■■■■■□□□□□</pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @return 진행 막대 문자열
     */
    public static String getProgressBar(double current, double max, ChatColor color) {
        return getProgressBar(current, max, color, 10);
    }

    /**
     * 액션바에 사용되는 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>[아이콘] ■■■■■□□□□□ [5/10]</pre>
     *
     * @param prefix  접두사
     * @param current 현재 값
     * @param max     최대 값
     * @param length  막대 길이 (글자 수)
     * @param symbol  막대 기호
     * @return 액션바 진행 막대 문자열
     */
    public static String getActionbarProgressBar(String prefix, int current, int max, int length, char symbol) {
        ChatColor color;
        if (current <= max / 4)
            color = ChatColor.RED;
        else if (current <= max / 2)
            color = ChatColor.YELLOW;
        else
            color = ChatColor.WHITE;

        String currentDisplay = String.format("%" + (int) (Math.log10(max) + 1) + "d", current);
        String maxDisplay = Integer.toString(max);

        return new StringJoiner(" §f")
                .add(prefix)
                .add(StringFormUtil.getProgressBar(current, max, color, length, symbol))
                .add(new StringJoiner("§f/", "[", "]")
                        .add(color + currentDisplay)
                        .add(maxDisplay)
                        .toString())
                .toString();
    }
}
