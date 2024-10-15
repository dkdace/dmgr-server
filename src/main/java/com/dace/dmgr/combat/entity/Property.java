package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.character.magritta.action.MagrittaT1Info;
import com.dace.dmgr.combat.character.neace.action.NeaceA1Info;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전투 시스템의 엔티티가 사용하는 속성의 종류.
 */
@AllArgsConstructor
@Getter
public enum Property {
    /** 치유 표식 */
    HEALING_MARK(NeaceA1Info.MAX_HEAL),
    /** 빙결 */
    FREEZE(JagerT1Info.MAX),
    /** 파쇄 */
    SHREDDING(MagrittaT1Info.MAX);

    /** 최댓값 */
    private final int max;
}
