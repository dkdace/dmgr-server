package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.combat.action.skill.Targeted;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.interaction.Target;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 스킬의 대상 지정(타겟팅) 모듈 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 대상으로 지정 가능한 엔티티
 * @see Targeted
 */
@RequiredArgsConstructor
public final class TargetModule<T extends CombatEntity> {
    /** 스킬 인스턴스 */
    @NonNull
    private final Targeted<T> skill;
    /** 최대 거리 (단위: 블록) */
    private final double maxDistance;

    /** 찾은 엔티티 */
    @Nullable
    private T currentTarget;

    /**
     * 현재 탐색 조건과 일치하는 대상을 찾는다.
     *
     * <p>찾은 엔티티는 {@link TargetModule#getCurrentTarget()}를 이용하여 가져올 수 있다.</p>
     *
     * @return 대상을 찾았으면 {@code true} 반환
     */
    public boolean findTarget() {
        currentTarget = null;

        new Target<T>(skill.getCombatUser(), maxDistance, skill.getEntityCondition()) {
            @Override
            protected void onFindEntity(@NonNull T target) {
                currentTarget = target;
            }

            @Override
            protected void onDestroy(@NonNull Location location) {
                if (currentTarget == null)
                    skill.getCombatUser().getUser().sendAlertActionBar("대상을 찾을 수 없습니다.");
            }
        }.shot();

        return currentTarget != null;
    }

    /**
     * {@link TargetModule#findTarget()}에서 찾은 엔티티를 반환한다.
     *
     * @return 찾은 엔티티
     * @throws NullPointerException 현재 대상이 지정되지 않았으면 발생
     */
    @NonNull
    public T getCurrentTarget() {
        return Validate.notNull(currentTarget, "현재 대상이 지정되지 않음");
    }
}
