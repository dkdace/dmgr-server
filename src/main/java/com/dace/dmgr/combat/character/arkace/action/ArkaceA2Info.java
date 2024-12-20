package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class ArkaceA2Info extends ActiveSkillInfo<ArkaceA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20L;
    /** 치유량 */
    public static final int HEAL = 350;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 치유 점수 */
    public static final int HEAL_SCORE = 8;
    @Getter
    private static final ArkaceA2Info instance = new ArkaceA2Info();

    private ArkaceA2Info() {
        super(ArkaceA2.class, "생체 회복막",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 회복막을 활성화하여 체력을 <:HEAL:회복>합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.HEAL, HEAL)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()
                )
        );
    }
}
