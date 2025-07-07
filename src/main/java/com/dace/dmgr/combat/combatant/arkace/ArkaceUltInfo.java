package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
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
    public static final CombatScore KILL_SCORE = new CombatScore("궁극기 보너스", 40);
    /** 궁극기 처치 점수 제한시간 */
    public static final Timespan KILL_SCORE_TIME_LIMIT = Timespan.ofSeconds(2);

    @Getter
    private static final ArkaceUltInfo instance = new ArkaceUltInfo();

    private ArkaceUltInfo() {
        super(ArkaceUlt.class, "오버클럭",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 기본 무기의 반동과 탄퍼짐 및 장거리 피해량 감소가 없어지고 재장전 없이 사격할 수 있게 됩니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 발사 */
        public static final PlayableEffect SHOOT = PlayableEffect.list(
                SoundEffect.builder("new.block.beacon.deactivate").volume(4).pitch(2).build(),
                SoundEffect.builder("random.energy").volume(4).pitch(1.6).build(),
                SoundEffect.builder("random.gun_reverb").volume(5).pitch(1.2).build());
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(0, 230, 255)).build();
    }
}
