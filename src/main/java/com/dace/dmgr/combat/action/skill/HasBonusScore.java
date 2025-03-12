package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import lombok.NonNull;

/**
 * 적 처치 시 추가 보너스 점수를 지급하는 스킬의 인터페이스.
 */
public interface HasBonusScore extends Skill {
    /**
     * @return 보너스 점수 모듈
     */
    @NonNull
    BonusScoreModule getBonusScoreModule();

    /**
     * 어시스트 모드 여부를 반환한다.
     *
     * <p>{@code true}이면 처치 기여자가 2명 이상일 때만, 보너스 점수를 처치 기여 비례가 아닌 고정치로 지급한다.</p>
     *
     * @return 어시스트 모드 여부
     * @implSpec {@code false}
     */
    default boolean isAssistMode() {
        return false;
    }
}
