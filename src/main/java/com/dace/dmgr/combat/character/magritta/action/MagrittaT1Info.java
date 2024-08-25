package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class MagrittaT1Info extends TraitInfo {
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.2 * 20);
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 8;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 70;
    /** 최대치 */
    public static final int MAX = 4;

    /** 최대치 피해 점수 */
    public static final double MAX_DAMAGE_SCORE = 1;
    @Getter
    private static final MagrittaT1Info instance = new MagrittaT1Info();

    private MagrittaT1Info() {
        super("파쇄");
    }
}
