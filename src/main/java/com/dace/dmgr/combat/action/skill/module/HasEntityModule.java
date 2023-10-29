package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.combat.action.ActionModule;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.entity.SummonEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 엔티티를 소환할 수 있는 스킬의 모듈 클래스.
 *
 * <p>스킬이 {@link HasEntity}를 상속받는 클래스여야 한다.</p>
 *
 * @see HasEntity
 */
@RequiredArgsConstructor
public final class HasEntityModule<T extends SummonEntity<?>> implements ActionModule {
    /** 스킬 객체 */
    private final HasEntity<T> skill;
    /** 소환된 엔티티 */
    @Getter
    @Setter
    private T summonEntity;

    /**
     * 소환된 엔티티를 제거한다.
     */
    public void removeSummonEntity() {
        if (getSummonEntity() != null)
            getSummonEntity().remove();
        setSummonEntity(null);
    }

    @Override
    public void onReset() {
        removeSummonEntity();
    }

    @Override
    public void onRemove() {
        removeSummonEntity();
    }
}
