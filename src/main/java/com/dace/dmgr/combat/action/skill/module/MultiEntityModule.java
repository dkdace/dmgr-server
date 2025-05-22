package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.combat.action.skill.MultiSummonable;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 스킬의 다중 소환 엔티티 모듈 클래스.
 *
 * @param <T> {@link SummonEntity}를 상속받는 소환 가능한 엔티티
 */
public final class MultiEntityModule<T extends SummonEntity<?>> {
    /** 최대 엔티티 수 */
    private final int maxCount;
    /** 소환한 엔티티 목록 */
    private final ArrayList<T> summonEntities = new ArrayList<>();

    /**
     * 다중 소환 엔티티 모듈 인스턴스를 생성한다.
     *
     * @param skill    대상 스킬
     * @param maxCount 최대 엔티티 수. 1 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public MultiEntityModule(@NonNull MultiSummonable<T> skill, int maxCount) {
        Validate.isTrue(maxCount >= 1, "maxCount >= 1 (%d)", maxCount);

        this.maxCount = maxCount;
        skill.addOnReset(this::removeEntities);
    }

    /**
     * 소환한 엔티티 목록을 반환한다.
     *
     * @return 소환한 엔티티 목록
     */
    @NonNull
    @UnmodifiableView
    public List<@NonNull T> get() {
        return Collections.unmodifiableList(summonEntities);
    }

    /**
     * 소환한 엔티티를 추가한다.
     *
     * <p>소환한 엔티티 수가 {@link MultiEntityModule#maxCount} 이상이면 가장 오래된 엔티티를 제거한 뒤 추가한다.</p>
     *
     * <p>새 엔티티를 소환했을 때 호출되어야 한다.</p>
     *
     * @param summonEntity 소환한 엔티티
     */
    public void add(@NonNull T summonEntity) {
        if (summonEntities.size() >= maxCount)
            removeEldestEntity();

        summonEntities.add(summonEntity);
        summonEntity.addOnRemove(() -> summonEntities.remove(summonEntity));
    }

    /**
     * 소환한 모든 엔티티의 {@link SummonEntity#remove()}를 실행한다.
     */
    public void removeEntities() {
        new ArrayList<>(summonEntities).forEach(SummonEntity::remove);
    }

    /**
     * 가장 오래된 엔티티의 {@link SummonEntity#remove()}를 실행한다.
     */
    public void removeEldestEntity() {
        summonEntities.get(0).remove();
    }
}
