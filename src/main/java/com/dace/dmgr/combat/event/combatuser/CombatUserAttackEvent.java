package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;
import lombok.Getter;

/**
 * 플레이어가 적 플레이어에게 피해를 줄 때 호출되는 이벤트
 */
@Getter
public class CombatUserAttackEvent extends CombatUserEvent {
    /** 피해 받은 플레이어 */
    private final CombatUser victim;
    /** 피해 수치 */
    private final int damage;

    /**
     * 이벤트를 생성한다.
     *
     * @param attacker 피해를 준 플레이어 / 이벤트를 호출한 플레이어
     * @param victim   피해 입힌 플레이어
     * @param damage   피해 수치
     */
    public CombatUserAttackEvent(CombatUser attacker, CombatUser victim, int damage) {
        super(attacker);
        this.victim = victim;
        this.damage = damage;
    }
}
