package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.util.Vector;

public final class JagerUltInfo extends UltimateSkillInfo<JagerUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 10000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 소환 시간 */
    public static final Timespan SUMMON_DURATION = Timespan.ofSeconds(1);
    /** 체력 */
    public static final int HEALTH = 1000;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 100;
    /** 최소 피해 범위 (단위: 블록) */
    public static final double MIN_RADIUS = 4;
    /** 최대 피해 범위 (단위: 블록) */
    public static final double MAX_RADIUS = 12;
    /** 최대 피해 범위에 도달하는 시간 */
    public static final Timespan MAX_RADIUS_DURATION = Timespan.ofSeconds(5);
    /** 초당 빙결량 */
    public static final int FREEZE_PER_SECOND = 40;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(20);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 30;
    /** 궁극기 처치 점수 제한시간 */
    public static final Timespan KILL_SCORE_TIME_LIMIT = Timespan.ofSeconds(2);
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 25;

    @Getter
    private static final JagerUltInfo instance = new JagerUltInfo();

    private JagerUltInfo() {
        super(JagerUlt.class, "백야의 눈폭풍",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("<3::눈폭풍 발생기>를 던져 긴 시간동안 눈폭풍을 일으킵니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .build(),
                        new ActionInfoLore.NamedSection("눈폭풍 발생기", ActionInfoLore.Section
                                .builder("일정 시간동안 <:DAMAGE:광역 피해>와 <5:WALK_SPEED_DECREASE:> <d::빙결>을 입히는 눈폭풍을 일으킵니다. " +
                                        "눈폭풍의 범위는 시간에 따라 점차 넓어집니다.")
                                .addValueInfo(TextIcon.HEAL, HEALTH)
                                .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                                .addValueInfo(TextIcon.DAMAGE, Format.PER_SECOND, DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.PER_SECOND, ChatColor.DARK_PURPLE, FREEZE_PER_SECOND)
                                .addValueInfo(TextIcon.RADIUS, "{0}m ~ {1}m (0초~{2}초)",
                                        MIN_RADIUS, MAX_RADIUS, MAX_RADIUS_DURATION.toSeconds())
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_CAT_PURREOW).volume(0.5).pitch(1.6).build());
        /** 소환 */
        public static final SoundEffect SUMMON = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.5).pitch(0.5).build());
        /** 소환 준비 대기 */
        public static final SoundEffect SUMMON_BEFORE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(0.8).pitch(1.7).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ITEM_ELYTRA_FLYING).volume(3).pitch(1.3).pitchVariance(0.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ITEM_ELYTRA_FLYING).volume(3).pitch(1.7).pitchVariance(0.2).build());
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.metalhit").volume(0.4).pitch(1.1).pitchVariance(0.1).build());
        /** 파괴 */
        public static final SoundEffect DEATH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_BREAK).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(2).pitch(1.2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(96, 220, 255)).count(15).horizontalSpread(0.6).verticalSpread(0.02).build());
        /** 소환 준비 대기 틱 입자 효과 */
        public static final ParticleEffect SUMMON_BEFORE_READY_TICK = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(Particle.EXPLOSION_NORMAL, new Vector(0, -0.3, 0)).build());
        /** 표시 */
        public static final ParticleEffect DISPLAY = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(96, 220, 255)).count(8).horizontalSpread(0.6).verticalSpread(0.02).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(80, 80, 100)).count(3).horizontalSpread(0.15).verticalSpread(0.02).build());
        /** 틱 입자 효과 (중심) */
        public static final ParticleEffect TICK_CORE = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.EXPLOSION_NORMAL)
                        .speedMultiplier(1, 0.35, 0.05)
                        .build());
        /** 틱 입자 효과 (장식) */
        public static final ParticleEffect TICK_DECO = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.SNOW_SHOVEL).count(3).verticalSpread(1.4).speed(0.04).build());
        /** 파괴 */
        public static final ParticleEffect DEATH = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.IRON_BLOCK, 0).count(120)
                        .horizontalSpread(0.1).verticalSpread(0.1).speed(0.15).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(80).horizontalSpread(0.1).verticalSpread(0.1).speed(0.5).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_LARGE).build());
    }
}
