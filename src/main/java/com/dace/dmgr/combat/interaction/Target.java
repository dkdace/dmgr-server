package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 대상 지정 (타겟팅)을 위한 히트스캔을 관리하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class Target<T extends CombatEntity> extends Hitscan<T> {
    /** 판정 크기 (단위: 블록) */
    private static final double SIZE = 1;

    /** 실패 시 경고 전송 여부 */
    private final boolean alertOnFail;
    /** 찾은 엔티티 */
    @Nullable
    private T currentTarget = null;

    /**
     * 대상 지정(타겟팅) 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter         발사자
     * @param maxDistance     최대 거리. (단위: 블록). 0 이상의 값
     * @param alertOnFail     실패 시 경고 전송 여부
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Target(@NonNull CombatUser shooter, double maxDistance, boolean alertOnFail, @NonNull CombatUtil.EntityCondition<T> entityCondition) {
        super(shooter, entityCondition, Option.builder().size(SIZE).maxDistance(maxDistance).build());
        this.alertOnFail = alertOnFail;
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
            currentTarget = target;
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

    @Override
    protected final void onDestroy(@NonNull Location location) {
        if (alertOnFail && currentTarget == null)
            ((CombatUser) shooter).getUser().sendAlertActionBar("대상을 찾을 수 없습니다.");
    }
}
