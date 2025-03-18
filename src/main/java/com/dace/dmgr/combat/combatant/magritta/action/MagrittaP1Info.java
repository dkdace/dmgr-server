package com.dace.dmgr.combat.combatant.magritta.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class MagrittaP1Info extends PassiveSkillInfo<MagrittaP1> {
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 100;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 15;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2);

    @Getter
    private static final MagrittaP1Info instance = new MagrittaP1Info();

    private MagrittaP1Info() {
        super(MagrittaP1.class, "방화광",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("근처에 <:FIRE:불>타는 적이 존재하면 <:HEAL:회복>합니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, Format.PER_SECOND, HEAL_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, DETECT_RADIUS)
                        .build()));
    }
}
