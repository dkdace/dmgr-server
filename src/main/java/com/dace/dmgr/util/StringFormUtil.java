package com.dace.dmgr.util;

import com.dace.dmgr.combat.action.TextIcon;
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
    /** 구분선 */
    public static final String BAR = "§7========================================";
    /** 진행 막대의 기본 기호 */
    private static final char PROGRESS_DEFAULT_SYMBOL = '■';

    /**
     * 진행 막대를 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // <흰색>*****<검정색>*****
     * StringFormUtil.getProgressBar(50, 100, ChatColor.WHITE, 10, '*');
     * }</pre>
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
        validateArgs(length);

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
     * // [Test] <노란색>****<검정색>****** [40/100]
     * StringFormUtil.getActionbarProgressBar("[Test]", 40, 100, 10, '*');
     * }</pre>
     *
     * @param prefix  접두사
     * @param current 현재 값
     * @param max     최대 값
     * @param length  막대 길이 (글자 수). 1 이상의 값
     * @param symbol  막대 기호
     * @return 액션바 진행 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static String getActionbarProgressBar(@NonNull String prefix, int current, int max, int length, char symbol) {
        validateArgs(length);

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

    /**
     * 액션바에 사용되는 남은 시간 막대를 반환한다.
     *
     * <p>기본적으로 흰색, 현재 값이 최대 값의 1/2 이하일 경우 노란색,
     * 1/4 이하일 경우 빨간색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // [Test] <노란색>****<검정색>****** [{@link TextIcon#DURATION} 40.5]
     * StringFormUtil.getActionbarProgressBar("[Test]", 40.5, 100, 10, '*');
     * }</pre>
     *
     * @param prefix  접두사
     * @param current 남은 시간 (tick)
     * @param max     최대 시간 (tick)
     * @param length  막대 길이 (글자 수). 1 이상의 값
     * @param symbol  막대 기호
     * @return 액션바 남은 시간 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static String getActionbarDurationBar(@NonNull String prefix, double current, double max, int length, char symbol) {
        validateArgs(length);

        ChatColor color;
        if (current <= max / 4)
            color = ChatColor.RED;
        else if (current <= max / 2)
            color = ChatColor.YELLOW;
        else
            color = ChatColor.WHITE;

        String currentDisplay = String.format("%.1f", current);

        return new StringJoiner(" §f")
                .add(prefix)
                .add(StringFormUtil.getProgressBar(current, max, color, length, symbol))
                .add("[" + color + TextIcon.DURATION + " " + currentDisplay + "§f]")
                .toString();
    }

    /**
     * 액션바에 사용되는 쿨타임 막대를 반환한다.
     *
     * <p>기본적으로 빨간색, 현재 값이 최대 값의 1/2 이하일 경우 노란색,
     * 1/4 이하일 경우 흰색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // [Test] <노란색>****<검정색>****** [{@link TextIcon#COOLDOWN} 40.5]
     * StringFormUtil.getActionbarCooldownBar("[Test]", 40.5, 100, 10, '*');
     * }</pre>
     *
     * @param prefix  접두사
     * @param current 남은 시간 (tick)
     * @param max     최대 시간 (tick)
     * @param length  막대 길이 (글자 수). 1 이상의 값
     * @param symbol  막대 기호
     * @return 액션바 남은 시간 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static String getActionbarCooldownBar(@NonNull String prefix, double current, double max, int length, char symbol) {
        validateArgs(length);

        ChatColor color;
        if (current <= max / 4)
            color = ChatColor.WHITE;
        else if (current <= max / 2)
            color = ChatColor.YELLOW;
        else
            color = ChatColor.RED;

        String currentDisplay = String.format("%.1f", current);

        return new StringJoiner(" §f")
                .add(prefix)
                .add(StringFormUtil.getProgressBar(current, max, color, length, symbol))
                .add("[" + color + TextIcon.COOLDOWN + " " + currentDisplay + "§f]")
                .toString();
    }

    /**
     * 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param length 막대 길이 (글자 수)
     */
    private static void validateArgs(int length) {
        if (length < 1)
            throw new IllegalArgumentException("'length'가 1 이상이어야 함");
    }
}
