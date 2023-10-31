package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.MoveModule;

/**
 * 움직일 수 있는 엔티티의 인터페이스.
 */
public interface Movable extends CombatEntity {
    /**
     * @return 이동 모듈
     */
    MoveModule getMoveModule();
}
