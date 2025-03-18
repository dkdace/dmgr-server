package com.dace.dmgr.combat.combatant.jager.action;

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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class JagerA3Info extends ActiveSkillInfo<JagerA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(14);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 폭파 시간 */
    public static final Timespan EXPLODE_DURATION = Timespan.ofSeconds(5);
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 600;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 50;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 30;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 6;
    /** 빙결량 */
    public static final int FREEZE = 100;
    /** 속박 시간 */
    public static final Timespan SNARE_DURATION = Timespan.ofSeconds(1.2);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.6;

    /** 속박 점수 */
    public static final int SNARE_SCORE = 8;

    @Getter
    private static final JagerA3Info instance = new JagerA3Info();

    private JagerA3Info() {
        super(JagerA3.class, "빙결 수류탄",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("빙결 수류탄의 핀을 뽑습니다. " +
                                "수류탄은 일정 시간 후 폭발하여 적에게 <:DAMAGE:광역 피해>를 입히고 <5:WALK_SPEED_DECREASE:> <d::빙결>시키며, 적이 최대치의 빙결을 입으면 <:SNARE:속박>됩니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, EXPLODE_DURATION.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.VARIABLE, ChatColor.DARK_PURPLE, FREEZE, FREEZE / 2)
                        .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION.toSeconds())
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .build(),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("수류탄을 던집니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("투척", ActionKey.SLOT_3)
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
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ITEM_FLINTANDSTEEL_USE).volume(0.8).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder("new.block.chain.place").volume(0.8).pitch(1.2).build());
        /** 폭발 */
        public static final SoundEffect EXPLODE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_FIREWORK_LARGE_BLAST).volume(4).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(4).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CURE).volume(4).pitch(1.5).build(),
                SoundEffect.SoundInfo.builder("random.explosion_reverb").volume(6).pitch(1.2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 120, 220, 240)
                        .count(3).horizontalSpread(0.1).verticalSpread(0.1).build());
        /** 폭발 */
        public static final ParticleEffect EXPLODE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.ICE, 0).count(300)
                        .horizontalSpread(0.2).verticalSpread(0.2).speed(0.5).build(),
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.PACKED_ICE, 0).count(300)
                        .horizontalSpread(0.2).verticalSpread(0.2).speed(0.5).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.FIREWORKS_SPARK).count(200).speed(0.3).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_LARGE).build());
        /** 틱 입자 효과 (빙결) */
        public static final ParticleEffect FREEZE_TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 120, 220, 240)
                        .count(5)
                        .horizontalSpread(0, 0, 0.5)
                        .verticalSpread(1, 0, 0.5)
                        .build());
    }
}
