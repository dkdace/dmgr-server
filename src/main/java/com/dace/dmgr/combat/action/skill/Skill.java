package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.Action;

/**
 * 스킬의 상태를 관리하는 인터페이스.
 *
 * @see AbstractSkill
 */
public interface Skill extends Action {
    /**
     * 스킬의 기본 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    long getDefaultDuration();

    /**
     * 스킬의 남은 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    long getDuration();

    /**
     * 스킬의 지속시간을 설정한다.
     *
     * @param duration 지속시간 (tick). -1로 설정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    void setDuration(long duration);

    /**
     * 스킬의 지속시간을 기본 지속시간으로 설정한다.
     *
     * @see Skill#getDefaultDuration()
     */
    void setDuration();

    /**
     * 스킬의 지속시간을 증가시킨다.
     *
     * @param duration 추가할 지속시간 (tick). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    void addDuration(long duration);

    /**
     * 스킬의 지속시간이 끝났는 지 확인한다.
     *
     * @return 지속시간 종료 여부
     */
    boolean isDurationFinished();
}
