package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

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
        super(MagrittaA1.class, "태초의 불꽃",
                "",
                "§f▍ 일정 시간 후 폭발하는 폭탄을 던져 §c" + TextIcon.DAMAGE + " 광역 피해",
                "§f▍ 와 §c" + TextIcon.FIRE + " 화염 피해§f를 입히고, §d파쇄§f를 적용합니다.",
                "§f▍ 적에게 부착할 수 있습니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1} ~ {2} (폭발)", TextIcon.DAMAGE, DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2),
                MessageFormat.format("§c{0}§f {1} (직격)", TextIcon.DAMAGE, DAMAGE_DIRECT),
                MessageFormat.format("§c{0}§f {1}초 ~ {2}초 / {3}/초", TextIcon.FIRE, FIRE_DURATION / 20.0, FIRE_DURATION / 2 / 20.0, FIRE_DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[1] §f사용");
    }
}
