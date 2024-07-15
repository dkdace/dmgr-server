package com.dace.dmgr.combat.interaction;

/**
 * 전투 시스템에서 플레이어가 입힐 수 있는 피해 타입의 종류.
 */
public enum DamageType {
    /** 기본 */
    NORMAL,
    /** 방어력 무시 */
    IGNORE_DEFENSE,
    /** 고정 피해 */
    FIXED
}
