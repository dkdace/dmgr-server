package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class JagerA2Info extends ActiveSkillInfo<JagerA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 15;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = (long) (1.5 * 20);
    /** 체력 */
    public static final int HEALTH = 400;
    /** 피해량 */
    public static final int DAMAGE = 300;
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = 3 * 20;

    /** 속박 점수 */
    public static final int SNARE_SCORE = 20;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 10;
    @Getter
    private static final JagerA2Info instance = new JagerA2Info();

    private JagerA2Info() {
        super(JagerA2.class, "곰덫",
                "",
                "§f▍ 눈에 잘 띄지 않는 §3곰덫§f을 던져 설치합니다.",
                "§f▍ 밟은 적은 §c" + TextIcon.DAMAGE + " 피해§f를 입고 §5" + TextIcon.SNARE + " 속박§f됩니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[2] §f사용",
                "",
                "§3[곰덫]",
                "",
                MessageFormat.format("§a{0}§f {1}", TextIcon.HEAL, HEALTH),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.SNARE, SNARE_DURATION / 20.0));
    }
}
