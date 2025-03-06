package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.MoveModule;
import lombok.NonNull;

/**
 * 스스로 움직일 수 있는 엔티티의 인터페이스.
 */
public interface Movable extends CombatEntity {
    /**
     * @return 이동 모듈
     */
    @NonNull
    MoveModule getMoveModule();

    /**
     * 엔티티가 움직일 수 있는지 확인한다.
     *
     * @return 이동 가능 여부
     * @implSpec {@code true}
     */
    default boolean canMove() {
        return true;
    }

    /**
     * 엔티티가 점프할 수 있는지 확인한다.
     *
     * @return 점프 가능 여부
     * @implSpec {@code true}
     */
    default boolean canJump() {
        return true;
    }
}
