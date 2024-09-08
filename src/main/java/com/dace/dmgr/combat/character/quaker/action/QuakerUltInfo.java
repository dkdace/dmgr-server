package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class QuakerUltInfo extends UltimateSkillInfo<QuakerUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 6500;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.8 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 12;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 25;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 1;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = 1 * 20;
    /** 이동 속도 감소량 */
    public static final int SLOW = 30;
    /** 이동 속도 감소 시간 (tick) */
    public static final long SLOW_DURATION = 12 * 20;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 3;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 30;
    @Getter
    private static final QuakerUltInfo instance = new QuakerUltInfo();

    private QuakerUltInfo() {
        super(QuakerUlt.class, "심판의 문지기",
                "",
                "§f▍ 방패로 충격파를 일으켜 §c" + TextIcon.DAMAGE + " 광역 피해§f와 §5" + TextIcon.STUN + " 기절",
                "§f▍ 을 입히고 크게 §5" + TextIcon.KNOCKBACK + " 밀쳐냅니다§f. 맞은 적은 긴",
                "§f▍ 시간동안 §b" + TextIcon.WALK_SPEED_DECREASE + " 이동 속도§f가 느려집니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.STUN, STUN_DURATION / 20.0),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_DECREASE, SLOW),
                MessageFormat.format("§b{0}§f {1}초", TextIcon.WALK_SPEED_DECREASE, SLOW_DURATION / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                "",
                "§7§l[4] §f사용");
    }
}
