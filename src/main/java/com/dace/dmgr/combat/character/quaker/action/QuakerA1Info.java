package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class QuakerA1Info extends ActiveSkillInfo<QuakerA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 1 * 20;
    /** 사망 시 쿨타임 (tick) */
    public static final long COOLDOWN_DEATH = 4 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 체력 */
    public static final int HEALTH = 5000;
    /** 체력 최대 회복 시간 (tick) */
    public static final int RECOVER_DURATION = 12 * 20;
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SLOW = 25;

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 50;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 20;
    @Getter
    private static final QuakerA1Info instance = new QuakerA1Info();

    private QuakerA1Info() {
        super(QuakerA1.class, "불굴의 방패",
                "",
                "§f▍ 커다란 §3방패§f를 펼쳐 전방의 공격을 방어합니다.",
                "§f▍ 사용 중에는 §b" + TextIcon.WALK_SPEED_DECREASE + " 이동 속도§f가 느려집니다.",
                "",
                MessageFormat.format("§f{0} {1}초 (파괴 시)", TextIcon.COOLDOWN, COOLDOWN_DEATH / 20.0),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_DECREASE, USE_SLOW),
                "",
                "§7§l[1] [우클릭] §f사용",
                "",
                "§3[방패]",
                "",
                MessageFormat.format("§a{0}§f {1}", TextIcon.HEAL, HEALTH),
                "",
                "§3[재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[1] [우클릭] §f해제");
    }
}
