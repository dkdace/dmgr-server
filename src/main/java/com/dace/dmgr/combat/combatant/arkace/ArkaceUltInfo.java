package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;

public final class ArkaceUltInfo extends UltimateSkillInfo<ArkaceUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7500;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(12);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 40;

    @Getter
    private static final ArkaceUltInfo instance = new ArkaceUltInfo();

    private ArkaceUltInfo() {
        super(ArkaceUlt.class, "오버클럭",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 기본 무기의 반동과 탄퍼짐 및 장거리 피해량 감소가 없어지고 재장전 없이 사격할 수 있게 됩니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사격 */
        public static final SoundEffect SHOOT = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.block.beacon.deactivate").volume(4).pitch(2).build(),
                SoundEffect.SoundInfo.builder("random.energy").volume(4).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb").volume(5).pitch(1.2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(0, 230, 255)).build());
    }
}
