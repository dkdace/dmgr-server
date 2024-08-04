package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.Builder;
import lombok.NonNull;

import java.util.function.Predicate;

/**
 * 히트스캔의 선택적 옵션을 관리하는 빌더 클래스.
 *
 * @see Hitscan
 */
@Builder
public final class HitscanOption {
    public static final int TRAIL_INTERVAL_DEFAULT = 14;
    public static final double START_DISTANCE_DEFAULT = 0.5;
    public static final double MAX_DISTANCE_DEFAULT = 70;
    public static final double SIZE_DEFAULT = 0.05;
    public static final double TARGET_SIZE_DEFAULT = 1;
    public static final Predicate<CombatEntity> CONDITION_DEFAULT = combatEntity -> true;

    /** 트레일 이벤트 ({@link Bullet#trail()})를 호출하는 주기. (단위: 판정점 개수) */
    @Builder.Default
    final int trailInterval = TRAIL_INTERVAL_DEFAULT;
    /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록) */
    @Builder.Default
    final double startDistance = START_DISTANCE_DEFAULT;
    /** 총알의 최대 사거리. (단위: 블록) */
    @Builder.Default
    final double maxDistance = MAX_DISTANCE_DEFAULT;
    /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록) */
    @Builder.Default
    final double size = SIZE_DEFAULT;
    /** 대상 엔티티를 찾는 조건 */
    @Builder.Default
    @NonNull
    final Predicate<@NonNull CombatEntity> condition = CONDITION_DEFAULT;
}
