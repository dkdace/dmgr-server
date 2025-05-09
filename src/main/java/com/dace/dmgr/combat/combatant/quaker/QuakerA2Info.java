package com.dace.dmgr.combat.combatant.quaker;

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
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class QuakerA2Info extends ActiveSkillInfo<QuakerA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(14);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(1);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.6);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 1.2;
    /** 기절 시간 */
    public static final Timespan STUN_DURATION = Timespan.ofSeconds(0.8);
    /** 이동 속도 감소량 */
    public static final int SLOW = 40;
    /** 이동 속도 감소 시간 */
    public static final Timespan SLOW_DURATION = Timespan.ofSeconds(2.8);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;

    @Getter
    private static final QuakerA2Info instance = new QuakerA2Info();

    private QuakerA2Info() {
        super(QuakerA2.class, "충격파 일격",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바닥을 내려찍어 충격파를 일으켜 <:DAMAGE:광역 피해>와 <:STUN:기절>을 입히고 <:WALK_SPEED_DECREASE:이동 속도>를 감소시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION.toSeconds())
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.TIME_WITH_PERCENT, SLOW_DURATION.toSeconds(), SLOW)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(1).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("random.gun2.shovel_leftclick").volume(1).pitch(0.55).pitchVariance(0.1).build());
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_HURT).volume(3).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.ITEM_TOTEM_USE).volume(3).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_CRIT).volume(3).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_CRIT).volume(3).pitch(0.7).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(20).horizontalSpread(0.2).verticalSpread(0.05).speed(0.25).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(50).speed(0.4).build());
    }
}
