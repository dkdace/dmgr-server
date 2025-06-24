package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.ScreenShake;
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
import org.bukkit.util.Vector;

public final class QuakerUltInfo extends UltimateSkillInfo<QuakerUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 6500;
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.8);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.5);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 20;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 1.5;
    /** 기절 시간 */
    public static final Timespan STUN_DURATION = Timespan.ofSeconds(1);
    /** 이동 속도 감소량 */
    public static final int SLOW = 30;
    /** 이동 속도 감소 시간 */
    public static final Timespan SLOW_DURATION = Timespan.ofSeconds(12);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 3;
    /** 흔들림 */
    public static final ScreenShake SHAKE = new ScreenShake(10, 8, Timespan.ofTicks(6));

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 30;

    @Getter
    private static final QuakerUltInfo instance = new QuakerUltInfo();

    private QuakerUltInfo() {
        super(QuakerUlt.class, "심판의 문지기",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("방패로 충격파를 일으켜 <:DAMAGE:광역 피해>와 <:STUN:기절>을 입히고 크게 <:KNOCKBACK:밀쳐냅니다>. " +
                                "맞은 적은 긴 시간동안 <:WALK_SPEED_DECREASE:이동 속도>가 느려집니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION.toSeconds())
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.TIME_WITH_PERCENT, SLOW_DURATION.toSeconds(), SLOW)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 준비 - 1 */
        public static final PlayableEffect USE_READY_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(5).pitch(0.5).build(),
                SoundEffect.builder(Sound.ENTITY_IRONGOLEM_DEATH).volume(5).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(5).pitch(0.7).build(),
                SoundEffect.builder(Sound.BLOCK_ANVIL_PLACE).volume(5).pitch(0.5).build(),
                SoundEffect.builder("random.explosion_reverb").volume(7).pitch(1.4).build());
        /** 사용 준비 - 2 */
        public static final ParticleEffect USE_READY_2 =
                ParticleEffect.Normal.builder(Particle.CRIT).count(100).horizontalSpread(0.2).verticalSpread(0.2).speed(0.6).build();
        /** 총알 궤적 */
        public static final PlayableEffect.Function<Vector> BULLET_TRAIL = velocity -> PlayableEffect.list(
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL, VectorUtil.getSpreadedVector(velocity.normalize(), 20)),
                ParticleEffect.Normal.builder(Particle.CRIT).count(4).horizontalSpread(0.2).verticalSpread(0.2).speed(0.15).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY =
                ParticleEffect.Normal.builder(Particle.CRIT).count(60).speed(0.4).build();

        /**
         * 사용 준비 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playUseReady(@NonNull Location location) {
            USE_READY_1.play(location);
            USE_READY_2.play(LocationUtil.getLocationFromOffset(location, 0, 0, 1.5));
        }
    }
}
