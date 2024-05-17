package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class NeaceA2Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20;
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 10;
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 10;
    /** 이동속도 증가량 */
    public static final int SPEED = 10;
    /** 지속시간 (tick) */
    public static final long DURATION = 8 * 20;
    @Getter
    private static final NeaceA2Info instance = new NeaceA2Info();

    private NeaceA2Info() {
        super(2, "축복");
    }

    @Override
    @NonNull
    public NeaceA2 createSkill(@NonNull CombatUser combatUser) {
        return new NeaceA2(combatUser);
    }
}
