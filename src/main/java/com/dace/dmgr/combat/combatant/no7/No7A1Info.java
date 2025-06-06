package com.dace.dmgr.combat.combatant.no7;

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
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class No7A1Info extends ActiveSkillInfo<No7A1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(6);
    /** 이동 강도 */
    public static final double PUSH = 0.6;
    /** 피해량 */
    public static final int DAMAGE = 50;
    /** 보호막 */
    public static final int SHIELD = 150;
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
                        new ActionInfoLore.NamedSection("지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_1)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 틱 효과음 */
        public static final PlayableEffect TICK_SOUND = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(2).pitch(0.5).build(),
                SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(2).pitch(1.5).build());
        /** 틱 입자 효과 */
        public static final PlayableEffect.Function<Vector> TICK_PARTICLE = velocity -> PlayableEffect.list(
                ParticleEffect.Directional.create(Particle.SMOKE_NORMAL, velocity.clone().multiply(RandomUtils.nextDouble(0.2, 0.35))),
                ParticleEffect.Directional.create(Particle.FLAME, velocity.clone().multiply(RandomUtils.nextDouble(0.1, 0.25))));
        /** 엔티티 타격 */
        public static final PlayableEffect HIT_ENTITY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(2).pitch(0.7).build(),
                SoundEffect.builder(Sound.ENTITY_IRONGOLEM_HURT).volume(2).pitch(0.7).build(),

                ParticleEffect.Normal.builder(Particle.CRIT).count(40).speed(0.4).build());
    }
}
