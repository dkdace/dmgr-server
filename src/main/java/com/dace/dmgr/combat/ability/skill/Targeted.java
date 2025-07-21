package com.dace.dmgr.combat.ability.skill;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.interaction.Target;
import lombok.NonNull;

/**
 * 대상 지정(타겟팅) 스킬의 인터페이스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 대상으로 지정 가능한 엔티티
 * @see Target
 */
public interface Targeted<T extends CombatEntity> extends Skill {
    /**
     * 대상 엔티티를 찾는 최대 거리를 반환한다.
     *
     * @return 최대 거리 (단위: 블록)
     */
    int getMaxDistance();

    /**
     * 대상 엔티티를 찾는 조건을 반환한다.
     *
     * @return 대상 엔티티를 찾는 조건
     */
    @NonNull
    EntityCondition<T> getEntityCondition();
}
