package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class ChedUltInfo extends UltimateSkillInfo<ChedUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 10000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (1.5 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 피해량 */
    public static final int DAMAGE = 1500;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 200;
    /** 화염 지대 지속 시간 (tick) */
    public static final long FIRE_FLOOR_DURATION = 8 * 20L;
    /** 화염 지대 범위 (단위: 블록) */
    public static final double FIRE_FLOOR_RADIUS = 7;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    /** 궁극기 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 2 * 20L;
    @Getter
    private static final ChedUltInfo instance = new ChedUltInfo();

    private ChedUltInfo() {
        super(ChedUlt.class, "피닉스 스트라이크",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 관통하는 불사조를 날려보내 적과 부딪히면 크게 폭발하여 <:DAMAGE:광역 피해>를 입히고 <3::화염 지대>를 만듭니다. " +
                                "플레이어가 아닌 적은 통과합니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, DAMAGE, DAMAGE / 2)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build(),
                        new ActionInfoLore.NamedSection("화염 지대", ActionInfoLore.Section
                                .builder("지속적인 <:FIRE:화염 피해>를 입히는 지역입니다.")
                                .addValueInfo(TextIcon.DURATION, Format.TIME, FIRE_FLOOR_DURATION / 20.0)
                                .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, FIRE_FLOOR_RADIUS)
                                .build()
                        )
                )
        );
    }
}
