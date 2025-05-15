package com.dace.dmgr.combat.combatant.no7;

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
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class No7A1Info extends ActiveSkillInfo<No7A1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(6);
    /** 이동 강도 */
    public static final double PUSH = 0.6;
    /** 피해량 */
    public static final int DAMAGE = 80;
    /** 보호막 */
    public static final int SHIELD = 200;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 2;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.8;
    /** 피해 쿨타임 */
    public static final Timespan DAMAGE_COOLDOWN = Timespan.ofSeconds(0.25);
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;

    @Getter
    private static final No7A1Info instance = new No7A1Info();

    private No7A1Info() {
        super(No7A1.class, "돌파",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 돌진하여 적과 부딪히면 <:DAMAGE:피해>를 입히고 <:KNOCKBACK:밀쳐내며>, <e:HEAL:보호막>을 얻습니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.HEAL, ChatColor.YELLOW, SHIELD)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build(),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_1)
                                .build())));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(2).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(2).pitch(1.5).build());
        /** 엔티티 타격 */
        public static final SoundEffect HIT_ENTITY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_HURT).volume(2).pitch(0.7).build());
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class Particles {
        /** 틱 입자 효과 */
        public static final ParticleEffect TICK = new ParticleEffect(
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.SMOKE_NORMAL)
                        .speedMultiplier(1, 0.2, 0.35)
                        .build(),
                ParticleEffect.DirectionalParticleInfo.builder(0, Particle.FLAME)
                        .speedMultiplier(1, 0.1, 0.25)
                        .build());
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(40).speed(0.4).build());
    }
}
