package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 동작 정보의 설명을 표현하는 클래스.
 */
public final class ActionInfoLore {
    /** 섹션 문자열 목록 */
    private final String[] sections;

    /**
     * 동작 정보 설명 인스턴스를 생성한다.
     *
     * @param defaultSection 기본 설명 섹션
     * @param sections       설명 섹션 목록
     */
    public ActionInfoLore(@NonNull Section defaultSection, @NonNull NamedSection @NonNull ... sections) {
        this.sections = new String[sections.length + 1];
        this.sections[0] = defaultSection.toString();
        for (int i = 0; i < sections.length; i++)
            this.sections[i + 1] = sections[i].toString();
    }

    /**
     * 동작 정보 설명 인스턴스를 생성한다.
     *
     * @param defaultSection 기본 설명 섹션
     */
    public ActionInfoLore(@NonNull Section defaultSection) {
        this.sections = new String[]{defaultSection.toString()};
    }

    /**
     * 동작 정보 설명의 전체 문자열을 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * //
     * // ▍ 동작 개요에 대한 내용
     * //
     * // \u4DC4 100
     * // \u4DC0 100 ~ 50
     * //
     * // [1] [좌클릭] 사용 [우클릭] 해제
     * //
     * // [재사용 시]
     * //
     * // ▍ 아군을 \u4DC4 치유한다.
     * //
     * // \u4DC4 100
     * //
     * // [1] 사용
     *
     * ActionInfoLore lore = new ActionInfoLore(Section
     *     .builder("동작 개요에 대한 내용")
     *     .addValueInfo(TextIcon.HEAL, ChatColor.GREEN, 100)
     *     .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, ChatColor.RED, 100, 50);
     *     .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.LEFT_CLICK)
     *     .addActionKeyInfo("해제", ActionKey.RIGHT_CLICK);
     *     .build(),
     *     new NamedSection("재사용 시", Section
     *         .builder("아군을 <a:HEAL:치유>한다.")
     *         .addValueInfo(ChatColor.GREEN, TextIcon.HEAL, 100)
     *         .addActionKeyInfo("사용", ActionKey.SLOT_1)
     *         .build()
     *     )
     * );
     * lore.toString();
     * </code></pre>
     *
     * @return 전체 문자열 ('\n'으로 줄바꿈)
     */
    @Override
    public String toString() {
        return String.join("\n\n", sections);
    }

    /**
     * 동작 설명의 섹션을 나타내는 클래스.
     */
    public static final class Section {
        /** 개요 줄바꿈 기준 길이 */
        private static final int SUMMARY_WRAP_LENGTH = 24;
        /** 개요 패턴 정규식. <code><색상 코드:TextIcon 이름:설명></code> 형식을 나타낸다. */
        private static final Pattern SUMMARY_PLACEHOLDER_PATTERN = Pattern.compile("<[0-9a-f]?:[A-Z_]+:[^\n]+?>");
        /** 개요 문자열 접두사 */
        private static final String SUMMARY_PREFIX = "§f▍ ";

        /** 개요 문자열 목록 */
        @NonNull
        private final String[] summaries;
        /** 수치 상세 설명 문자열 목록 */
        @NonNull
        private final String @Nullable [] valueInfos;
        /** 동작 사용 키 설명 문자열 목록 */
        @NonNull
        private final String @Nullable [] actionKeyInfos;

        /**
         * 동작 설명 섹션 인스턴스를 생성한다.
         *
         * @param sectionBuilder 설명 섹션 빌더
         */
        private Section(@NonNull SectionBuilder sectionBuilder) {
            this.summaries = parseSummary(sectionBuilder.summary);
            ArrayList<ValueInfo> valueInfoList = sectionBuilder.valueInfos;
            ArrayList<ActionKeyInfo> actionKeyInfoList = sectionBuilder.actionKeyInfos;

            if (valueInfoList == null)
                this.valueInfos = null;
            else {
                this.valueInfos = new String[valueInfoList.size()];
                for (int i = 0; i < valueInfoList.size(); i++) {
                    ValueInfo valueInfo = valueInfoList.get(i);
                    this.valueInfos[i] = MessageFormat.format(
                            MessageFormat.format("{0}{1} §f{2}", valueInfo.color, valueInfo.textIcon, valueInfo.pattern),
                            valueInfo.arguments);
                }
            }

            if (actionKeyInfoList == null)
                this.actionKeyInfos = null;
            else {
                this.actionKeyInfos = new String[actionKeyInfoList.size()];
                for (int i = 0; i < actionKeyInfoList.size(); i++) {
                    ActionKeyInfo actionKeyInfo = actionKeyInfoList.get(i);
                    this.actionKeyInfos[i] = MessageFormat.format("§7§l[{0}] §f{1}",
                            Arrays.stream(actionKeyInfo.actionKeys).map(ActionKey::getName).collect(Collectors.joining("] [")),
                            actionKeyInfo.description);
                }
            }
        }

