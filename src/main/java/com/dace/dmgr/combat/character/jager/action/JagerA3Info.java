package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

public final class JagerA3Info extends ActiveSkillInfo {
    /** 쿨타임 */
    public static final long COOLDOWN = 14 * 20;
    /** 시전 시간 */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 폭파 시간 */
    public static final long EXPLODE_DURATION = 5 * 20;
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 600;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 50;
    /** 투사체 속력 */
    public static final int VELOCITY = 30;
    /** 피해 범위 */
    public static final int RADIUS = 6;
    /** 빙결량 */
    public static final int FREEZE = 100;
    /** 속박 시간 */
    public static final long SNARE_DURATION = (long) (1.2 * 20);
    @Getter
    private static final JagerA3Info instance = new JagerA3Info();

    public JagerA3Info() {
        super(3, "빙결 수류탄");
    }

    @Override
    public Skill createSkill(CombatUser combatUser) {
        return new JagerA3(combatUser);
    }
}
