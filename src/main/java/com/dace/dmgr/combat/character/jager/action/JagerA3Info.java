package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class JagerA3Info extends ActiveSkillInfo<JagerA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 14 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 폭파 시간 (tick) */
    public static final long EXPLODE_DURATION = 5 * 20L;
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
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("빙결 수류탄의 핀을 뽑습니다. " +
                                "수류탄은 일정 시간 후 폭발하여 적에게 <:DAMAGE:광역 피해>를 입히고 <5:WALK_SPEED_DECREASE:> <d::빙결>시키며, 적이 최대치의 빙결을 입으면 <:SNARE:속박>됩니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, EXPLODE_DURATION / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.VARIABLE, ChatColor.DARK_PURPLE, FREEZE, FREEZE / 2)
                        .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION / 20.0)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .build(),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("수류탄을 던집니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("투척", ActionKey.SLOT_3)
                                .build()
                        )
                )
        );
    }
}
