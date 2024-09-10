package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class QuakerA2Info extends ActiveSkillInfo<QuakerA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = 1 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 1.2;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = (long) (0.8 * 20);
    /** 이동 속도 감소량 */
    public static final int SLOW = 40;
    /** 이동 속도 감소 시간 (tick) */
    public static final long SLOW_DURATION = (long) (2.8 * 20);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final QuakerA2Info instance = new QuakerA2Info();

    private QuakerA2Info() {
        super(QuakerA2.class, "충격파 일격",
                "",
                "§f▍ 바닥을 내려찍어 충격파를 일으켜 §c" + TextIcon.DAMAGE + " 광역 피해§f와",
                "§f▍ §5" + TextIcon.STUN + " 기절§f을 입히고 §b" + TextIcon.WALK_SPEED_DECREASE + " 이동 속도§f를 감소시킵니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.STUN, STUN_DURATION / 20.0),
                MessageFormat.format("§b{0}§f {1}초 / {2}%", TextIcon.WALK_SPEED_DECREASE, SLOW_DURATION / 20.0, SLOW),
                "",
                "§7§l[2] §f사용");
    }
}
