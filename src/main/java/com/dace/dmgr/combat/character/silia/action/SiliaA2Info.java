package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class SiliaA2Info extends ActiveSkillInfo<SiliaA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 11 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 이동 강도 */
    public static final double PUSH = 0.8;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 15;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 25;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.8;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    @Getter
    private static final SiliaA2Info instance = new SiliaA2Info();

    private SiliaA2Info() {
        super(SiliaA2.class, "진권풍",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("회오리바람을 날려 적에게 <:DAMAGE:피해>를 입히고 <:KNOCKBACK:공중에 띄웁니다>. " +
                                "적중 시 맞은 적의 뒤로 순간이동합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.RIGHT_CLICK)
                        .build()
                )
        );
    }
}
