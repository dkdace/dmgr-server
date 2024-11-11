package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class SiliaT2Info extends TraitInfo {
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 350;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3.7;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 5;
    @Getter
    private static final SiliaT2Info instance = new SiliaT2Info();

    private SiliaT2Info() {
        super("일격",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("특수 공격으로, 칼을 휘둘러 근거리에 <:DAMAGE:광역 피해>를 입히고 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .build()
                )
        );
    }
}
