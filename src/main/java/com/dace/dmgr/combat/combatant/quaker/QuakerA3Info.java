package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class QuakerA3Info extends ActiveSkillInfo<QuakerA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(8);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.8);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 150;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 1.2;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2;
    /** 속박 시간 */
    public static final Timespan SNARE_DURATION = Timespan.ofSeconds(0.5);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 2;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 4;

    @Getter
    private static final QuakerA3Info instance = new QuakerA3Info();

    private QuakerA3Info() {
        super(QuakerA3.class, "돌풍 강타",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("검기를 날려 처음 맞은 적을 크게 <:KNOCKBACK:밀쳐내고> <:DAMAGE:피해>와 <:SNARE:속박>을 입힙니다. " +
                                "적이 벽에 충돌하면 같은 효과를 다시 입히며, 날아가며 부딪힌 적에게도 같은 효과를 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION.toSeconds())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(1).pitch(0.8).build(),
                SoundEffect.builder("random.gun2.shovel_leftclick").volume(1).pitch(0.5).build(),
                SoundEffect.builder("random.gun2.shovel_leftclick").volume(1).pitch(0.8).build());
        /** 검기 효과 - 1 */
        public static final ParticleEffect TRAIL_1 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, QuakerWeaponInfo.Effects.COLOR).count(2)
                        .horizontalSpread(0.12).verticalSpread(0.12).build();
        /** 검기 효과 - 2 */
        public static final ParticleEffect TRAIL_2 =
                ParticleEffect.Normal.builder(Particle.CRIT).count(3).horizontalSpread(0.07).verticalSpread(0.07).build();
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(2).pitch(0.5).build(),
                SoundEffect.builder("new.item.trident.throw").volume(2).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_SWEEP).volume(2).pitch(0.7).build());
        /** 총알 궤적 - 1 */
        public static final SoundEffect BULLET_TRAIL_1 =
                SoundEffect.builder(Sound.ENTITY_GHAST_SHOOT).volume(0.6).pitch(0.5).build();
        /** 총알 궤적 - 2 */
        public static final PlayableEffect.Function<Vector> BULLET_TRAIL_2 = velocity ->
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL,
                        VectorUtil.getSpreadedVector(velocity.clone().normalize(), 30).multiply(1.4));
        /** 타격 */
        public static final ParticleEffect HIT =
                ParticleEffect.Normal.builder(Particle.EXPLOSION_NORMAL).count(50).horizontalSpread(0.2).verticalSpread(0.2).speed(0.4).build();
        /** 블록 타격 */
        public static final PlayableEffect.Function<Block> HIT_BLOCK = block -> PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.6).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_CRIT).volume(2).pitch(0.7).build(),

                CombatEffectUtil.HIT_BLOCK_PARTICLE.apply(block, 5.0));
        /** 엔티티 타격 */
        public static final PlayableEffect HIT_ENTITY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.6).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_CRIT).volume(2).pitch(0.7).build(),

                ParticleEffect.Normal.builder(Particle.CRIT).count(50).speed(0.4).build());
        /** 엔티티 넉백 */
        public static final PlayableEffect.Function<Vector> HIT_ENTITY_KNOCKBACK = velocity ->
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL,
                        VectorUtil.getSpreadedVector(velocity.clone().normalize(), 20).multiply(0.6));
        /** 엔티티 벽 충돌 */
        public static final PlayableEffect.Function<Block> HIT_ENTITY_WALL = block ->
                CombatEffectUtil.HIT_BLOCK_PARTICLE.apply(block, 7.0);

        private static void playTrail(@NonNull Location location, @NonNull Vector velocity) {
            for (int i = 0; i < 3; i++) {
                Location loc1 = LocationUtil.getLocationFromOffset(location, velocity, -0.25 + i * 0.25, 0, 0);
                TRAIL_1.play(loc1);

                Vector vec = velocity.clone().normalize().multiply(0.25);
                Location loc2 = loc1.clone().add(vec);

                TRAIL_2.play(loc2);
            }
        }

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playUseTick(long i, @NonNull Location location) {
            location = LocationUtil.getLocationFromOffset(location, 0, 0, 1);

            Vector vector = VectorUtil.getYawAxis(location).multiply(-1);
            Vector axis = VectorUtil.getPitchAxis(location);

            for (int j = 0; j < i; j++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (j - 2.5));
                Location loc = location.clone().add(vec);

                playTrail(loc, vec);
            }
        }

        /**
         * 총알 궤적을 재생한다.
         *
         * @param location 위치
         * @param velocity 총알 속력
         */
        public static void playBulletTrail(@NonNull Location location, @NonNull Vector velocity) {
            BULLET_TRAIL_1.play(location);

            Vector vector = VectorUtil.getYawAxis(location).multiply(-1);
            Vector axis = VectorUtil.getPitchAxis(location);

            for (int i = 0; i < 8; i++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (i - 3.5));
                Location loc = location.clone().add(vec);

                playTrail(loc, vec);
                BULLET_TRAIL_2.apply(velocity).play(location);
            }
        }

        /**
         * 엔티티 넉백 효과를 재생한다.
         *
         * @param location 위치
         * @param velocity 총알 속력
         */
        public static void playHitEntityKnockback(@NonNull Location location, @NonNull Vector velocity) {
            for (int i = 0; i < 5; i++)
                HIT_ENTITY_KNOCKBACK.apply(velocity).play(location);
        }
    }
}
