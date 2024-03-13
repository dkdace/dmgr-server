package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.MoveModule;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import lombok.NonNull;

/**
 * 움직일 수 있는 엔티티의 인터페이스.
 */
public interface Movable extends CombatEntity {
    /**
     * @return 이동 모듈
     */
    @NonNull
    MoveModule getMoveModule();

    /**
     * 엔티티가 움직일 수 있는 지 확인한다.
     *
     * <p>기본값은 기절과 속박이 걸린 상태가 아니면 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 이동 가능 여부
     */
    default boolean canMove() {
        return !getStatusEffectModule().hasStatusEffect(StatusEffectType.STUN) && !getStatusEffectModule().hasStatusEffect(StatusEffectType.SNARE);
    }
}
