package com.dace.dmgr.combat.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.util.StringFormUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;

import java.text.MessageFormat;
import java.util.StringJoiner;

/**
 * 액션바에 무기 및 스킬 상태 표시를 위해 자주 쓰이는 문자열 형식을 제공하는 클래스.
 */
@UtilityClass
public final class ActionBarStringUtil {
    /**
     * 동작 사용 키 설명을 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>[1][우클릭] 사용</pre>
     * <pre><code>
     * ActionBarStringUtil.getKeyInfo("사용", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK);
     * </code></pre>
     *
     * @param description 설명
     * @param actionKeys  동작 사용 키 목록
     * @return 사용 키 설명
     */
    @NonNull
    public static String getKeyInfo(@NonNull String description, @NonNull ActionKey @NonNull ... actionKeys) {
        StringJoiner keys = new StringJoiner("][");
        for (ActionKey actionKey : actionKeys)
            keys.add(actionKey.toString());

        return MessageFormat.format("  §7[{0}] §f{1}", keys, description);
    }

    /**
     * 지정한 스킬의 사용 키 설명을 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>[1][우클릭] 사용</pre>
     * <pre><code>
     * ActionBarStringUtil.getKeyInfo(skill, "사용");
     * </code></pre>
     *
     * @param skill       스킬
     * @param description 설명
     * @return 사용 키 설명
     */
    @NonNull
    public static String getKeyInfo(@NonNull Skill skill, @NonNull String description) {
        return getKeyInfo(description, skill.getDefaultActionKeys());
    }

    /**
     * 진행 막대를 반환한다.
     *
     * <p>기본적으로 흰색, 현재 값이 최대 값의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>[Test] <font color="yellow">****</font><font color="black">******</font> [40/100]</pre>
     * <pre><code>
     * ActionBarStringUtil.getProgressBar("[Test]", 40, 100, 10, '*');
     * </code></pre>
     *
     * @param prefix  접두사
     * @param current 현재 값
     * @param max     최대 값
     * @param length  막대 길이 (글자 수). 1 이상의 값
     * @param symbol  막대 기호
     * @return 진행 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static String getProgressBar(@NonNull String prefix, int current, int max, int length, char symbol) {
        Validate.isTrue(length >= 1, "length >= 1 (%d)", length);

        ChatColor color;
        if (current <= max / 4)
            color = ChatColor.RED;
        else if (current <= max / 2)
            color = ChatColor.YELLOW;
        else
            color = ChatColor.WHITE;

        String currentDisplay = String.format(String.format("%%%dd", (int) (Math.log10(max) + 1)), current);
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
     * 지정한 충전형 스킬의 상태 변수 진행 막대를 반환한다.
     *
     * <p>기본적으로 흰색, 상태 변수가 최대 값의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>[스킬명] <font color="yellow">■■■■</font><font color="black">■■■■■■</font> [40/100]</pre>
     * <pre><code>
     * ActionBarStringUtil.getProgressBar(skill);
     * </code></pre>
     *
     * @param chargeableSkill 충전형 스킬
     * @return 진행 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static String getProgressBar(@NonNull ChargeableSkill chargeableSkill) {
        return getProgressBar(chargeableSkill.getSkillInfo().toString(), (int) chargeableSkill.getStateValue(), (int) chargeableSkill.getMaxStateValue(),
                10, '■');
    }

    /**
     * 지정한 스킬의 남은 시간 막대를 반환한다.
     *
     * <p>기본적으로 흰색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>[스킬명] <font color="yellow">■■■■</font><font color="black">■■■■■■</font> [(아이콘) 40.5]</pre>
     * <pre><code>
     * ActionBarStringUtil.getDurationBar(skill, Timespan.ofSeconds(40.5), Timespan.ofSeconds(100));
     * </code></pre>
     *
     * @param skill       스킬
     * @param duration    남은 시간
     * @param maxDuration 최대 시간
     * @return 남은 시간 막대 문자열
     */
    @NonNull
    public static String getDurationBar(@NonNull Skill skill, @NonNull Timespan duration, @NonNull Timespan maxDuration) {
        double currentSeconds = duration.toSeconds();
        double maxSeconds = maxDuration.toSeconds();

        ChatColor color;
        if (currentSeconds <= maxSeconds / 4)
            color = ChatColor.RED;
        else if (currentSeconds <= maxSeconds / 2)
            color = ChatColor.YELLOW;
        else
            color = ChatColor.WHITE;

        String currentDisplay = String.format("%.1f", currentSeconds);

        return new StringJoiner(" §f")
                .add(skill.getSkillInfo().toString())
                .add(StringFormUtil.getProgressBar(currentSeconds, maxSeconds, color))
                .add(MessageFormat.format("[{0}{1} {2}§f]", color, TextIcon.DURATION, currentDisplay))
                .toString();
    }

    /**
     * 지정한 스킬의 남은 시간 막대를 반환한다.
     *
     * <p>기본적으로 흰색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>[스킬명] <font color="yellow">■■■■</font><font color="black">■■■■■■</font> [(아이콘) 40.5]</pre>
     * <pre><code>
     * ActionBarStringUtil.getDurationBar(skill);
     * </code></pre>
     *
     * @param skill 스킬
     * @return 남은 시간 막대 문자열
     */
    @NonNull
    public static String getDurationBar(@NonNull Skill skill) {
        return getDurationBar(skill, skill.getDuration(), skill.getDefaultDuration());
    }

    /**
     * 지정한 스킬의 쿨타임 막대를 반환한다.
     *
     * <p>기본적으로 빨간색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 흰색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>[스킬명] <font color="red">■■■■■■</font><font color="black">■■■■</font> [(아이콘) 60.5]</pre>
     * <pre><code>
     * ActionBarStringUtil.getCooldownBar(skill, Timespan.ofSeconds(60.5), Timespan.ofSeconds(100));
     * </code></pre>
     *
     * @param skill       스킬
     * @param cooldown    남은 시간
     * @param maxCooldown 최대 시간
     * @return 쿨타임 막대 문자열
     */
    @NonNull
    public static String getCooldownBar(@NonNull Skill skill, @NonNull Timespan cooldown, @NonNull Timespan maxCooldown) {
        double currentSeconds = cooldown.toSeconds();
        double maxSeconds = maxCooldown.toSeconds();

        ChatColor color;
        if (currentSeconds <= maxSeconds / 4)
            color = ChatColor.WHITE;
        else if (currentSeconds <= maxSeconds / 2)
            color = ChatColor.YELLOW;
        else
            color = ChatColor.RED;

        String currentDisplay = String.format("%.1f", currentSeconds);

        return new StringJoiner(" §f")
                .add(skill.getSkillInfo().toString())
                .add(StringFormUtil.getProgressBar(currentSeconds, maxSeconds, color))
                .add(MessageFormat.format("[{0}{1} {2}§f]", color, TextIcon.COOLDOWN, currentDisplay))
                .toString();
    }

    /**
     * 지정한 스킬의 쿨타임 막대를 반환한다.
     *
     * <p>기본적으로 빨간색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 흰색으로 표시한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>[스킬명] <font color="red">■■■■■■</font><font color="black">■■■■</font> [(아이콘) 60.5]</pre>
     * <pre><code>
     * ActionBarStringUtil.getCooldownBar(skill);
     * </code></pre>
     *
     * @param skill 스킬
     * @return 쿨타임 막대 문자열
     */
    @NonNull
    public static String getCooldownBar(@NonNull Skill skill) {
        return getCooldownBar(skill, skill.getCooldown(), skill.getDefaultCooldown());
    }
}
