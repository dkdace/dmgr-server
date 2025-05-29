package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.EntityCondition;
import lombok.NonNull;

/**
 * 근접 공격 히트스캔. 근접 공격에 사용되는 히트스캔을 관리하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class MeleeHitscan<T extends CombatEntity> extends Hitscan<T> {
    /**
     * 근접 공격 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter         발사자
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @param maxDistance     최대 사거리. (단위: 블록). 0.5 이상의 값
     * @param size            판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected MeleeHitscan(@NonNull CombatEntity shooter, @NonNull EntityCondition<T> entityCondition, double maxDistance, double size) {
        super(shooter, entityCondition, Option.builder().maxDistance(maxDistance).size(size).build());
    }

    @Override
    protected final boolean canBeRemoved() {
        return false;
    }
}
