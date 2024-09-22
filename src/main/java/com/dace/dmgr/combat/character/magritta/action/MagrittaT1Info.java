package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

import java.text.MessageFormat;

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
        super("파쇄",
                "",
                "§f▍ 수치에 비례하여 마그리타로부터 §c" + TextIcon.DAMAGE_INCREASE + " 받는 피해§f가",
                "§f▍ 증가하는 상태이상입니다.",
                "§f▍ 최대치에 도달하면 불이 붙어 §c" + TextIcon.FIRE + " 화염 피해§f를",
                "§f▍ 받습니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§5{0}§f 최대 {1}", TextIcon.DAMAGE_INCREASE, MAX),
                MessageFormat.format("§c{0}§f (파쇄)×{1}%", TextIcon.DAMAGE_INCREASE, DAMAGE_INCREMENT),
                MessageFormat.format("§c{0}§f {1}/초", TextIcon.FIRE, FIRE_DAMAGE_PER_SECOND));
    }
}
