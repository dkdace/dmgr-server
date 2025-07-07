package com.dace.dmgr.combat.combatant.vellion;

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
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.util.Vector;

public final class VellionUltInfo extends UltimateSkillInfo<VellionUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(1);
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 이동 속도 감소량 */
    public static final int SLOW = 50;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);
    /** 피해량 비율 */
    public static final double DAMAGE_RATIO = 0.5;
    /** 기절 시간 */
    public static final Timespan STUN_DURATION = Timespan.ofSeconds(1);

    /** 피해 점수 */
    public static final CombatScore DAMAGE_SCORE = new CombatScore("결계 발동", 15);
    /** 처치 지원 점수 */
    public static final CombatScore ASSIST_SCORE = new CombatScore("처치 지원", 25);

    @Getter
    private static final VellionUltInfo instance = new VellionUltInfo();

    private VellionUltInfo() {
        super(VellionUlt.class, "나락의 결계",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 무적이 되어 주변 적의 <:WALK_SPEED_DECREASE:이동 속도>를 느리게 하고 <:GROUNDING:고정>시킵니다. " +
                                "일정 시간 후 결계가 폭발하여 탈출하지 못한 적은 <:DAMAGE:광역 피해>를 입고 <:STUN:기절>합니다. " +
                                "사용 중에는 움직일 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.PERCENT, SLOW)
                        .addValueInfo(TextIcon.DAMAGE, "적 최대 체력의 {0}%", (int) (100 * DAMAGE_RATIO))
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION.toSeconds())
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(90, 0, 55);

        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_GUARDIAN_HURT).volume(2).pitch(1.8).build());
        /** 사용 시 틱 효과 */
        public static final PlayableEffect USE_TICK = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.SPELL_WITCH).count(3).horizontalSpread(0.05).verticalSpread(0.05).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, Color.fromRGB(70, 0, 45)).build());
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(3).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS).volume(3).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK).volume(3).pitch(0.85).build(),
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON).volume(3).pitch(0.7).build());
        /** 틱 효과 - 1 */
        public static final ParticleEffect TICK_1 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).count(4).horizontalSpread(0.3).build();
        /** 틱 효과 - 2 */
        public static final ParticleEffect TICK_2 =
                ParticleEffect.Normal.builder(Particle.PORTAL).count(40).speed(1.5).build();
        /** 틱 효과 - 3 */
        public static final ParticleEffect TICK_3 =
                ParticleEffect.Normal.builder(Particle.SPELL_WITCH).count(20).verticalSpread(2).build();
        /** 틱 효과 - 4 */
        public static final PlayableEffect.Function<Long> TICK_4 = i ->
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE,
                        Color.fromRGB((int) (30 + i * 1.95), 0, (int) (18 + i * 1.64))).build();
        /** 틱 효과 - 5 */
        public static final ParticleEffect TICK_5 =
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.CONCRETE, 14).build();
        /** 틱 효과 - 6 */
        public static final ParticleEffect TICK_6 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).count(3).horizontalSpread(0.1).verticalSpread(0.1)
                        .build();
        /** 틱 효과 - 7 */
        public static final ParticleEffect TICK_7 =
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.MYCEL, 0).count(4).horizontalSpread(0.15)
                        .verticalSpread(0.4).build();
        /** 폭발 */
        public static final PlayableEffect EXPLODE = PlayableEffect.list(
                SoundEffect.builder("new.block.conduit.deactivate").volume(3).pitch(0.6).build(),
                SoundEffect.builder("new.block.respawn_anchor.deplete").volume(3).pitch(0.6).build(),
                SoundEffect.builder("new.block.respawn_anchor.deplete").volume(3).pitch(0.8).build(),

                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.STAINED_GLASS, 2).count(300)
                        .horizontalSpread(0.3).verticalSpread(0.3).speed(0.4).build(),
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.STAINED_GLASS, 14).count(200)
                        .horizontalSpread(0.3).verticalSpread(0.3).speed(0.4).build());
        /** 엔티티 타격 - 1 */
        public static final ParticleEffect HIT_ENTITY_1 =
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(50).speed(0.4).build();
        /** 엔티티 타격 - 2 */
        public static final ParticleEffect HIT_ENTITY_2 =
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).count(3).horizontalSpread(0.05).verticalSpread(0.05).build();

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playUseTick(long i, @NonNull Location location) {
            location = location.clone().add(0, 0.1, 0);
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location);
            Vector axis = VectorUtil.getYawAxis(location);

            for (int j = 0; j < 2; j++) {
                long index = i * 2 + j;
                long index1 = Math.min(index, 14);
                double angle = index * 6.0;
                double up = 0;
                double distance = index1 * 0.15;

                if (index1 == 14)
                    up += (index - index1) * 0.15;

                for (int k = 0; k < 6; k++) {
                    angle += 360 / 3.0;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 3 ? angle : -angle).multiply(distance);
                    Location loc = location.clone().add(vec).add(0, up, 0);

                    USE_TICK.play(loc);
                }
            }
        }

        /**
         * 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 사용 위치
         * @param target   대상 위치
         */
        public static void playTick(long i, @NonNull Location location, @NonNull Location target) {
            target = target.clone().add(0, 1, 0);

            TICK_1.play(target);
            if (i < 8)
                TICK_2.play(target);

            location = location.clone().add(0, 0.1, 0);
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location);
            Vector axis = VectorUtil.getYawAxis(location);

            for (long j = (i >= 5 ? i - 5 : 0); j < i; j++) {
                double angle = j * (j > 30 ? -3.0 : 5.0);
                double distance = j * 0.16;

                for (int k = 0; k < 12; k++) {
                    angle += 360 / 6.0;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle).multiply(distance);
                    Location loc = location.clone().add(vec);

                    if (j > 0 && j % 10 == 0)
                        TICK_3.play(loc.clone().add(0, 2.5, 0));
                    else if (i > 20)
                        TICK_5.play(loc);
                    else
                        TICK_4.apply(j).play(loc);
                }
            }

            double angle = i * 4.0;
            for (int j = 0; j < 8; j++) {
                angle += 360 / 4.0;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 4 ? angle : -angle).multiply(8);
                Location loc1 = location.clone().add(vec);
                Location loc2 = loc1.clone().add(0, 2, 0);

                TICK_6.play(loc1);
                TICK_7.play(loc2);
            }
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
            for (Location loc : LocationUtil.getLine(start.clone().add(0, 1, 0), end, 0.4))
                HIT_ENTITY_2.play(loc);
        }
    }
}
