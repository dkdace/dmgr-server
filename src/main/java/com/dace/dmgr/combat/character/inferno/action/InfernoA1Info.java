package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class InfernoA1Info extends ActiveSkillInfo<InfernoA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.5;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 1.6;
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.6;
    @Getter
    private static final InfernoA1Info instance = new InfernoA1Info();

    private InfernoA1Info() {
        super(InfernoA1.class, "점프 부스터",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("앞으로 높게 도약하여 착지할 때 <:DAMAGE:광역 피해>를 입히고 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()
                )
        );
    }
}
