package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.JumpModule;
import lombok.NonNull;

/**
 * 점프가 가능한 엔티티의 인터페이스.
 */
public interface Jumpable extends Movable {
    /**
     * @return 점프 모듈
     */
    @NonNull
    JumpModule getMoveModule();

    /**
     * 엔티티가 점프할 수 있는 지 확인한다.
     *
     * @return 점프 가능 여부
     * @implSpec {@code true}
     */
    default boolean canJump() {
        return true;
    }
}
