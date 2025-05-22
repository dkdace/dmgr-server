package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 스킬의 소환 엔티티 모듈 클래스.
 *
 * @param <T> {@link SummonEntity}를 상속받는 소환 가능한 엔티티
 */
public final class EntityModule<T extends SummonEntity<?>> {
    /** 소환한 엔티티 */
    @Nullable
    private T summonEntity = null;

    /**
     * 소환 엔티티 모듈 인스턴스를 생성한다.
     *
     * @param skill 대상 스킬
     */
    public EntityModule(@NonNull Summonable<T> skill) {
        skill.addOnReset(this::removeEntity);
    }

    /**
     * 소환한 엔티티를 반환한다.
     *
     * @return 소환한 엔티티
     */
    @Nullable
    public T get() {
        return summonEntity;
    }

    /**
     * 소환한 엔티티를 지정한다.
     *
     * <p>새 엔티티를 소환했을 때 호출되어야 한다.</p>
     *
     * @param summonEntity 소환한 엔티티
     */
    public void set(@NonNull T summonEntity) {
        this.summonEntity = summonEntity;

        summonEntity.addOnRemove(() -> {
            if (this.summonEntity != null)
                this.summonEntity = null;
        });
    }

    /**
     * 소환한 엔티티가 지정되어 있으면 {@link SummonEntity#remove()}를 실행한다.
     */
    public void removeEntity() {
        if (summonEntity != null)
            summonEntity.remove();
    }
}
