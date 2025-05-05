package com.dace.dmgr.combat.combatant.metar;

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
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MetarA3Info extends ActiveSkillInfo<MetarA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(11);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 폭파 시간 */
    public static final Timespan EXPLODE_DURATION = Timespan.ofSeconds(6);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 15;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5.5;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(0.7);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.4;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 8;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    /** 처치 지원 점수 제한시간 */
    public static final Timespan ASSIST_SCORE_TIME_LIMIT = Timespan.ofSeconds(1.4);

    @Getter
    private static final MetarA3Info instance = new MetarA3Info();

    private MetarA3Info() {
        super(MetarA3.class, "중력장 발생기",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("중력장 발생기를 발사합니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, EXPLODE_DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build(),
                        new ActionInfoLore.NamedSection("지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("발생기에서 중력장을 방출하여 주변의 적을 <:KNOCKBACK:끌어당깁니다>.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                                .addActionKeyInfo("격발", ActionKey.SLOT_3)
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_CAT_PURREOW).volume(0.5).pitch(1.6).build());
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_SHULKER_BULLET_HIT).volume(3).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GHAST_SHOOT).volume(3).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder("random.energy").volume(3).pitch(1.6).build());
        /** 격발 */
        public static final SoundEffect DETONATE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_BUTTON_CLICK_ON).volume(0.5).pitch(0.8).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.block.beacon.ambient").volume(0.6).pitch(1.8).build());
        /** 격발 시 틱 효과음 */
        public static final SoundEffect DETONATE_TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERMEN_TELEPORT).volume(2).pitch(1.2, 1.7).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(0, 0, 0);

        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.PORTAL).count(15).horizontalSpread(0.1).verticalSpread(0.1).speed(0.3).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).horizontalSpread(0.3)
                        .verticalSpread(0.3).count(12).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.DRAGON_BREATH).count(5).horizontalSpread(0.05).verticalSpread(0.05).build());
        /** 격발 시 틱 입자 효과 (중심) */
        public static final ParticleEffect DETONATE_TICK_CORE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(30).horizontalSpread(0.25).verticalSpread(0.25).build());
        /** 격발 시 틱 입자 효과 (장식) */
        public static final ParticleEffect DETONATE_TICK_DECO = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(0, ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                                Color.fromRGB(228, 55, 205), COLOR)
                        .count(2).horizontalSpread(0.08).verticalSpread(0.08).build(),
                ParticleEffect.DirectionalParticleInfo.builder(1, Particle.SMOKE_NORMAL)
                        .speedMultiplier(0.4).build());
    }
}
