package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class MagrittaA2Info extends ActiveSkillInfo<MagrittaA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20L;
    /** 이동속도 증가량 */
    public static final int SPEED = 60;
    /** 지속 시간 (tick) */
    public static final long DURATION = 20;
    @Getter
    private static final MagrittaA2Info instance = new MagrittaA2Info();

    private MagrittaA2Info() {
        super(MagrittaA2.class, "불꽃의 그림자",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("짧은 시간동안 <:WALK_SPEED_INCREASE:이동 속도>가 빨라지며 모든 공격을 받지 않습니다. " +
                                "사용 후 기본 무기를 재장전합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.RIGHT_CLICK)
                        .build()
                )
        );
    }
}
