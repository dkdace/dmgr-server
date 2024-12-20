package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class NeaceA3Info extends ActiveSkillInfo<NeaceA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 3 * 20L;
    /** 이동 강도 */
    public static final double PUSH = 0.9;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 2 * 20L;
    @Getter
    private static final NeaceA3Info instance = new NeaceA3Info();

    private NeaceA3Info() {
        super(NeaceA3.class, "도움의 손길",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 아군을 향해 날아갑니다.")
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build(),
                        new ActionInfoLore.NamedSection("대상 접근/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("해제", ActionKey.SLOT_3)
                                .build()
                        )
                )
        );
    }
}
