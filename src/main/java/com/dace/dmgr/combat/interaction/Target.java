package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.NonNull;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * 대상 지정 (타겟팅)을 위한 히트스캔을 관리하는 클래스.
 */
public abstract class Target extends Hitscan {
    /** 판정 크기 (단위: 블록) */
    private static final double SIZE = 1;

    /** 실패 시 경고 전송 여부 */
    private final boolean alertOnFail;
    /** 찾은 엔티티 */
    @Nullable
    private Damageable currentTarget = null;

    /**
     * 대상 지정(타겟팅) 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter     발사자
     * @param maxDistance 최대 거리. (단위: 블록). 0 이상의 값
     * @param alertOnFail 실패 시 경고 전송 여부
     * @param condition   대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Target(@NonNull CombatUser shooter, double maxDistance, boolean alertOnFail, @NonNull Predicate<@NonNull CombatEntity> condition) {
        super(shooter, HitscanOption.builder().size(SIZE).maxDistance(maxDistance)
                .condition(condition.and(combatEntity -> combatEntity != shooter)).build());

        this.alertOnFail = alertOnFail;
    }

    @Override
    protected final boolean onHitBlock(@NonNull Block hitBlock) {
        return false;
    }

    @Override
    protected final boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
        currentTarget = target;
        onFindEntity(target);

        return false;
    }

    /**
     * 조건에 맞는 대상 엔티티를 찾았을 때 실행할 작업.
     *
     * @param target 대상 엔티티
     */
    protected abstract void onFindEntity(@NonNull Damageable target);

    @Override
    protected final void onDestroy() {
        if (alertOnFail && currentTarget == null)
            ((CombatUser) shooter).getUser().sendAlertActionBar("대상을 찾을 수 없습니다.");
    }
}
