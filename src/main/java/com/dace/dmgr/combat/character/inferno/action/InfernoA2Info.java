package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class InfernoA2Info extends ActiveSkillInfo<InfernoA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20L;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 60;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;
    @Getter
    private static final InfernoA2Info instance = new InfernoA2Info();

    private InfernoA2Info() {
        super(InfernoA2.class, "불꽃 방출",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 주변에 불꽃을 방출하여 <:FIRE:화염 피해>를 입히고 <:GROUNDING:고정>시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
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
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_CONTRACT).volume(2).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_CONTRACT).volume(2).pitch(0.6).build()
        );
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(2).pitch(0.55).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_AMBIENT).volume(2).pitch(0.6).pitchVariance(0.1).build()
        );
    }
}
