package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class JagerA2Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 15;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = (long) (1.5 * 20);
    /** 체력 */
    public static final int HEALTH = 400;
    /** 피해량 */
    public static final int DAMAGE = 300;
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = 3 * 20;
    @Getter
    private static final JagerA2Info instance = new JagerA2Info();

    private JagerA2Info() {
        super(2, "곰덫");
    }

    @Override
    @NonNull
    public JagerA2 createSkill(@NonNull CombatUser combatUser) {
        return new JagerA2(combatUser);
    }
}
