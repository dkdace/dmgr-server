package com.dace.dmgr.combat.combatant.neace;

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
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class NeaceA1Info extends ActiveSkillInfo<NeaceA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(10);
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 250;
    /** 최대 치유량 */
    public static final int MAX_HEAL = 1000;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(15);

    @Getter
    private static final NeaceA1Info instance = new NeaceA1Info();

    private NeaceA1Info() {
        super(NeaceA1.class, "구원의 표식",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 아군에게 표식을 남겨 일정 시간동안 <:HEAL:치유>합니다. " +
                                "이미 표식이 있는 아군에게 사용할 수 없으며, 치유량이 최대치에 도달하거나 지속 시간이 지나면 사라집니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, Format.PER_SECOND + " / 최대 {1}", HEAL_PER_SECOND, MAX_HEAL)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder("new.block.respawn_anchor.charge").volume(2).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder("new.block.note_block.chime").volume(2).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder("new.block.note_block.chime").volume(2).pitch(1.2).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 사용 */
        public static final ParticleEffect USE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.VILLAGER_HAPPY).count(2).build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(215, 255, 130)).count(2).horizontalSpread(0.1).verticalSpread(0.1).build(),
                ParticleEffect.NormalParticleInfo.builder(Particle.VILLAGER_HAPPY).build());
        /** 표식 */
        public static final ParticleEffect MARK = new ParticleEffect(
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.REDSTONE,
                        Color.fromRGB(215, 255, 130)).count(4).horizontalSpread(0.2).verticalSpread(0.2).build(),
                ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB,
                        Color.fromRGB(215, 255, 130)).build());
    }
}
