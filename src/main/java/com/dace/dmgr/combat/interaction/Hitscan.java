package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.EntityCondition;
import lombok.Builder;
import lombok.NonNull;

/**
 * 히트스캔. 광선과 같이 사용 즉시 판정이 발생하는 총알을 관리하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class Hitscan<T extends CombatEntity> extends Bullet<T> {
    /**
     * 히트스캔 인스턴스를 생성한다.
     *
     * <p>히트스캔의 선택적 옵션은 {@link Option}을 통해 전달받는다.</p>
     *
     * @param shooter         발사자
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @param option          히트스캔의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Option
     */
    protected Hitscan(@NonNull CombatEntity shooter, @NonNull EntityCondition<T> entityCondition, @NonNull Option option) {
        super(shooter, option.startDistance, option.maxDistance, option.size, entityCondition);
    }

    /**
     * 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter         발사자
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Option
     */
    protected Hitscan(@NonNull CombatEntity shooter, @NonNull EntityCondition<T> entityCondition) {
        this(shooter, entityCondition, Option.builder().build());
    }

    @Override
    final void onShot() {
        while (getDistanceFromStart() < maxDistance) {
            next();
            if (isDestroyed())
                return;
        }

        destroy();
    }

    /**
     * 히트스캔의 선택적 옵션을 관리하는 클래스.
     */
    @Builder
    public static final class Option {
        /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록). 0 이상의 값 */
        @Builder.Default
        private final double startDistance = 0.5;
        /** 총알의 최대 사거리. (단위: 블록). {@code startDistance} 이상의 값 */
        @Builder.Default
        private final double maxDistance = 70;
        /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록). 0 이상의 값 */
        @Builder.Default
        private final double size = 0.05;
    }
}
