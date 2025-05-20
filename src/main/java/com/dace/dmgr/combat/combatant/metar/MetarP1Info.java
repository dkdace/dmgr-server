package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class MetarP1Info extends PassiveSkillInfo<MetarP1> {
    /** 넉백 저항 증가량 */
    public static final double KNOCKBACK_RESISTANCE_INCREMENT = 0.8;
    /** 감소량 배수 */
    public static final double DECREASE_MULTIPLIER = 40;
    /** 최대치 */
    public static final int MAX = 100;
    /** 초당 충전량 */
    public static final int RECOVER_PER_SECOND = 7;

    @Getter
    private static final MetarP1Info instance = new MetarP1Info();

    private MetarP1Info() {
        super(MetarP1.class, "중기갑",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("수치에 비례하여 <:KNOCKBACK:밀쳐내기> 효과를 적게 받습니다. 적에 의해 밀려나면 감소합니다. " +
                                "자신의 밀쳐내기 효과는 받지 않습니다.")
                        .addValueInfo(TextIcon.UNDEFINED, "최대 {0}", MAX)
                        .addValueInfo(TextIcon.UNDEFINED, "초당 +{0}%", RECOVER_PER_SECOND)
                        .addValueInfo(TextIcon.KNOCKBACK, "(중기갑)×{0}", KNOCKBACK_RESISTANCE_INCREMENT)
                        .build()));
    }
}
