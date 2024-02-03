package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.combat.action.skill.HasEntities;
import com.dace.dmgr.combat.entity.SummonEntity;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 여러 엔티티를 소환할 수 있는 스킬의 모듈 클래스.
 *
 * <p>스킬이 {@link HasEntities}를 상속받는 클래스여야 한다.</p>
 *
 * @see HasEntities
 */
@RequiredArgsConstructor
public final class HasEntitiesModule<T extends SummonEntity<?>> implements Disposable {
    /** 스킬 객체 */
    @NonNull
    private final HasEntities<T> skill;
    /** 소환된 엔티티 목록 */
    @NonNull
    @Getter
    private List<T> summonEntities;
    /** 비활성화 여부 */
    @Getter
    private boolean isDisposed = false;

    /**
     * 소환된 엔티티를 추가한다.
     *
     * @param summonEntity 추가할 엔티티
     */
    public void addSummonEntity(@NonNull T summonEntity) {
        summonEntities.add(summonEntity);
    }

    /**
     * 소환된 엔티티를 제거한다.
     *
     * @param summonEntity 제거할 엔티티
     */
    public void removeSummonEntity(@NonNull T summonEntity) {
        summonEntity.dispose();
        summonEntities.remove(summonEntity);
    }

    /**
     * 소환된 엔티티를 모두 제거한다.
     */
    public void clearSummonEntities() {
        summonEntities.forEach(SummonEntity::dispose);
        summonEntities.clear();
    }

    @Override
    public void dispose() {
        checkAccess();
        clearSummonEntities();

        isDisposed = true;
    }
}
