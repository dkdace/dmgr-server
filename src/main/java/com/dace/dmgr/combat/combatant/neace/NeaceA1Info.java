package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
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
    public static final int HEAL_PER_SECOND = 200;
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
                                "이미 표식이 있는 아군에게 사용할 수 없으며, 치유량이 최대치에 도달하거나 지속 시간이 지나면 사라집니다. " +
                                "기본 무기로 치유하고 있는 대상은 치유할 수 없습니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, Format.PER_SECOND + " / 최대 {1}", HEAL_PER_SECOND, MAX_HEAL)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(215, 255, 130);

        /** 사용 효과음 */
        public static final PlayableEffect USE_SOUND = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.6).build(),
                SoundEffect.builder("new.block.respawn_anchor.charge").volume(2).pitch(1.4).build(),
                SoundEffect.builder("new.block.note_block.chime").volume(2).pitch(1.6).build(),
                SoundEffect.builder("new.block.note_block.chime").volume(2).pitch(1.2).build());
        /** 사용 입자 효과 - 1 */
        public static final PlayableEffect USE_PARTICLE_1 = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(2).horizontalSpread(0.1).verticalSpread(0.1)
                        .build(),
                ParticleEffect.Normal.builder(Particle.VILLAGER_HAPPY).build());
        /** 사용 입자 효과 - 2 */
        public static final ParticleEffect USE_PARTICLE_2 =
                ParticleEffect.Normal.builder(Particle.VILLAGER_HAPPY).count(2).build();
        /** 표식 */
        public static final PlayableEffect MARK = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(4).horizontalSpread(0.2).verticalSpread(0.2)
                        .build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).build());
    }
}
