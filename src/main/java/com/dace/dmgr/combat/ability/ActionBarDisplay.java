package com.dace.dmgr.combat.ability;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.skill.ChargeableSkill;
import com.dace.dmgr.combat.ability.skill.Skill;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.Swappable;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.util.StringFormUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;

import java.text.MessageFormat;
import java.util.StringJoiner;

/**
 * 액션바에 무기 및 스킬 상태 표시를 처리하는 클래스.
 */
public final class ActionBarDisplay {
    /** 탄약 표시 막대 ({@link Builder#ammoBar(int, char)})에 사용되는 기호 */
    public static final char AMMO_BAR_SYMBOL = '|';
    /** 탄약 표시 막대 ({@link Builder#ammoBar(int, char)})에 사용되는 기호 (대형) */
    public static final char AMMO_BAR_BIG_SYMBOL = '┃';

    /** 제목 */
    private final String title;
    /** 표시 막대 */
    private final String bar;
    /** 접미사 */
    private final String suffix;
    /** 동작 사용 키 설명 */
    private final String keyInfo;

    private ActionBarDisplay(Builder builder) {
        this.title = builder.title;
        this.bar = builder.bar;
        this.suffix = builder.suffix;
        this.keyInfo = builder.keyInfo;
    }

    /**
     * 빌더 인스턴스를 생성하여 반환한다.
     *
     * @param ability 능력
     * @return {@link Builder}
     */
    @NonNull
    public static Builder builder(@NonNull Ability ability) {
        return new Builder(ability);
    }

    /**
     * 액션바 표시 전체 문자열을 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre>[능력 이름] <font color="yellow">■■■■</font><font color="black">■■■■■■</font> [(아이콘) 40.5]  [1][좌클릭] 해제</pre>
     *
     * <pre><code>
     * ActionBarDisplay actionBarDisplay = ActionBarDisplay.builder(ability)
     *     .title()
     *     .durationBar()
     *     .keyInfo("해제")
     *     .build();
     * actionBarDisplay.toString();
     * </code></pre>
     *
     * @return 전체 문자열
     */
    @Override
    public String toString() {
        return title + bar + suffix + keyInfo;
    }

    /**
     * {@link ActionBarDisplay}의 빌더 클래스.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        private final Ability ability;
        private String title = "";
        private String bar = "";
        private String suffix = "";
        private String keyInfo = "";

        /**
         * 제목을 설정한다.
         *
         * <p>Example:</p>
         *
         * <pre>Test title</pre>
         * <pre><code>
         * builder.title("Test title");
         * </code></pre>
         *
         * @param title 제목
         * @return {@link Builder}
         */
        @NonNull
        public Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        /**
         * 제목을 {@link Ability#getDisplayName()}으로 설정한다.
         *
         * <p>Example:</p>
         *
         * <pre>[능력 이름]</pre>
         * <pre><code>
         * builder.title();
         * </code></pre>
         *
         * @return {@link Builder}
         */
        @NonNull
        public Builder title() {
            return title(ability.getDisplayName());
        }

        /**
         * 표시 막대를 진행 막대로 설정한다.
         *
         * <p>기본적으로 흰색, 현재 값이 최대 값의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
         *
         * <p>Example:</p>
         *
         * <pre><font color="yellow">****</font><font color="black">******</font> [40/100]</pre>
         * <pre><code>
         * builder.progressBar(40, 100, 10, '*');
         * </code></pre>
         *
         * @param current 현재 값
         * @param max     최대 값
         * @param length  막대 길이 (글자 수). 1 이상의 값
         * @param symbol  막대 기호
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public Builder progressBar(int current, int max, int length, char symbol) {
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

            bar = " " + new StringJoiner(" §f")
                    .add(StringFormUtil.getProgressBar(current, max, color, length, symbol))
                    .add(new StringJoiner("§f/", "[", "]")
                            .add(color + currentDisplay)
                            .add(maxDisplay)
                            .toString());

            return this;
        }

        /**
         * 표시 막대를 진행 막대로 설정한다.
         *
         * <p>기본적으로 흰색, 현재 값이 최대 값의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
         *
         * <p>Example:</p>
         *
         * <pre><font color="yellow">■■■■</font><font color="black">■■■■■■</font> [40/100]</pre>
         * <pre><code>
         * builder.progressBar(40, 100);
         * </code></pre>
         *
         * @param current 현재 값
         * @param max     최대 값
         * @return {@link Builder}
         */
        @NonNull
        public Builder progressBar(int current, int max) {
            return progressBar(current, max, 10, StringFormUtil.PROGRESS_DEFAULT_SYMBOL);
        }

