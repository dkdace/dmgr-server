package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.DistantDamage;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class ArkaceA1Info extends ActiveSkillInfo<ArkaceA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(7);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(0.5);
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 120;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3;
    /** 거리별 피해량 (폭발) */
    public static final DistantDamage DISTANT_DAMAGE_EXPLODE = new DistantDamage(DAMAGE_EXPLODE, RADIUS / 2);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.25;

    /** 직격 점수 */
    public static final int DIRECT_HIT_SCORE = 3;

    @Getter
    private static final ArkaceA1Info instance = new ArkaceA1Info();

    private ArkaceA1Info() {
        super(ArkaceA1.class, "D.I.A. 코어 미사일",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("소형 미사일을 연속으로 발사하여 <:DAMAGE:광역 피해>를 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE + " (폭발)", DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE_DIRECT + " (직격)")
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.LEFT_CLICK)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(32, 250, 225);

        /** 발사 */
        public static final PlayableEffect SHOOT = PlayableEffect.list(
                SoundEffect.builder("random.gun.grenade").volume(3).pitch(1.5).build(),
                SoundEffect.builder(Sound.ENTITY_SHULKER_SHOOT).volume(3).pitch(1.2).build());
        /** 총알 궤적 */
        public static final PlayableEffect BULLET_TRAIL = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).build());
        /** 폭발 */
        public static final PlayableEffect EXPLODE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_FIREWORK_LARGE_BLAST).volume(4).pitch(0.8).build(),
                SoundEffect.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(4).pitch(1.4).build(),
                SoundEffect.builder("random.gun_reverb2").volume(6).pitch(0.9).build(),

                ParticleEffect.Normal.builder(Particle.EXPLOSION_NORMAL).count(40).horizontalSpread(0.2).verticalSpread(0.2).speed(0.2).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(200).horizontalSpread(2.5)
                        .verticalSpread(2.5).build());
    }
}
