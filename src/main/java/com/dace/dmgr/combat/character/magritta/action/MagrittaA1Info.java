package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class MagrittaA1Info extends ActiveSkillInfo<MagrittaA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 폭파 시간 (tick) */
    public static final long EXPLODE_DURATION = 20;
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 250;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 50;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 80;
    /** 화염 지속 시간 (tick) */
    public static final long FIRE_DURATION = 5 * 20L;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3.2;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.5;

    /** 부착 점수 */
    public static final int STUCK_SCORE = 8;
    @Getter
    private static final MagrittaA1Info instance = new MagrittaA1Info();

    private MagrittaA1Info() {
        super(MagrittaA1.class, "태초의 불꽃",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간 후 폭발하는 폭탄을 던져 <:DAMAGE:광역 피해>와 <:FIRE:화염 피해>를 입히고, <d::파쇄>를 적용합니다. " +
                                "적에게 부착할 수 있습니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.FIRE, Format.VARIABLE_TIME_WITH_PER_SECOND,
                                FIRE_DURATION / 20.0, FIRE_DURATION / 2 / 20.0, FIRE_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()
                )
        );
    }
}
