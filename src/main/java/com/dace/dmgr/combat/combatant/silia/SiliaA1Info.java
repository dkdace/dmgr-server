package com.dace.dmgr.combat.combatant.silia;

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
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public final class SiliaA1Info extends ActiveSkillInfo<SiliaA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(8);
    /** 이동 거리 (단위: 블록) */
    public static final int MOVE_DISTANCE = 15;
    /** 이동 강도 */
    public static final double PUSH = 2.5;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;

    @Getter
    private static final SiliaA1Info instance = new SiliaA1Info();

    private SiliaA1Info() {
        super(SiliaA1.class, "연풍 가르기",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("앞으로 빠르게 이동하며 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "적을 처치하면 <7:COOLDOWN:쿨타임>이 초기화됩니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MOVE_DISTANCE)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder("new.item.trident.throw").volume(1.5).pitch(0.8).build(),
                SoundEffect.builder("random.swordhit").volume(1.5).pitch(0.8).build(),
                SoundEffect.builder("random.swordhit").volume(1.5).pitch(0.8).build());
        /** 틱 효과 */
        public static final PlayableEffect TICK = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.CRIT).count(3).horizontalSpread(0.02).verticalSpread(0.02).build(),
                ParticleEffect.Normal.builder(Particle.END_ROD).horizontalSpread(0.02).verticalSpread(0.02).build());
        /** 총알 궤적 - 1 */
        public static final ParticleEffect BULLET_TRAIL_1 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, SiliaWeaponInfo.Effects.COLOR).build();
        /** 총알 궤적 - 2 */
        public static final PlayableEffect.Function<Vector> BULLET_TRAIL_2 = velocity ->
                ParticleEffect.Directional.create(Particle.EXPLOSION_NORMAL, velocity.clone().multiply(-0.4));
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY =
                ParticleEffect.Normal.builder(Particle.CRIT).count(40).speed(0.4).build();
    }
}
