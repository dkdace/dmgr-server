package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class No7P2Info extends PassiveSkillInfo<No7P2> {
    /** 최소 초당 피해량 */
    public static final int MIN_DAMAGE_PER_SECOND = 40;
    /** 최대 초당 피해량 */
    public static final int MAX_DAMAGE_PER_SECOND = 80;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 4;

    @Getter
    private static final No7P2Info instance = new No7P2Info();

    private No7P2Info() {
        super(No7P2.class, "방전",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("<d::충전>으로 얻은 보호막을 가지고 있으면 주위에 전류를 방출하여 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "보호막이 많을 수록 피해량이 증가합니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_PER_SECOND, MIN_DAMAGE_PER_SECOND, MAX_DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 엔티티 타격 */
        public static final PlayableEffect.Function<Double> HIT_ENTITY = power -> PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_FIREWORK_BLAST).volume(0.4 + power * 0.4).pitch(1.6).build(),

                ParticleEffect.Normal.builder(Particle.CRIT).count((int) (10 + power * 20)).speed(0.2 + power * 0.4).build());

        /**
         * 엔티티 타격 효과를 재생한다.
         *
         * @param location 사용 위치
         * @param target   대상 위치
         * @param power    위력
         */
        public static void playHitEntity(@NonNull Location location, @NonNull Location target, double power) {
            HIT_ENTITY.apply(power).play(target);
            for (Location loc : LocationUtil.getLine(location, target, 0.4))
                CombatEffectUtil.BULLET_TRAIL_PARTICLE.play(loc);
        }
    }
}
