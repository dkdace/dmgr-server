package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.DistantDamage;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class ChedUltInfo extends UltimateSkillInfo<ChedUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 10000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(1.5);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 피해량 */
    public static final int DAMAGE = 1500;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 거리별 피해량 */
    public static final DistantDamage DISTANT_DAMAGE = new DistantDamage(DAMAGE, SIZE / 2);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 200;
    /** 화염 지대 지속 시간 */
    public static final Timespan FIRE_FLOOR_DURATION = Timespan.ofSeconds(8);
    /** 화염 지대 범위 (단위: 블록) */
    public static final double FIRE_FLOOR_RADIUS = 7;
    /** 화염 지대 높이 (단위: 블록) */
    public static final double FIRE_FLOOR_HEIGHT = 1.5;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    /** 궁극기 처치 점수 제한시간 */
    public static final Timespan KILL_SCORE_TIME_LIMIT = Timespan.ofSeconds(2);

    @Getter
    private static final ChedUltInfo instance = new ChedUltInfo();

    private ChedUltInfo() {
        super(ChedUlt.class, "피닉스 스트라이크",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 관통하는 불사조를 날려보내 적과 부딪히면 크게 폭발하여 <:DAMAGE:광역 피해>를 입히고 <3::화염 지대>를 만듭니다. " +
                                "플레이어가 아닌 적은 통과합니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, DAMAGE, DAMAGE / 2)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build(),
                        new ActionInfoLore.NamedSection("화염 지대", ActionInfoLore.Section
                                .builder("지속적인 <:FIRE:화염 피해>를 입히는 지역입니다.")
                                .addValueInfo(TextIcon.DURATION, Format.TIME, FIRE_FLOOR_DURATION.toSeconds())
                                .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, FIRE_FLOOR_RADIUS)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.4).build(),
                SoundEffect.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR).volume(2).pitch(0.8).build(),
                SoundEffect.builder("new.entity.squid.squirt").volume(2).pitch(0.7).build());
        /** 사용 시 틱 효과 - 1 */
        public static final PlayableEffect.BiFunction<Long, Vector> USE_TICK_1 = (i, velocity) -> PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.DRIP_LAVA).build(),
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(0.02 + i * 0.0014)));
        /** 사용 시 틱 효과 - 2 */
        public static final PlayableEffect.Function<Vector> USE_TICK_2 = velocity -> PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.DRIP_LAVA).build(),
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(0.1)),
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(0.16)));
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder("new.entity.phantom.death").volume(3).pitch(0.7).build(),
                SoundEffect.builder("new.entity.phantom.death").volume(3).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_WITHER_SHOOT).volume(3).pitch(0.5).build(),
                SoundEffect.builder(Sound.ENTITY_VEX_CHARGE).volume(3).pitch(0.85).build());
        /** 총알 궤적 - 1 */
        public static final PlayableEffect BULLET_TRAIL_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1.5).pitch(1.2).build(),

                ParticleEffect.Normal.builder(Particle.REDSTONE).count(20).horizontalSpread(0.28).verticalSpread(0.28).build());
        /** 총알 궤적 - 2 */
        public static final PlayableEffect.BiFunction<Double, Double> BULLET_TRAIL_2 = (horizontalSpread, verticalSpread) ->
                ParticleEffect.Normal.builder(Particle.REDSTONE).count(8).horizontalSpread(horizontalSpread).verticalSpread(verticalSpread).build();
        /** 총알 궤적 - 3 */
        public static final ParticleEffect BULLET_TRAIL_3 =
                ParticleEffect.Normal.builder(Particle.LAVA).count(3).build();
        /** 총알 궤적 - 4 */
        public static final PlayableEffect.Function<Vector> BULLET_TRAIL_4 = velocity ->
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(-0.25));
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY =
                ParticleEffect.Normal.builder(Particle.FLAME).count(5).horizontalSpread(0.05).verticalSpread(0.05).build();
        /** 폭발 */
        public static final PlayableEffect EXPLODE = PlayableEffect.list(
                SoundEffect.builder(Sound.ITEM_TOTEM_USE).volume(5).pitch(1.3).build(),
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(5).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(5).pitch(0.6).build(),
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(5).pitch(0.8).build(),
                SoundEffect.builder("random.explosion_reverb").volume(7).pitch(0.6).build(),

                ParticleEffect.Normal.builder(Particle.EXPLOSION_HUGE).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_LARGE).count(400).horizontalSpread(0.5).verticalSpread(0.5).speed(0.2).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(600).horizontalSpread(0.4).verticalSpread(0.4).speed(0.4).build(),
                ParticleEffect.Normal.builder(Particle.LAVA).count(150).horizontalSpread(3).verticalSpread(3).build(),
                ParticleEffect.Normal.builder(Particle.FLAME).count(400).horizontalSpread(0.2).verticalSpread(0.2).speed(0.25).build());
        /** 화염 지대 틱 효과음 */
        public static final SoundEffect FIRE_FLOOR_TICK_SOUND =
                SoundEffect.builder(Sound.BLOCK_FIRE_AMBIENT).volume(2).pitch(0.75).pitchVariance(0.1).build();
        /** 화염 지대 틱 입자 효과 */
        public static final PlayableEffect FIRE_FLOOR_TICK_PARTICLE = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.FLAME).count(20).horizontalSpread(4).build(),
                ParticleEffect.Normal.builder(Particle.SMOKE_LARGE).count(6).horizontalSpread(4).build());
    }
}
