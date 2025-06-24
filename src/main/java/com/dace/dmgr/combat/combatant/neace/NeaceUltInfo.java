package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.UltimateSkillInfo;
import com.dace.dmgr.effect.FireworkEffect;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.util.Vector;

public final class NeaceUltInfo extends UltimateSkillInfo<NeaceUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.8);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(12);

    @Getter
    private static final NeaceUltInfo instance = new NeaceUltInfo();

    private NeaceUltInfo() {
        super(NeaceUlt.class, "치유의 성역",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("체력을 최대치로 즉시 <:HEAL:회복>하고 일정 시간동안 여러 대상을 자동으로 치유합니다. " +
                                "사용 중에는 공격할 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(215, 255, 130);

        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.2).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.2).build(),
                SoundEffect.builder("new.block.respawn_anchor.charge").volume(2).pitch(0.7).build());
        /** 사용 시 틱 효과 */
        public static final PlayableEffect USE_TICK = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.VILLAGER_HAPPY).count(3).horizontalSpread(0.05).verticalSpread(0.05).build(),
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.GRASS, 0).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).build());
        /** 사용 준비 효과음 */
        public static final PlayableEffect USE_READY_SOUND = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON).volume(3).pitch(1.1).build(),
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON).volume(3).pitch(1.1).build());
        /** 사용 준비 폭죽 효과 */
        public static final FireworkEffect USE_READY_FIREWORK = FireworkEffect.builder(org.bukkit.FireworkEffect.Type.STAR, COLOR)
                .fadeColor(Color.fromRGB(255, 255, 255)).trail().build();
        /** 틱 효과 */
        public static final ParticleEffect TICK =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).horizontalSpread(0.1).verticalSpread(0.1).build();

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playUseTick(long i, @NonNull Location location) {
            location = location.clone().add(0, 0.1, 0);
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location);
            Vector axis = VectorUtil.getYawAxis(location);

            for (int j = 0; j < 3; j++) {
                long index = i * 3 + j;
                long index1 = Math.min(index, 26);
                double angle = index1 * 7.0;
                double distance = index1 * 0.35;
                double up = 0;

                if (index1 == 26) {
                    long subIndex = index - index1;
                    angle -= subIndex * 31;
                    up = subIndex * 0.2;
                }

                for (int k = 0; k < 8; k++) {
                    angle += 360 / 4.0;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 4 ? angle : -angle).multiply(distance);
                    Location loc = location.clone().add(vec).add(0, up, 0);

                    USE_TICK.play(loc);
                }
            }
        }

        /**
         * 사용 준비 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playUseReady(@NonNull Location location) {
            USE_READY_SOUND.play(location);
            USE_READY_FIREWORK.play(location);
        }

        /**
         * 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playTick(long i, @NonNull Location location) {
            NeaceWeaponInfo.Effects.HEAL_USE_SOUND.play(location);

            location = location.clone().add(0, 0.1, 0);
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location).multiply(1.5);
            Vector axis = VectorUtil.getYawAxis(location);

            double angle = i * 5.0;
            for (int j = 0; j < 6; j++) {
                angle += 360 / 3.0;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 3 ? angle : -angle);
                Location loc = location.clone().add(vec);

                TICK.play(loc);
            }
        }
    }
}
