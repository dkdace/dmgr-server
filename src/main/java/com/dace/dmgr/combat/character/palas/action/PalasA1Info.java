package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class PalasA1Info extends ActiveSkillInfo<PalasA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (1.2 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 50;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = (long) (1.8 * 20);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final PalasA1Info instance = new PalasA1Info();

    private PalasA1Info() {
        super(PalasA1.class, "테이저건",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("테이저건을 발사하여 약간의 <:DAMAGE:피해>를 입히고 <:STUN:기절>시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()
                )
        );
    }
}
