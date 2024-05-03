package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.NonNull;

/**
 * 치명타 히트박스를 가진 엔티티의 인터페이스.
 */
public interface HasCritHitbox extends CombatEntity {
    /**
     * @return 치명타 히트박스
     */
    @NonNull
    Hitbox getCritHitbox();
}
