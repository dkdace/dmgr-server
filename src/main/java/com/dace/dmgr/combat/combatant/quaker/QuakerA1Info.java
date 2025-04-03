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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class QuakerA1Info extends ActiveSkillInfo<QuakerA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(1);
    /** 사망 시 쿨타임 */
    public static final Timespan COOLDOWN_DEATH = Timespan.ofSeconds(4);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.4);
    /** 체력 */
    public static final int HEALTH = 5000;
    /** 체력 최대 회복 시간 */
    public static final Timespan RECOVER_DURATION = Timespan.ofSeconds(14);
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SLOW = 25;

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 50;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 20;

    @Getter
    private static final QuakerA1Info instance = new QuakerA1Info();

    private QuakerA1Info() {
        super(QuakerA1.class, "불굴의 방패",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("커다란 <3::방패>를 펼쳐 전방의 공격을 방어합니다. " +
                                "사용 중에는 <:WALK_SPEED_DECREASE:이동 속도>가 느려집니다.")
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.PERCENT, USE_SLOW)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("방패", ActionInfoLore.Section
                                .builder("공격을 막는 방벽입니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME + " (파괴 시)", COOLDOWN_DEATH.toSeconds())
                                .addValueInfo(TextIcon.HEALTH, HEALTH)
                                .build()),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK)
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1).pitch(0.6).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_SHULKER_BOX_OPEN).volume(1).pitch(0.7).build());
        /** 해제 */
        public static final SoundEffect DISABLE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_SHULKER_BOX_CLOSE).volume(1).pitch(1.4).build());
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ANVIL_LAND).volume(0.25).pitch(1.2).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder("random.metalhit").volume(0.3).pitch(0.85).pitchVariance(0.1).build());
        /** 파괴 */
        public static final SoundEffect DEATH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_HURT).volume(2).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder("random.metalhit").volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ITEM_SHIELD_BLOCK).volume(2).pitch(0.5).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 파괴 */
        public static final ParticleEffect DEATH = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.IRON_BLOCK, 0).count(50)
                        .horizontalSpread(0.3).verticalSpread(0.3).speed(0.2).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(50).horizontalSpread(0.3).verticalSpread(0.3).speed(0.4).build());
    }
}
