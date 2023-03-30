package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;
import lombok.Getter;

/**
 * 플레이어가 아군 플레이어에게 치유할 때 호출되는 이벤트
 */
@Getter
public class CombatUserHealEvent extends CombatUserEvent {
    /** 치유 받은 플레이어 */
    private final CombatUser victim;
    /** 치유 수치 */
    private final int heal;

    /**
     * 이벤트를 생성한다.
     *
     * @param attacker 치유한 플레이어 / 이벤트를 호출한 플레이어
     * @param victim   치유 받은 플레이어
     * @param heal     치유 수치
     */
    public CombatUserHealEvent(CombatUser attacker, CombatUser victim, int heal) {
        super(attacker);
        this.victim = victim;
        this.heal = heal;
    }
}
