package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.SummonEntity;

/**
 * 전투 시스템에서 플레이어가 입힐 수 있는 피해 타입의 종류.
 */
public enum DamageType {
    /** 기본 */
    NORMAL,
    /** {@link SummonEntity}를 이용한 공격 */
    ENTITY,
    /** 시스템 (고정 피해) */
    SYSTEM
}
