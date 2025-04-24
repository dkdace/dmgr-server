package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.EntityCondition;
import lombok.NonNull;

/**
 * 대상 지정 (타겟팅)을 위한 히트스캔을 관리하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class Target<T extends CombatEntity> extends Hitscan<T> {
    /** 판정 크기 (단위: 블록) */
    private static final double SIZE = 1;

    /**
     * 대상 지정(타겟팅) 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter         발사자
     * @param maxDistance     최대 거리. (단위: 블록). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Target(@NonNull CombatUser shooter, double maxDistance, @NonNull EntityCondition<T> entityCondition) {
        super(shooter, entityCondition, Option.builder().size(SIZE).maxDistance(maxDistance).build());
    }

    @Override
    @NonNull
    protected final IntervalHandler getIntervalHandler() {
        return (location, i) -> true;
    }

    @Override
    @NonNull
    protected final HitBlockHandler getHitBlockHandler() {
        return (location, hitBlock) -> false;
    }

    @Override
    @NonNull
    protected final HitEntityHandler<T> getHitEntityHandler() {
        return (location, target) -> {
            onFindEntity(target);
            return false;
        };
    }

    /**
     * 조건에 맞는 대상 엔티티를 찾았을 때 실행할 작업.
     *
     * @param target 대상 엔티티
     */
    protected abstract void onFindEntity(@NonNull T target);
}
