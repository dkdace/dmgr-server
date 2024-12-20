package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class ChedP1Info extends PassiveSkillInfo<ChedP1> {
    /** 벽타기 이동 강도 */
    public static final double PUSH = 0.45;
    /** 벽타기 최대 횟수 */
    public static final int USE_COUNT = 10;
    /** 매달리기 최대 시간 (tick) */
    public static final long HANG_DURATION = 6 * 20L;
    @Getter
    private static final ChedP1Info instance = new ChedP1Info();

    private ChedP1Info() {
        super(ChedP1.class, "궁사의 날렵함",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 클릭하여 벽을 오를 수 있습니다. 오르는 도중 <3::매달리기>를 할 수 있습니다.")
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("매달리기", ActionInfoLore.Section
                                .builder("벽에 매달려 위치를 고정합니다.")
                                .addValueInfo(TextIcon.DURATION, Format.TIME, HANG_DURATION / 20.0)
                                .addActionKeyInfo("사용", ActionKey.SNEAK)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("매달리기: 지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addActionKeyInfo("해제", ActionKey.SNEAK)
                                .build()
                        )
                )
        );
    }
}
