package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class QuakerA3Info extends ActiveSkillInfo {
    /** 쿨타임 */
    public static final long COOLDOWN = 10 * 20;
    /** 시전 시간 */
    public static final long READY_DURATION = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 사거리 */
    public static final double DISTANCE = 40;
    /** 투사체 속력 */
    public static final int VELOCITY = 10;
    /** 투사체 크기 */
    public static final double SIZE = 2.5;
    /** 속박 시간 */
    public static final long SNARE_DURATION = (long) (0.3 * 20);
    @Getter
    private static final QuakerA3Info instance = new QuakerA3Info();

    public QuakerA3Info() {
        super(3, "돌풍 강타");
    }

    @Override
    public @NonNull QuakerA3 createSkill(@NonNull CombatUser combatUser) {
        return new QuakerA3(combatUser);
    }
}
