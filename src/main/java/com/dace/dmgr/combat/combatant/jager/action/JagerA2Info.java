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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class JagerA2Info extends ActiveSkillInfo<JagerA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(10);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 15;
    /** 소환 시간 */
    public static final Timespan SUMMON_DURATION = Timespan.ofSeconds(1.5);
    /** 체력 */
    public static final int HEALTH = 400;
    /** 피해량 */
    public static final int DAMAGE = 300;
    /** 속박 시간 */
    public static final Timespan SNARE_DURATION = Timespan.ofSeconds(3);

    /** 속박 점수 */
    public static final int SNARE_SCORE = 20;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 10;
    @Getter
    private static final JagerA2Info instance = new JagerA2Info();

    private JagerA2Info() {
        super(JagerA2.class, "곰덫",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("눈에 잘 띄지 않는 <3::곰덫>을 던져 설치합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new ActionInfoLore.NamedSection("곰덫", ActionInfoLore.Section
                                .builder("밟은 적은 <:DAMAGE:피해>를 입고 <:SNARE:속박>됩니다.")
                                .addValueInfo(TextIcon.HEAL, HEALTH)
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION.toSeconds())
                                .build()
                        )
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
        /** 소환 */
        public static final SoundEffect SUMMON = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_HORSE_ARMOR).volume(0.5).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder("random.craft").volume(0.5).pitch(1.3).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.5).pitch(0.5).build()
        );
        /** 소환 준비 */
        public static final SoundEffect SUMMON_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.5).pitch(0.5).build());
        /** 발동 */
        public static final SoundEffect TRIGGER = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_SHEEP_SHEAR).volume(2).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder("new.entity.player.hurt_sweet_berry_bush").volume(2).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder("random.metalhit").volume(2).pitch(1.2).build()
        );
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.metalhit").volume(0.4).pitch(1.1).pitchVariance(0.1).build());
        /** 파괴 */
        public static final SoundEffect DEATH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(1).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder("random.metalhit").volume(1).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_BREAK).volume(1).pitch(0.8).build()
        );
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 총알 궤적 */
        public static final ParticleEffect BULLET_TRAIL = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE, 120, 120, 135)
                        .count(17).horizontalSpread(0.7).build());
        /** 소환 준비 대기 틱 입자 효과 */
        public static final ParticleEffect SUMMON_BEFORE_READY_TICK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB, 120, 120, 135)
                        .count(5).horizontalSpread(0.2).verticalSpread(0.2).build());
        /** 표시 */
        public static final ParticleEffect DISPLAY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.TOWN_AURA).build());
        /** 파괴 */
        public static final ParticleEffect DEATH = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.IRON_BLOCK, 0).count(80)
                        .horizontalSpread(0.1).verticalSpread(0.1).speed(0.15).build());
    }
}