        /**
         * 개요 문자열을 파싱한다.
         *
         * @param summary 원본 문자열
         * @return 파싱된 문자열 목록
         */
        private static @NonNull String @NonNull [] parseSummary(@NonNull String summary) {
            Matcher matcher = SUMMARY_PLACEHOLDER_PATTERN.matcher(summary);
            StringBuffer result = new StringBuffer();
            ArrayList<String> formattedTexts = new ArrayList<>();
            ArrayList<String> formattedTempTexts = new ArrayList<>();

            while (matcher.find()) {
                String group = matcher.group();
                String[] texts = group.substring(1, group.length() - 1).split(":");

                TextIcon textIcon = TextIcon.valueOf(texts[1]);
                if (texts[0].isEmpty())
                    texts[0] = String.valueOf(textIcon.getDefaultColor().getChar());

                String formatted = MessageFormat.format("§{0}{1} {2}§f", texts[0], textIcon, texts[2]);
                String formattedTemp = ChatColor.stripColor(formatted).replace(" ", "\u3000");
                matcher.appendReplacement(result, formattedTemp);

                formattedTexts.add(formatted);
                formattedTempTexts.add(formattedTemp);
            }
            matcher.appendTail(result);

            String wrappedSummary = WordUtils.wrap(
                    SUMMARY_PREFIX + WordUtils.wrap(result.toString(), SUMMARY_WRAP_LENGTH, "\n" + SUMMARY_PREFIX, false),
                    1, ". \n" + SUMMARY_PREFIX, false, "\\. ");
            return StringUtils.replaceEachRepeatedly(wrappedSummary,
                    formattedTempTexts.toArray(new String[0]), formattedTexts.toArray(new String[0])).split("\n");
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * <p>Example:</p>
         *
         * <pre><code>
         * //
         * // ▍ 아군을 \u4DC4 치유한다.
         * Section section = Section.builder("아군을 <a:HEAL:치유>한다.").build();
         * section.toString();
         * </code></pre>
         *
         * @param summary 개요. 정규식 {@link Section#SUMMARY_PLACEHOLDER_PATTERN}을
         *                포함할 수 있는 문자열
         * @return {@link SectionBuilder}
         */
        @NonNull
        public static SectionBuilder builder(@NonNull String summary) {
            return new SectionBuilder(summary);
        }

        /**
         * 설명 섹션의 전체 문자열을 반환한다.
         *
         * <p>Example:</p>
         *
         * <pre><code>
         * //
         * // ▍ 동작 개요에 대한 내용
         * //
         * // \u4DC4 100
         * // \u4DC0 100 ~ 50
         * //
         * // [1] [좌클릭] 사용 [우클릭] 해제
         *
         * Section useSection = Section
         *     .builder("동작 개요에 대한 내용")
         *     .addValueInfo(TextIcon.HEAL, ChatColor.GREEN, 100)
         *     .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, ChatColor.RED, 100, 50);
         *     .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.LEFT_CLICK)
         *     .addActionKeyInfo("해제", ActionKey.RIGHT_CLICK);
         *     .build();
         * useSection.toString();
         * </code></pre>
         *
         * @return 전체 문자열 ('\n'으로 줄바꿈)
         */
        @Override
        public String toString() {
            StringJoiner lore = new StringJoiner("\n");

            lore.add("");
            for (String summary : summaries)
                lore.add(summary);

            if (valueInfos != null) {
                lore.add("");
                for (String valueInfo : valueInfos)
                    lore.add(valueInfo);
            }

            if (actionKeyInfos != null) {
                lore.add("");
                StringJoiner actionKeyInfoText = new StringJoiner(" ");
                for (String actionKeyInfo : actionKeyInfos)
                    actionKeyInfoText.add(actionKeyInfo);
                lore.add(actionKeyInfoText.toString());
            }

            return lore.toString();
        }

        /**
         * 수치 상세 설명에서 지정할 수 있는 포맷 형식의 목록.
         */
        @AllArgsConstructor
        public enum Format {
            /** 기본 */
            DEFAULT("{0}"),
            /** 가변 값 */
            VARIABLE("{0} ~ {1}"),
            /** 거리별 가변 값. 거리별 피해량 등 */
            VARIABLE_WITH_DISTANCE("{0} ~ {1} ({2}m~{3}m)"),
            /** 시간. 쿨타임, 지속시간 등 */
            TIME("{0}초"),
            /** 시간 및 RPM. 무기의 연사속도 등 */
            TIME_WITH_RPM("{0}초 ({1}/분)"),
            /** 장탄수 */
            CAPACITY("{0}발"),
            /** 시간당 값. 초당 피해량, 초당 치유량 등 */
            PER_SECOND("{0}/초"),
            /** 퍼센트 */
            PERCENT("{0}%"),
            /** 거리 */
            DISTANCE("{0}m"),
            /** 시간 및 시간당 값. 지속시간과 초당 피해량 등 */
            TIME_WITH_PER_SECOND("{0}초 / {1}/초"),
            /** 가변 시간 및 시간당 값. 거리별 지속시간과 초당 피해량 등 */
            VARIABLE_TIME_WITH_PER_SECOND("{0}초 ~ {1}초 / {2}/초");

            /** 문자열 패턴 */
            private final String pattern;

            @Override
            public String toString() {
                return pattern;
            }
        }

        /**
         * 동작 설명의 섹션 인스턴스를 생성하는 빌더 클래스.
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class SectionBuilder {
            @NonNull
            private final String summary;
            @Nullable
            private ArrayList<ValueInfo> valueInfos;
            @Nullable
            private ArrayList<ActionKeyInfo> actionKeyInfos;

            /**
             * 수치 상세 설명을 추가한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // <빨간색>\u4DDE <흰색>10 + 20
             * builder.addValueInfo(TextIcon.DAMAGE, "{0} + {1}", ChatColor.RED, 10, 20);
             * </code></pre>
             *
             * @param textIcon  텍스트 아이콘
             * @param pattern   문자열 패턴
             * @param color     텍스트 아이콘 색상
             * @param arguments 포맷에 사용할 인자 목록
             * @return {@link SectionBuilder}
             */
            @NonNull
            public SectionBuilder addValueInfo(@NonNull TextIcon textIcon, @NonNull String pattern, @NonNull ChatColor color,
                                               @NonNull Object @NonNull ... arguments) {
                if (valueInfos == null)
                    valueInfos = new ArrayList<>();

                valueInfos.add(new ValueInfo(textIcon, pattern, color, arguments));
                return this;
            }

            /**
             * 수치 상세 설명을 추가한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // <빨간색>\u4DDE <흰색>10 + 20
             * builder.addValueInfo(TextIcon.DAMAGE, "{0} + {1}", 10, 20);
             * </code></pre>
             *
             * @param textIcon  텍스트 아이콘
             * @param pattern   문자열 패턴
             * @param arguments 포맷에 사용할 인자 목록
             * @return {@link SectionBuilder}
             */
            @NonNull
            public SectionBuilder addValueInfo(@NonNull TextIcon textIcon, @NonNull String pattern, @NonNull Object @NonNull ... arguments) {
                return addValueInfo(textIcon, pattern, textIcon.getDefaultColor(), arguments);
            }

            /**
             * 수치 상세 설명을 추가한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // <빨간색>\u4DDE <흰색>10m
             * builder.addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, ChatColor.RED, 10);
             * // <빨간색>\u4DC0 <흰색>100 ~ 50
             * builder.addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, ChatColor.RED, 100, 50);
             * </code></pre>
             *
             * @param textIcon  텍스트 아이콘
             * @param format    포맷 형식
             * @param color     텍스트 아이콘 색상
             * @param arguments 포맷에 사용할 인자 목록
             * @return {@link SectionBuilder}
             */
            @NonNull
            public SectionBuilder addValueInfo(@NonNull TextIcon textIcon, @NonNull Format format, @NonNull ChatColor color,
                                               @NonNull Object @NonNull ... arguments) {
                return addValueInfo(textIcon, format.pattern, color, arguments);
            }

            /**
             * 수치 상세 설명을 추가한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // <흰색>\u4DDE <흰색>10m
             * builder.addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, 10);
             * // <빨간색>\u4DC0 <흰색>100 ~ 50
             * builder.addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, 100, 50);
             * </code></pre>
             *
             * @param textIcon  텍스트 아이콘
             * @param format    포맷 형식
             * @param arguments 포맷에 사용할 인자 목록
             * @return {@link SectionBuilder}
             */
            @NonNull
            public SectionBuilder addValueInfo(@NonNull TextIcon textIcon, @NonNull Format format, @NonNull Object @NonNull ... arguments) {
                return addValueInfo(textIcon, format.pattern, textIcon.getDefaultColor(), arguments);
            }

            /**
             * 수치 상세 설명을 추가한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // <빨간색>\u4DC0 <흰색>100
             * builder.addValueInfo(TextIcon.DAMAGE, ChatColor.RED, 100);
             * </code></pre>
             *
             * @param textIcon 텍스트 아이콘
             * @param color    텍스트 아이콘 색상
             * @param argument 값
             * @return {@link SectionBuilder}
             */
            @NonNull
            public SectionBuilder addValueInfo(@NonNull TextIcon textIcon, @NonNull ChatColor color, @NonNull Object argument) {
                return addValueInfo(textIcon, Format.DEFAULT.pattern, color, argument);
            }

            /**
             * 수치 상세 설명을 추가한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // <빨간색>\u4DC0 <흰색>100
             * builder.addValueInfo(TextIcon.DAMAGE, 100);
             * </code></pre>
             *
             * @param textIcon 텍스트 아이콘
             * @param argument 값
             * @return {@link SectionBuilder}
             */
            @NonNull
            public SectionBuilder addValueInfo(@NonNull TextIcon textIcon, @NonNull Object argument) {
                return addValueInfo(textIcon, Format.DEFAULT.pattern, textIcon.getDefaultColor(), argument);
            }

            /**
             * 동작 사용 키 설명을 추가한다.
             *
             * <p>Example:</p>
             *
             * <pre><code>
             * // [1] [좌클릭] 사용 [우클릭] 해제
             * builder
             *     .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.LEFT_CLICK)
             *     .addActionKeyInfo("해제", ActionKey.RIGHT_CLICK);
             * </code></pre>
             *
             * @param description 동작 설명
             * @param actionKeys  동작 사용 키 목록
             * @return {@link SectionBuilder}
             */
            @NonNull
            public SectionBuilder addActionKeyInfo(@NonNull String description, @NonNull ActionKey @NonNull ... actionKeys) {
                if (actionKeyInfos == null)
                    actionKeyInfos = new ArrayList<>();

                actionKeyInfos.add(new ActionKeyInfo(description, actionKeys));
                return this;
            }

            /**
             * 동작 설명 섹션을 생성하여 반환한다.
             *
             * @return {@link Section}
             */
            @NonNull
            public Section build() {
                return new Section(this);
            }
        }

        /**
         * 수치 상세 설명을 나타내는 클래스.
         */
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        private static final class ValueInfo {
            /** 텍스트 아이콘 */
            @NonNull
            private final TextIcon textIcon;
            /** 문자열 패턴 */
            @NonNull
            private final String pattern;
            /** 텍스트 아이콘 색상 */
            @NonNull
            private final ChatColor color;
            /** 포맷에 사용할 인자 목록 */
            @NonNull
            private final Object[] arguments;
        }

        /**
         * 동작 사용 키 설명을 나타내는 클래스.
         */
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        private static final class ActionKeyInfo {
            /** 동작 설명 */
            @NonNull
            private final String description;
            /** 동작 사용 키 목록 */
            @NonNull
            private final ActionKey @NonNull [] actionKeys;
        }
    }

    /**
     * 이름이 지정된 설명 섹션을 나타내는 클래스.
     */
    @AllArgsConstructor
    public static final class NamedSection {
        /** 이름 */
        @NonNull
        private final String name;
        /** 설명 섹션 */
        @NonNull
        private final Section section;

        /**
         * 이름이 지정된 설명 섹션의 전체 문자열을 반환한다.
         *
         * <p>Example:</p>
         *
         * <pre><code>
         * // [재사용 시]
         * //
         * // ▍ 아군을 \u4DC4 치유한다.
         * //
         * // \u4DC4 100
         * //
         * // [1] 사용
         *
         * NamedSection useSection = new NamedSection("재사용 시", Section
         *     .builder("아군을 <a:HEAL:치유>한다.")
         *     .addValueInfo(TextIcon.HEAL, ChatColor.GREEN, 100)
         *     .addActionKeyInfo("사용", ActionKey.SLOT_1)
         *     .build());
         * useSection.toString();
         * </code></pre>
         *
         * @return 전체 문자열 ('\n'으로 줄바꿈)
         */
        @Override
        public String toString() {
            StringJoiner lore = new StringJoiner("\n");

            lore.add("§3[" + name + "]");
            lore.add(section.toString());

            return lore.toString();
        }
    }
}