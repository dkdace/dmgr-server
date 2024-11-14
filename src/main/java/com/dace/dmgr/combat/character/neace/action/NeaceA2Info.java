package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class NeaceA2Info extends ActiveSkillInfo<NeaceA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 7 * 20L;
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 15;
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 15;
    /** 지속시간 (tick) */
    public static final long DURATION = 8 * 20L;

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final NeaceA2Info instance = new NeaceA2Info();

    private NeaceA2Info() {
        super(NeaceA2.class, "축복",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 기본 무기의 치유 대상을 <3::축복>할 수 있습니다. " +
                                "사용 중에는 기본 무기로 치유할 수 없습니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("축복", ActionInfoLore.Section
                                .builder("<:DAMAGE_INCREASE:공격력>과 <:DEFENSE_INCREASE:방어력>이 증가합니다.")
                                .addValueInfo(TextIcon.DAMAGE_INCREASE, Format.PERCENT, DAMAGE_INCREMENT)
                                .addValueInfo(TextIcon.DEFENSE_INCREASE, Format.PERCENT, DEFENSE_INCREMENT)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("해제", ActionKey.SLOT_2)
                                .build()
                        )
                )
        );
    }
}
