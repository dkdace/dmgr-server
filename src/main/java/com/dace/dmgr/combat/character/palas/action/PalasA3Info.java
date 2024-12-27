package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class PalasA3Info extends ActiveSkillInfo<PalasA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 16 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 최대 체력 증가 비율 */
    public static final double HEALTH_INCREASE_RATIO = 0.3;
    /** 최대 체력 감소 비율 */
    public static final double HEALTH_DECREASE_RATIO = 0.3;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 지속시간 (tick) */
    public static final long DURATION = 6 * 20L;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 10;
    @Getter
    private static final PalasA3Info instance = new PalasA3Info();

    private PalasA3Info() {
        super(PalasA3.class, "R.S.K. 생체 제어 수류탄",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("특수 수류탄을 던져 범위의 적에게는 <c:HEAL_DECREASE:최대 체력>을 감소시키고, 아군에게는 <:HEAL_INCREASE:최대 체력>을 증가시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.HEAL_DECREASE, Format.PERCENT, ChatColor.RED, (int) (100 * HEALTH_DECREASE_RATIO))
                        .addValueInfo(TextIcon.HEAL_INCREASE, Format.PERCENT, (int) (100 * HEALTH_INCREASE_RATIO))
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_CAT_PURREOW).volume(0.5).pitch(1.6).build());
        /** 폭발 */
        public static final SoundEffect EXPLODE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_BREWING_STAND_BREW).volume(2).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SWIM).volume(2).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_GLASS_BREAK).volume(2).pitch(1.2).build()
        );
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 220, 161, 43)
                        .count(5).horizontalSpread(0.15).verticalSpread(0.15).build());
        /** 폭발 */
        public static final ParticleEffect EXPLODE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.STAINED_GLASS, 4)
                        .count(400).horizontalSpread(0.1).verticalSpread(0.1).speed(0.25).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.TOTEM).count(200).horizontalSpread(0.15).verticalSpread(0.15).speed(0.6).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.SPELL_INSTANT).count(300).horizontalSpread(1.5).verticalSpread(1.5).speed(1).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.WATER_SPLASH).count(300).horizontalSpread(0.6).verticalSpread(0.6).build()
        );
    }
}
