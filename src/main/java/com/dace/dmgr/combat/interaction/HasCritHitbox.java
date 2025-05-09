package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 치명타 히트박스를 가진 엔티티의 인터페이스.
 *
 * @see Bullet#createCritHitEntityHandler(Bullet.CritHitEntityHandler)
 */
public interface HasCritHitbox extends CombatEntity {
    /**
     * @return 치명타 히트박스
     */
    @Nullable
    Hitbox getCritHitbox();
}
