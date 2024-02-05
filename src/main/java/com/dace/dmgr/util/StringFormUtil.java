package com.dace.dmgr.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 자주 쓰이는 문자열 형식을 제공하는 클래스.
 */
@UtilityClass
public final class StringFormUtil {
    /** 추가 접두사 */
    public static final String ADD_PREFIX = "§f§l[§a§l+§f§l] §b";
    /** 제거 접두사 */
    public static final String REMOVE_PREFIX = "§f§l[§6§l-§f§l] §b";
    /** 진행 막대의 기본 기호 */
    private static final char PROGRESS_DEFAULT_SYMBOL = '■';

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // <흰색>*****<검정색>*****
     * StringFormUtil.getProgressBar(50, 100, ChatColor.WHITE, 10, *);
     * }</pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @param length  막대 길이 (글자 수)
     * @param symbol  막대 기호
     * @return 진행 막대 문자열
     */
    @NonNull
    public static String getProgressBar(double current, double max, @NonNull ChatColor color, int length, char symbol) {
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
        bar.insert(0, color);

        return bar.toString();
    }

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // <흰색>■■■■■<검정색>■■■■■
     * StringFormUtil.getProgressBar(50, 100, ChatColor.WHITE, 10);
     * }</pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @param length  막대 길이 (글자 수)
     * @return 진행 막대 문자열
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
     * <pre>{@code
     * // <흰색>■■■■■<검정색>■■■■■
     * StringFormUtil.getProgressBar(50, 100, ChatColor.WHITE);
     * }</pre>
     *
     * @param current 현재 값
     * @param max     최대 값
     * @param color   막대 색
     * @return 진행 막대 문자열
     */
    @NonNull
    public static String getProgressBar(double current, double max, @NonNull ChatColor color) {
        return getProgressBar(current, max, color, 10);
    }

    /**
     * 액션바에 사용되는 진행 막대를 반환한다.
     *
     * <p>기본적으로 흰색, 현재 값이 최대 값의 1/2 이하일 경우 노란색,
     * 1/4 이하일 경우 빨간색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // [Test] <노란색>****<흰색>****** [40/100]
     * StringFormUtil.getActionbarProgressBar("[Test]", 40, 100, 10, *);
     * }</pre>
     *
     * @param prefix  접두사
     * @param current 현재 값
     * @param max     최대 값
     * @param length  막대 길이 (글자 수)
     * @param symbol  막대 기호
     * @return 액션바 진행 막대 문자열
     */
    @NonNull
    public static String getActionbarProgressBar(@NonNull String prefix, int current, int max, int length, char symbol) {
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
