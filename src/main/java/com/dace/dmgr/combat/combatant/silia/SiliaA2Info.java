package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class SiliaA2Info extends ActiveSkillInfo<SiliaA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(11);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(1);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 이동 강도 */
    public static final double PUSH = 0.8;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 15;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 25;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.8;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;

    @Getter
    private static final SiliaA2Info instance = new SiliaA2Info();

    private SiliaA2Info() {
        super(SiliaA2.class, "진권풍",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("회오리바람을 날려 적에게 <:DAMAGE:피해>를 입히고 <:KNOCKBACK:공중에 띄웁니다>. " +
                                "적중 시 맞은 적의 뒤로 순간이동합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.RIGHT_CLICK)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final SoundEffect USE =
                SoundEffect.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(1).pitch(1).build();
        /** 사용 시 틱 효과 */
        public static final PlayableEffect.Function<Vector> USE_TICK = velocity ->
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL, velocity.clone().multiply(0.2));
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder("random.swing").volume(1.5).pitch(0.6).build(),
                SoundEffect.builder("new.item.trident.riptide_3").volume(1.5).pitch(0.8).build());
        /** 총알 궤적 */
        public static final PlayableEffect.Function<Vector> BULLET_TRAIL = velocity -> PlayableEffect.list(
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL, velocity.clone().multiply(0.25)),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, SiliaWeaponInfo.Effects.COLOR).count(3)
                        .horizontalSpread(0.3).verticalSpread(0.3).build());
        /** 타격 */
        public static final PlayableEffect.Function<Vector> HIT = velocity ->
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL,
                        VectorUtil.getSpreadedVector(velocity, 60).multiply(RandomUtils.nextDouble(0.3, 0.4)));
        /** 엔티티 타격 - 1 */
        public static final PlayableEffect HIT_ENTITY_1 = PlayableEffect.list(
                SoundEffect.builder("random.swing").volume(1).pitch(0.7).build(),
                SoundEffect.builder("new.item.trident.riptide_2").volume(1).pitch(0.9).build());
        /** 엔티티 타격 - 2 */
        public static final ParticleEffect HIT_ENTITY_2 =
                ParticleEffect.Normal.builder(Particle.END_ROD).count(3).speed(0.05).build();
        /** 블록 타격 */
        public static final PlayableEffect.Function<Block> HIT_BLOCK = block ->
                CombatEffectUtil.HIT_BLOCK_PARTICLE.apply(block, 3.0);

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playUseTick(long i, @NonNull Location location) {
            location = LocationUtil.getLocationFromOffset(location, 0, 0, 1);

            Vector vector = VectorUtil.getYawAxis(location).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(location);

            double angle = i * 23.0;
            for (int j = 0; j < 6; j++) {
                angle += 360 / 6.0;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle).multiply(1.6 - i * 0.2);
                Location loc = location.clone().add(vec);

                USE_TICK.apply(vec).play(loc);
            }
        }

        /**
         * 총알 궤적을 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playBulletTrail(long i, @NonNull Location location) {
            Vector vector = VectorUtil.getYawAxis(location).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(location);

            double angle = i * 12.0;
            for (int j = 0; j < 2; j++) {
                angle += 360 / 2.0;
                Vector vec = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, angle), 8);
                Location loc = location.clone().add(vec);

                BULLET_TRAIL.apply(vec).play(loc);
            }
        }

        /**
         * 타격 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playHit(@NonNull Location location) {
            for (int i = 0; i < 40; i++)
                HIT.apply(new Vector(0, 1, 0)).play(location);
        }

        /**
         * 엔티티 타격 효과를 재생한다.
         *
         * @param hit   피격 위치
         * @param start 시작 위치
         * @param end   끝 위치
         */
        public static void playHitEntity(@NonNull Location hit, @NonNull Location start, @NonNull Location end) {
            HIT_ENTITY_1.play(hit);
            for (Location loc : LocationUtil.getLine(start, end, 0.5))
                HIT_ENTITY_2.play(loc.add(0, 1, 0));
        }
    }
}
