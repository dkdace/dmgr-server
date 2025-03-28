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

public final class MetarA1Info extends ActiveSkillInfo<MetarA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(9);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 30;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 적 감지 범위 (단위: 블록) */
    public static final double ENEMY_DETECT_RADIUS = 7;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.35;

    /** 직격 점수 */
    public static final int DIRECT_HIT_SCORE = 1;

    @Getter
    private static final MetarA1Info instance = new MetarA1Info();

    private MetarA1Info() {
        super(MetarA1.class, "열추적 기압 미사일",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("적을 추적하는 기압 미사일을 연속으로 발사하여 <:DAMAGE:광역 피해>를 입히고 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.LEFT_CLICK)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_EXTEND).volume(1.5).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_SHULKER_BOX_OPEN).volume(1.5).pitch(0.8).build());
        /** 사격 */
        public static final SoundEffect SHOOT = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(3).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_SHULKER_SHOOT).volume(3).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_LAUNCH).volume(3).pitch(0.8).build());
        /** 폭발 */
        public static final SoundEffect EXPLODE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_LARGE_BLAST).volume(4).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_BLAST).volume(4).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb2").volume(6).pitch(1.2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(40, 160, 200);

        /** 사용 시 틱 입자 효과 (중심) */
        public static final ParticleEffect USE_TICK_CORE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(90, 94, 121)).count(3).horizontalSpread(0.2).verticalSpread(0.1).build());
        /** 사용 시 틱 입자 효과 (모양) */
        public static final ParticleEffect USE_TICK_SHAPE = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(200, 10, 10)).horizontalSpread(0).verticalSpread(0).build());
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_NORMAL)
                        .speedMultiplier(-0.2).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, COLOR).build());
        /** 폭발 */
        public static final ParticleEffect EXPLODE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_NORMAL).count(25).speed(0.25).build());
    }
}
