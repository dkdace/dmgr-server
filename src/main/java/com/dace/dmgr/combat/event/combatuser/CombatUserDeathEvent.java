package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;
import lombok.Getter;

/**
 * 플레이어가 적 플레이어를 처치로 인한 데스를 처리할때 호출되는 이벤트
 */
@Getter
public class CombatUserDeathEvent extends CombatUserEvent {
    /* 처치한 플레이어 */
    private final CombatUser attacker;

    /**
     * 이벤트를 생성한다.
     *
     * @param attacker 처치한 플레이어 / 이벤트를 호출한 플레이어
     * @param victim   처치당한 플레이어
     */
    public CombatUserDeathEvent(CombatUser attacker, CombatUser victim) {
        super(victim);
        this.attacker = attacker;
    }
}