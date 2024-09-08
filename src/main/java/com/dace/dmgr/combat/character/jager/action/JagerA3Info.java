package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class JagerA3Info extends ActiveSkillInfo<JagerA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 14 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 폭파 시간 (tick) */
    public static final long EXPLODE_DURATION = 5 * 20;
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 600;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 50;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 6;
    /** 빙결량 */
    public static final int FREEZE = 100;
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = (long) (1.2 * 20);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.6;

    /** 속박 점수 */
    public static final int SNARE_SCORE = 8;
    @Getter
    private static final JagerA3Info instance = new JagerA3Info();

    private JagerA3Info() {
        super(JagerA3.class, "빙결 수류탄",
                "",
                "§f▍ 빙결 수류탄의 핀을 뽑습니다. 수류탄은 일정",
                "§f▍ 시간 후 폭발하여 적에게 §c" + TextIcon.DAMAGE + " 광역 피해§f를 입히고",
                "§f▍ §5" + TextIcon.WALK_SPEED_DECREASE + " §d빙결§f시키며, 적이 최대치의 빙결을 입으면",
                "§f▍ §5" + TextIcon.SNARE + " 속박§f됩니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, EXPLODE_DURATION / 20.0),
                MessageFormat.format("§c{0}§f {1} ~ {2} (폭발)", TextIcon.DAMAGE, DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2),
                MessageFormat.format("§c{0}§f {1} (직격)", TextIcon.DAMAGE, DAMAGE_DIRECT),
                MessageFormat.format("§5{0}§f {1} ~ {2}", TextIcon.WALK_SPEED_DECREASE, FREEZE, FREEZE / 2),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.SNARE, SNARE_DURATION / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[3] §f사용",
                "",
                "§3[재사용 시]",
                "",
                "§f▍ 수류탄을 던집니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[3] [좌클릭] §f투척");
    }
}
