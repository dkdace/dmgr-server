package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import lombok.NonNull;

/**
 * 소환 가능한 엔티티({@link SummonEntity})를 소환할 수 있는 스킬의 인터페이스.
 *
 * @param <T> {@link SummonEntity}를 상속받는 소환 가능한 엔티티
 */
public interface Summonable<T extends SummonEntity<?>> extends Skill {
    /**
     * @return 소환 엔티티 모듈
     */
    @NonNull
    EntityModule<T> getEntityModule();
}