        /**
         * 표시 막대를 충전형 스킬의 상태 변수 막대로 설정한다.
         *
         * <p>기본적으로 흰색, 상태 변수가 최대 값의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
         *
         * <p>Example:</p>
         *
         * <pre><font color="yellow">■■■■</font><font color="black">■■■■■■</font> [40/100]</pre>
         * <pre><code>
         * builder.progressBar();
         * </code></pre>
         *
         * @return {@link Builder}
         * @throws IllegalArgumentException 능력이 {@link ChargeableSkill}을 상속받지 않으면 발생
         */
        @NonNull
        public Builder progressBar() {
            Validate.isTrue(ability instanceof ChargeableSkill, "ability가 ChargeableSkill을 상속받지 않음");
            return progressBar((int) ((ChargeableSkill) ability).getStateValue(), (int) ((ChargeableSkill) ability).getMaxStateValue(), 10,
                    StringFormUtil.PROGRESS_DEFAULT_SYMBOL);
        }

        /**
         * 표시 막대를 남은 시간 막대로 설정한다.
         *
         * <p>기본적으로 흰색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
         *
         * <p>Example:</p>
         *
         * <pre><font color="yellow">■■■■</font><font color="black">■■■■■■</font> [(아이콘) 40.5]</pre>
         * <pre><code>
         * builder.durationBar(Timespan.ofSeconds(40.5), Timespan.ofSeconds(100));
         * </code></pre>
         *
         * @param duration    남은 시간
         * @param maxDuration 최대 시간
         * @return {@link Builder}
         */
        @NonNull
        public Builder durationBar(@NonNull Timespan duration, @NonNull Timespan maxDuration) {
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

            bar = " " + new StringJoiner(" §f")
                    .add(StringFormUtil.getProgressBar(currentSeconds, maxSeconds, color))
                    .add(MessageFormat.format("[{0}{1} {2}§f]", color, TextIcon.DURATION, currentDisplay));

            return this;
        }

        /**
         * 표시 막대를 스킬의 남은 시간 막대로 설정한다.
         *
         * <p>기본적으로 흰색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 빨간색으로 표시한다.</p>
         *
         * <p>Example:</p>
         *
         * <pre><font color="yellow">■■■■</font><font color="black">■■■■■■</font> [(아이콘) 40.5]</pre>
         * <pre><code>
         * builder.durationBar();
         * </code></pre>
         *
         * @return {@link Builder}
         * @throws IllegalArgumentException 능력이 {@link Skill}을 상속받지 않으면 발생
         */
        @NonNull
        public Builder durationBar() {
            Validate.isTrue(ability instanceof Skill, "ability가 Skill을 상속받지 않음");
            return durationBar(((Skill) ability).getDuration(), ((Skill) ability).getDefaultDuration());
        }

        /**
         * 표시 막대를 쿨타임 막대로 설정한다.
         *
         * <p>기본적으로 빨간색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 흰색으로 표시한다.</p>
         *
         * <p>Example:</p>
         *
         * <pre><font color="red">■■■■■■</font><font color="black">■■■■</font> [(아이콘) 60.5]</pre>
         * <pre><code>
         * builder.cooldownBar(Timespan.ofSeconds(60.5), Timespan.ofSeconds(100));
         * </code></pre>
         *
         * @param cooldown    남은 시간
         * @param maxCooldown 최대 시간
         * @return {@link Builder}
         */
        @NonNull
        public Builder cooldownBar(@NonNull Timespan cooldown, @NonNull Timespan maxCooldown) {
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

            bar = " " + new StringJoiner(" §f")
                    .add(StringFormUtil.getProgressBar(currentSeconds, maxSeconds, color))
                    .add(MessageFormat.format("[{0}{1} {2}§f]", color, TextIcon.COOLDOWN, currentDisplay));

            return this;
        }

