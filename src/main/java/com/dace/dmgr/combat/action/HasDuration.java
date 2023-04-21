package com.dace.dmgr.combat.action;

/**
 * 지속시간이 존재하는 스킬의 인터페이스.
 */
public interface HasDuration {
    /**
     * 스킬의 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    long getDuration();
}
