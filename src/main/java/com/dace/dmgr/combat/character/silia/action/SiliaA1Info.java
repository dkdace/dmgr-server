package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;

public final class SiliaA1Info extends ActiveSkillInfo<SiliaA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 8 * 20L;
    /** 이동 거리 (단위: 블록) */
    public static final int MOVE_DISTANCE = 15;
    /** 이동 강도 */
    public static final double PUSH = 2.5;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;
    @Getter
    private static final SiliaA1Info instance = new SiliaA1Info();

    private SiliaA1Info() {
        super(SiliaA1.class, "연풍 가르기",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("앞으로 빠르게 이동하며 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "적을 처치하면 <7:COOLDOWN:쿨타임>이 초기화됩니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MOVE_DISTANCE)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final DefinedSound USE = new DefinedSound(
                new DefinedSound.SoundEffect("new.item.trident.throw", 1.5, 0.8),
                new DefinedSound.SoundEffect("random.swordhit", 1.5, 0.8),
                new DefinedSound.SoundEffect("random.swordhit", 1.5, 0.8)
        );
    }
}
