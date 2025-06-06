package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.DistantDamage;
import com.dace.dmgr.combat.entity.DistantTimespan;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MagrittaA1Info extends ActiveSkillInfo<MagrittaA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(12);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 폭파 시간 */
    public static final Timespan EXPLODE_DURATION = Timespan.ofSeconds(1);
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 250;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 50;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 80;
    /** 화염 지속 시간 */
    public static final Timespan FIRE_DURATION = Timespan.ofSeconds(5);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3.2;
    /** 거리별 피해량 (폭발) */
    public static final DistantDamage DISTANT_DAMAGE_EXPLODE = new DistantDamage(DAMAGE_EXPLODE, RADIUS / 2);
    /** 거리별 화염 지속 시간 */
    public static final DistantTimespan DISTANT_FIRE_DURATION = new DistantTimespan(FIRE_DURATION, RADIUS / 2);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.5;

    /** 부착 점수 */
    public static final int STUCK_SCORE = 8;

    @Getter
    private static final MagrittaA1Info instance = new MagrittaA1Info();

    private MagrittaA1Info() {
        super(MagrittaA1.class, "태초의 불꽃",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간 후 폭발하는 폭탄을 던져 <:DAMAGE:광역 피해>와 <:FIRE:화염 피해>를 입히고, <d::파쇄>를 적용합니다. " +
                                "적에게 부착할 수 있습니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.FIRE, Format.VARIABLE_TIME_WITH_PER_SECOND,
                                FIRE_DURATION.toSeconds(), FIRE_DURATION.toSeconds() / 2, FIRE_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final SoundEffect USE =
                SoundEffect.builder(Sound.ENTITY_CAT_PURREOW).volume(0.5).pitch(1.6).build();
        /** 사용 준비 */
        public static final PlayableEffect USE_READY =
                CombatEffectUtil.THROW_SOUND.apply(0.7);
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(200, 95, 35)).count(3)
                        .horizontalSpread(0.1).verticalSpread(0.1).build();
        /** 부착 */
        public static final PlayableEffect STUCK = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_PLAYER_HURT).volume(0.8).pitch(0.5).build(),
                SoundEffect.builder(Sound.ITEM_FLINTANDSTEEL_USE).volume(0.8).pitch(1.5).build());
        /** 부착 틱 효과음 */
        public static final SoundEffect STUCK_TICK_SOUND =
                SoundEffect.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1.5).pitch(1.8).build();
        /** 부착 틱 입자 효과 */
        public static final PlayableEffect STUCK_TICK_PARTICLE = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(200, 30, 15)).count(6)
                        .horizontalSpread(0.15).verticalSpread(0.15).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(4).horizontalSpread(0.12).verticalSpread(0.12).build());
        /** 폭발 */
        public static final PlayableEffect EXPLODE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(4).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_FIREWORK_LARGE_BLAST).volume(4).pitch(0.6).build(),
                SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(4).pitch(0.8).build(),
                SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(4).pitch(0.5).build(),
                SoundEffect.builder("random.explosion_reverb").volume(6).pitch(1.2).build(),

                ParticleEffect.Normal.builder(Particle.EXPLOSION_LARGE).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_LARGE).count(80).horizontalSpread(0.3).verticalSpread(0.3).speed(0.1).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(150).horizontalSpread(0.2).verticalSpread(0.2).speed(0.3).build(),
                ParticleEffect.Normal.builder(Particle.LAVA).count(100).horizontalSpread(0.8).verticalSpread(0.8).build(),
                ParticleEffect.Normal.builder(Particle.FLAME).count(250).horizontalSpread(0.2).verticalSpread(0.2).speed(0.15).build());
    }
}
