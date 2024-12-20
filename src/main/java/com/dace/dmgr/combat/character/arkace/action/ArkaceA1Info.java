package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class ArkaceA1Info extends ActiveSkillInfo<ArkaceA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 7 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.5 * 20);
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 120;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.25;

    /** 직격 점수 */
    public static final int DIRECT_HIT_SCORE = 3;
    @Getter
    private static final ArkaceA1Info instance = new ArkaceA1Info();

    private ArkaceA1Info() {
        super(ArkaceA1.class, "D.I.A. 코어 미사일",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("소형 미사일을 연속으로 발사하여 <:DAMAGE:광역 피해>를 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.LEFT_CLICK)
                        .build()
                )
        );
    }
}
