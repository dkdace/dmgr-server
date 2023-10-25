package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.entity.CombatEntityBase;
import com.dace.dmgr.combat.entity.SummonEntity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;

/**
 * 여러 엔티티를 소환할 수 있는 스킬의 인터페이스.
 *
 * @param <T> {@link SummonEntity}를 상속받는 타입
 * @see HasEntity
 */
public interface HasEntities<T extends SummonEntity<?>> extends Skill {
    /**
     * 소환된 엔티티 목록을 반환한다.
     *
     * @return 소환된 엔티티 목록
     */
    List<T> getSummonEntities();

    /**
     * 소환된 엔티티를 모두 제거한다.
     */
    default void clearSummonEntities() {
        getSummonEntities().forEach(CombatEntityBase::remove);
        getSummonEntities().clear();
    }

    @Override
    @MustBeInvokedByOverriders
    default void onRemove() {
        clearSummonEntities();
    }

    @Override
    @MustBeInvokedByOverriders
    default void onReset() {
        clearSummonEntities();
    }
}