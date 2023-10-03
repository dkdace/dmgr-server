package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

public final class JagerUltInfo extends UltimateSkillInfo {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9000;
    /** 시전 시간 */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 투사체 속력 */
    public static final int VELOCITY = 30;
    /** 소환 시간 */
    public static final long SUMMON_DURATION = 1 * 20;
    /** 체력 */
    public static final int HEALTH = 1000;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 100;
    /** 최소 피해 범위 */
    public static final int MIN_RADIUS = 4;
    /** 최대 피해 범위 */
    public static final int MAX_RADIUS = 12;
    /** 최대 피해 범위에 도달하는 시간 */
    public static final long MAX_RADIUS_DURATION = 5 * 20;
    /** 초당 빙결량 */
    public static final int FREEZE_PER_SECOND = 40;
    /** 지속시간 */
    public static final long DURATION = 20 * 20;
    @Getter
    private static final JagerUltInfo instance = new JagerUltInfo();

    public JagerUltInfo() {
        super("백야의 눈폭풍");
    }

    @Override
    public Skill createSkill(CombatUser combatUser) {
        return new JagerUlt(combatUser);
    }
}
