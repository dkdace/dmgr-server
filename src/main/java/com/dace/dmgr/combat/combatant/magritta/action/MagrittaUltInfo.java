package com.dace.dmgr.combat.combatant.magritta.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MagrittaUltInfo extends UltimateSkillInfo<MagrittaUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 11000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.6);
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SLOW = 40;
    /** 공격 쿨타임 */
    public static final Timespan ATTACK_COOLDOWN = Timespan.ofSeconds(0.1);
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(3);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;

    @Getter
    private static final MagrittaUltInfo instance = new MagrittaUltInfo();

    private MagrittaUltInfo() {
        super(MagrittaUlt.class, "초토화",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 기본 무기를 난사하여 강력한 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME_WITH_RPM, ATTACK_COOLDOWN.toSeconds(), 60 / ATTACK_COOLDOWN.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(1).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(1).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(1).pitch(0.6).build());
        /** 사격 */
        public static final SoundEffect SHOOT = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(2).pitch(0.8).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder("random.gun2.xm1014_1").volume(3).pitch(1).build(),
                SoundEffect.SoundInfo.builder("random.gun2.spas_12_1").volume(3).pitch(1).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb").volume(5).pitch(0.9).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb").volume(5).pitch(0.8).build());
        /** 사용 종료 */
        public static final SoundEffect END = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(2).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_BREAK).volume(2).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(2).pitch(1.4).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 255, 70, 0)
                        .build());
        /** 타격 */
        public static final ParticleEffect HIT = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.FLAME).speed(0.15).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.LAVA).build());
        /** 블록 타격 */
        public static final ParticleEffect HIT_BLOCK = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.DRIP_LAVA).build());
        /** 사용 종료 */
        public static final ParticleEffect END = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_LARGE).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SMOKE_NORMAL).count(50).speed(0.05).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.LAVA).count(15).build(),
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.IRON_BLOCK, 0).count(50)
                        .speed(0.1).build());
    }
}
