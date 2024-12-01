package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;

public final class SiliaUltInfo extends UltimateSkillInfo<SiliaUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = 20;
    /** 지속시간 (tick) */
    public static final long DURATION = 4 * 20L;
    /** 처치 시 지속시간 증가 (tick) */
    public static final long DURATION_ADD_ON_KILL = 2 * 20L;
    /** 이동속도 증가량 */
    public static final int SPEED = 30;
    /** 일격 쿨타임 (tick) */
    public static final long STRIKE_COOLDOWN = (long) (0.55 * 20);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 25;
    @Getter
    private static final SiliaUltInfo instance = new SiliaUltInfo();

    private SiliaUltInfo() {
        super(SiliaUlt.class, "폭풍의 부름",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 <:WALK_SPEED_INCREASE:이동 속도>가 빨라지고 기본 공격 시 <d::일격>을 날립니다. " +
                                "적 처치 시 <7:DURATION:지속 시간>이 늘어나며, 사용 중에는 <d::진권풍>, <d::폭풍전야>를 사용할 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME + " (+{1}초)", DURATION / 20.0, DURATION_ADD_ON_KILL / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 준비 */
        public static final DefinedSound USE_READY = new DefinedSound(
                new DefinedSound.SoundEffect("random.swordhit", 2, 1),
                new DefinedSound.SoundEffect("random.swordhit", 2, 0.7),
                new DefinedSound.SoundEffect("new.item.trident.return", 2.5, 1.4),
                new DefinedSound.SoundEffect("new.item.trident.return", 2.5, 1.2)
        );
    }
}
