package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class VellionA2Info extends ActiveSkillInfo<VellionA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.8 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 20;
    /** 방어력 감소량 */
    public static final int DEFENSE_DECREMENT = 25;
    /** 대상 위치 통과 불가 시 초기화 제한 시간 (tick) */
    public static final long BLOCK_RESET_DELAY = 2 * 20;

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final VellionA2Info instance = new VellionA2Info();

    private VellionA2Info() {
        super(VellionA2.class, "저주 귀속",
                "",
                "§f▍ 바라보는 적에게 저주를 걸어 §6" + TextIcon.DEFENSE_DECREASE + " 방어력§f을",
                "§f▍ 감소시키고 해당 적을 제외한 주변에 지속적인",
                "§f▍ §c" + TextIcon.DAMAGE + " 광역 피해§f를 입힙니다.",
                "§f▍ 해당 적이 시야에서 2초간 사라지거나 사거리를",
                "§f▍ 벗어나면 저주가 풀립니다.",
                "",
                MessageFormat.format("§5{0}§f {1}m", TextIcon.DISTANCE, MAX_DISTANCE),
                MessageFormat.format("§6{0}§f {1}%", TextIcon.DEFENSE_DECREASE, DEFENSE_DECREMENT),
                MessageFormat.format("§c{0}§f {1}/초", TextIcon.DAMAGE, DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[2] §f사용",
                "",
                "§3[취소/재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[2] §f해제");
    }
}
