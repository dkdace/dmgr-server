package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.character.silia.Silia;
import lombok.Getter;

public final class SiliaA3Info extends ActiveSkillInfo<SiliaA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 20;
    /** 강제 해제 쿨타임 (tick) */
    public static final long COOLDOWN_FORCE = 5 * 20L;
    /** 강제 해제 피해량 비율 */
    public static final double CANCEL_DAMAGE_RATIO = 0.1;
    /** 이동속도 증가량 */
    public static final int SPEED = 20;
    /** 최대 지속시간 (tick) */
    public static final int MAX_DURATION = 10 * 20;
    /** 일격 활성화 시간 (tick) */
    public static final long ACTIVATE_DURATION = 2 * 20L;
    @Getter
    private static final SiliaA3Info instance = new SiliaA3Info();

    private SiliaA3Info() {
        super(SiliaA3.class, "폭풍전야",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 <:WALK_SPEED_INCREASE:이동 속도>가 빨라지고 발소리 및 모든 행동의 소음이 감소합니다. " +
                                "일정량의 피해를 입으면 해제되며, " + ACTIVATE_DURATION / 20.0 + "초동안 유지하면 다음 기본 공격 시 <d::일격>을 날립니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN_FORCE / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME_WITH_MAX_TIME, MAX_DURATION / 20.0, MAX_DURATION / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                        .addValueInfo(TextIcon.DAMAGE, (int) (Silia.getInstance().getHealth() * CANCEL_DAMAGE_RATIO) + " (강제 해제 피해량)")
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build(),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("해제", ActionKey.SLOT_3)
                                .build()
                        )
                )
        );
    }
}