        /**
         * 표시 막대를 동작의 쿨타임 막대로 설정한다.
         *
         * <p>기본적으로 빨간색, 남은 시간이 최대 시간의 1/2 이하일 경우 노란색, 1/4 이하일 경우 흰색으로 표시한다.</p>
         *
         * <p>Example:</p>
         *
         * <pre><font color="red">■■■■■■</font><font color="black">■■■■</font> [(아이콘) 60.5]</pre>
         * <pre><code>
         * builder.cooldownBar();
         * </code></pre>
         *
         * @return {@link Builder}
         * @throws IllegalArgumentException 능력이 {@link Action}을 상속받지 않으면 발생
         */
        @NonNull
        public Builder cooldownBar() {
            Validate.isTrue(ability instanceof Action, "ability가 Action을 상속받지 않음");
            return cooldownBar(((Action) ability).getCooldown(), ((Action) ability).getDefaultCooldown());
        }

        /**
         * 표시 막대를 무기의 탄약 표시 막대로 설정한다.
         *
         * @param length 막대 길이 (글자 수). 1 이상의 값
         * @param symbol 막대 기호
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않거나 능력이 {@link Reloadable}을 상속받지 않으면 발생
         */
        @NonNull
        public Builder ammoBar(int length, char symbol) {
            Validate.isTrue(length >= 1, "length >= 1 (%d)", length);
            Validate.isTrue(ability instanceof Reloadable, "ability가 Reloadable을 상속받지 않음");

            progressBar(((Reloadable) ability).getReloadModule().getRemainingAmmo(), ((Reloadable) ability).getCapacity(), length, symbol);

            String prefix = TextIcon.CAPACITY.toString();

            Weapon weapon = ability.getCombatUser().getAbilityManager().getWeapon();
            if (weapon instanceof Swappable && (ability == weapon) != ((Swappable<?>) weapon).getSwapModule().isSwapped())
                prefix = "§a" + prefix;

            bar = prefix + bar;
            return this;
        }

        /**
         * 접미사를 설정한다.
         *
         * <p>Example:</p>
         *
         * <pre>Test suffix</pre>
         * <pre><code>
         * builder.suffix("Test suffix");
         * </code></pre>
         *
         * @param suffix 접미사
         * @return {@link Builder}
         */
        @NonNull
        public Builder suffix(@NonNull String suffix) {
            if (!suffix.isEmpty())
                this.suffix = " " + suffix;

            return this;
        }

        /**
         * 동작 사용 키 설명을 설정한다.
         *
         * <p>Example:</p>
         *
         * <pre>[1][우클릭] 사용</pre>
         * <pre><code>
         * builder.keyInfo("사용", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK);
         * </code></pre>
         *
         * @param description 설명
         * @param actionKeys  동작 사용 키 목록
         * @return {@link Builder}
         */
        @NonNull
        public Builder keyInfo(@NonNull String description, @NonNull ActionKey @NonNull ... actionKeys) {
            StringJoiner keys = new StringJoiner("][");
            for (ActionKey actionKey : actionKeys)
                keys.add(actionKey.toString());

            keyInfo = MessageFormat.format("  §7[{0}] §f{1}", keys, description);
            return this;
        }

        /**
         * 동작 사용 키 설명을 설정한다.
         *
         * <p>Example:</p>
         *
         * <pre>[1][우클릭] 사용</pre>
         * <pre><code>
         * builder.keyInfo("사용");
         * </code></pre>
         *
         * @param description 설명
         * @return {@link Builder}
         * @throws IllegalArgumentException 능력이 {@link Action}을 상속받지 않으면 발생
         */
        @NonNull
        public Builder keyInfo(@NonNull String description) {
            Validate.isTrue(ability instanceof Action, "ability가 Action을 상속받지 않음");
            return keyInfo(description, ((Action) ability).getDefaultActionKeys().toArray(new ActionKey[0]));
        }

        /**
         * 액션바 표시 처리기를 생성하여 반환한다.
         *
         * @return {@link ActionBarDisplay}
         */
        @NonNull
        public ActionBarDisplay build() {
            return new ActionBarDisplay(this);
        }
    }
}
