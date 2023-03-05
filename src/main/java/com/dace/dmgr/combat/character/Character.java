package com.dace.dmgr.combat.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전투원 정보를 관리하는 클래스.
 */
@AllArgsConstructor
@Getter
public abstract class Character implements ICharacter {
    /** 이름 */
    private final String name;
    /** 스킨 이름 */
    private final String skinName;
    /** 체력 */
    private final int health;
    /** 이동속도 */
    private final float speed;
    /** 히트박스 크기 계수 */
    private final float hitbox;
}
