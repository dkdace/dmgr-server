package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class PalasA1Info extends ActiveSkillInfo<PalasA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (1.2 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 50;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = (long) (1.8 * 20);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final PalasA1Info instance = new PalasA1Info();

    private PalasA1Info() {
        super(PalasA1.class, "테이저건",
                "",
                "§f▍ 테이저건을 발사하여 §c" + TextIcon.DAMAGE + " 피해§f를 입히고",
                "§f▍ §5" + TextIcon.STUN + " 기절§f시킵니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.STUN, STUN_DURATION / 20.0),
                "",
                "§7§l[1] §f사용");
    }
}