package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class JagerA2Info extends ActiveSkillInfo<JagerA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 15;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = (long) (1.5 * 20);
    /** 체력 */
    public static final int HEALTH = 400;
    /** 피해량 */
    public static final int DAMAGE = 300;
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = 3 * 20L;

    /** 속박 점수 */
    public static final int SNARE_SCORE = 20;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 10;
    @Getter
    private static final JagerA2Info instance = new JagerA2Info();

    private JagerA2Info() {
        super(JagerA2.class, "곰덫",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("눈에 잘 띄지 않는 <3::곰덫>을 던져 설치합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("곰덫", ActionInfoLore.Section
                                .builder("밟은 적은 <:DAMAGE:피해>를 입고 <:SNARE:속박>됩니다.")
                                .addValueInfo(TextIcon.HEAL, HEALTH)
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION / 20.0)
                                .build()
                        )
                )
        );
    }
}
