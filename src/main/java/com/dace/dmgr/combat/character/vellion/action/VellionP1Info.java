package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class VellionP1Info extends PassiveSkillInfo<VellionP1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20L;
    /** 이동속도 증가량 */
    public static final int SPEED = 20;
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.2;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 0.6;
    /** 지속시간 (tick) */
    public static final long DURATION = 10 * 20L;
    @Getter
    private static final VellionP1Info instance = new VellionP1Info();

    private VellionP1Info() {
        super(VellionP1.class, "비행",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("공중에서 날아다닐 수 있습니다. " +
                                "비행 도중 <:WALK_SPEED_INCREASE:이동 속도>가 증가합니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                        .addActionKeyInfo("사용", ActionKey.SPACE)
                        .build(),
                        new ActionInfoLore.NamedSection("지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("해제", ActionKey.SPACE)
                                .build()
                        )
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.entity.phantom.flap").volume(1).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder("new.entity.phantom.flap").volume(1).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(0.6).pitch(0.7).build()
        );
        /** 해제 */
        public static final SoundEffect DISABLE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.entity.phantom.flap").volume(1).pitch(1.5).build(),
                SoundEffect.SoundInfo.builder("new.entity.phantom.flap").volume(1).pitch(1.7).build()
        );
    }
}
