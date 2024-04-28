package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class JagerA1Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 3 * 20;
    /** 사망 시 쿨타임 (tick) */
    public static final long COOLDOWN_DEATH = 9 * 20;
    /** 소환 최대 거리 (단위: 블록) */
    public static final int SUMMON_MAX_DISTANCE = 15;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = 1 * 20;
    /** 체력 */
    public static final int HEALTH = 600;
    /** 피해량 */
    public static final int DAMAGE = 150;
    /** 이동속도 */
    public static final double SPEED = 0.45;
    /** 치명상 감지 범위 (단위: 블록) */
    public static final double LOW_HEALTH_DETECT_RADIUS = 20;
    /** 체력 최대 회복 시간 (tick) */
    public static final int RECOVER_DURATION = 6 * 20;
    @Getter
    private static final JagerA1Info instance = new JagerA1Info();

    private JagerA1Info() {
        super(1, "설랑");
    }

    @Override
    @NonNull
    public JagerA1 createSkill(@NonNull CombatUser combatUser) {
        return new JagerA1(combatUser);
    }
}
