package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class SiliaA3Info extends ActiveSkillInfo {
    /** 쿨타임 */
    public static final long COOLDOWN = 1 * 20;
    /** 강제 해제 쿨타임 */
    public static final long COOLDOWN_FORCE = 5 * 20;
    /** 강제 해제 피해량 비율 */
    public static final double CANCEL_DAMAGE_RATIO = 0.1;
    /** 이동속도 증가량 */
    public static final int SPEED = 20;
    /** 지속시간 */
    public static final long DURATION = 10 * 20;
    /** 일격 활성화 시간 */
    public static final int ACTIVATE_DURATION = 2 * 20;
    @Getter
    private static final SiliaA3Info instance = new SiliaA3Info();

    private SiliaA3Info() {
        super(3, "폭풍전야");
    }

    @Override
    @NonNull
    public SiliaA3 createSkill(@NonNull CombatUser combatUser) {
        return new SiliaA3(combatUser);
    }
}
