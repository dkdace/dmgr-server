package com.dace.dmgr.combat.combatant.neace.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class NeaceA2Info extends ActiveSkillInfo<NeaceA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(7);
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 15;
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 15;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(8);

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final NeaceA2Info instance = new NeaceA2Info();

    private NeaceA2Info() {
        super(NeaceA2.class, "축복",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 기본 무기의 치유 대상을 <3::축복>할 수 있습니다. " +
                                "사용 중에는 기본 무기로 치유할 수 없습니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("축복", ActionInfoLore.Section
                                .builder("<:DAMAGE_INCREASE:공격력>과 <:DEFENSE_INCREASE:방어력>이 증가합니다.")
                                .addValueInfo(TextIcon.DAMAGE_INCREASE, Format.PERCENT, DAMAGE_INCREMENT)
                                .addValueInfo(TextIcon.DEFENSE_INCREASE, Format.PERCENT, DEFENSE_INCREMENT)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_2)
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
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.5).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.5).pitch(1.4).build()
        );
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 사용 시 틱 입자 효과 */
        public static final ParticleEffect USE_TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(0, ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                                200, 140, 255, 255, 160, 245)
                        .count(6).horizontalSpread(0.2).verticalSpread(0.2).build());
        /** 틱 입자 효과 */
        public static final ParticleEffect TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 140, 255, 245)
                        .count(3).horizontalSpread(1).verticalSpread(1.5).build());
    }
}
