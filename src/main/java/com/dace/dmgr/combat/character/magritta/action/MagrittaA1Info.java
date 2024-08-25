package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class MagrittaA1Info extends ActiveSkillInfo<MagrittaA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 폭파 시간 (tick) */
    public static final long EXPLODE_DURATION = 1 * 20;
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 250;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 50;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 80;
    /** 화염 지속 시간 (tick) */
    public static final long FIRE_DURATION = 5 * 20;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3.2;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.5;

    /** 부착 점수 */
    public static final int STUCK_SCORE = 8;
    @Getter
    private static final MagrittaA1Info instance = new MagrittaA1Info();

    private MagrittaA1Info() {
        super(MagrittaA1.class, "태초의 불꽃");
    }
}
