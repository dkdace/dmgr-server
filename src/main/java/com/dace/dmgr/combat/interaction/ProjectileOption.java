package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.Builder;

import java.util.function.Predicate;

/**
 * 투사체의 선택적 옵션을 관리하는 빌더 클래스.
 *
 * @see Projectile
 */
@Builder
public final class ProjectileOption {
    /** 트레일 이벤트 ({@link Bullet#trail()})를 호출하는 주기. (단위: 판정점 개수) */
    @Builder.Default
    final int trailInterval = 14;
    /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록) */
    @Builder.Default
    final double startDistance = 0.5;
    /** 총알의 최대 사거리. (단위: 블록) */
    @Builder.Default
    final double maxDistance = 70;
    /** 투사체가 유지되는 시간 (tick). -1로 설정 시 무한 지속 */
    @Builder.Default
    final long duration = -1;
    /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록) */
    @Builder.Default
    final double size = 0.13;
    /** 중력 작용 여부 */
    @Builder.Default
    final boolean hasGravity = false;
    /** 대상 엔티티를 찾는 조건 */
    @Builder.Default
    final Predicate<CombatEntity> condition = combatEntity -> true;
}
