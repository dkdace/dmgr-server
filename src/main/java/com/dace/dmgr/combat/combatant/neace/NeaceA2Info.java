package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class NeaceA2Info extends ActiveSkillInfo<NeaceA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(1);
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 15;
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 15;
    /** 최대 지속시간 */
    public static final Timespan MAX_DURATION = Timespan.ofSeconds(8);
    /** 최대 충전 시간 */
    public static final Timespan RECOVER_DURATION = Timespan.ofSeconds(6);

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;

    @Getter
    private static final NeaceA2Info instance = new NeaceA2Info();

    private NeaceA2Info() {
        super(NeaceA2.class, "축복",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 기본 무기의 치유 대상을 <3::축복>할 수 있습니다. " +
                                "사용 중에는 기본 무기로 치유할 수 없습니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME_WITH_MAX_TIME, MAX_DURATION.toSeconds(), RECOVER_DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new AbilityInfoLore.NamedSection("축복", AbilityInfoLore.Section
                                .builder("<:DAMAGE_INCREASE:공격력>과 <:DEFENSE_INCREASE:방어력>이 증가합니다.")
                                .addValueInfo(TextIcon.DAMAGE_INCREASE, Format.PERCENT, DAMAGE_INCREMENT)
                                .addValueInfo(TextIcon.DEFENSE_INCREASE, Format.PERCENT, DEFENSE_INCREMENT)
                                .build()),
                        new AbilityInfoLore.NamedSection("재사용 시", AbilityInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_2)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 활성화 */
        public static final PlayableEffect ON = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.5).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.4).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.4).build(),
                SoundEffect.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.5).pitch(1.4).build());
        /** 사용 시 틱 효과 */
        public static final PlayableEffect.Function<Long> USE_TICK = i ->
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE,
                                Color.fromRGB((int) (200 - i * 6.67), 255, (int) (160 + i * 9.45))).count(6).horizontalSpread(0.2)
                        .verticalSpread(0.2).build();
        /** 틱 효과 */
        public static final ParticleEffect TICK =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(140, 255, 245)).count(3)
                        .horizontalSpread(1).verticalSpread(1.5).build();
        /** 비활성화 */
        public static final SoundEffect OFF =
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(1).pitch(1.8).build();

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playUseTick(long i, @NonNull Location location) {
            location = location.clone();
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location).multiply(1.3);
            Vector axis = VectorUtil.getYawAxis(location);

            double angle = i * 14.0;
            for (int j = 0; j < 4; j++) {
                angle += 360 / 4.0;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                Location loc = location.clone().add(vec).add(0, (i * 4 + j) * 0.05, 0);

                USE_TICK.apply(i).play(loc);
            }
        }
    }
}
