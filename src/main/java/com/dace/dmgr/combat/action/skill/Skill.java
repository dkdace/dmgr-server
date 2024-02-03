package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.Action;

/**
 * 스킬의 상태를 관리하는 인터페이스.
 *
 * @see AbstractSkill
 */
public interface Skill extends Action {
    /**
     * @return 스킬 번호
     */
    int getNumber();

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
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
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
     * @param duration 추가할 지속시간 (tick)
     */
    void addDuration(long duration);

    /**
     * 스킬의 지속시간이 끝났는 지 확인한다.
     *
     * @return 지속시간 종료 여부
     */
    boolean isDurationFinished();
}
