package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;
import lombok.Getter;

/**
 * 플레이어가 적 플레이어를 처치할 때 호출되는 이벤트
 */
@Getter
public class CombatUserKillEvent extends CombatUserEvent {
    /** 처치당한 플레이어 */
    private final CombatUser victim;

    /**
     * 이벤트를 생성한다.
     *
     * @param attacker 처치한 플레이어 / 이벤트를 호출한 플레이어
     * @param victim   처치당한 플레이어
     */
    public CombatUserKillEvent(CombatUser attacker, CombatUser victim) {
        super(attacker);
        this.victim = victim;
    }
}
