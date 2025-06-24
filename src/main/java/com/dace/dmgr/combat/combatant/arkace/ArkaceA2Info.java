package com.dace.dmgr.combat.combatant.arkace;

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

public final class ArkaceA2Info extends ActiveSkillInfo<ArkaceA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(12);
    /** 치유량 */
    public static final int HEAL = 350;
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(2.5);

    /** 치유 점수 */
    public static final int HEAL_SCORE = 8;

    @Getter
    private static final ArkaceA2Info instance = new ArkaceA2Info();

    private ArkaceA2Info() {
        super(ArkaceA2.class, "생체 회복막",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("일정 시간동안 회복막을 활성화하여 체력을 <:HEAL:회복>합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.HEAL, HEAL)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1.5).pitch(0.9).build(),
                SoundEffect.builder(Sound.ITEM_ARMOR_EQUIP_DIAMOND).volume(1.5).pitch(1.4).build(),
                SoundEffect.builder(Sound.ITEM_ARMOR_EQUIP_DIAMOND).volume(1.5).pitch(1.2).build());
        /** 틱 효과 */
        public static final PlayableEffect.Function<Integer> TICK = i ->
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(220 - i * 30, 255, 36))
                        .count(3).verticalSpread(0.4).build();

        /**
         * 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 위치
         */
        public static void playTick(long i, @NonNull Location location) {
            location = location.clone().add(0, 1, 0);
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location);
            Vector axis = VectorUtil.getYawAxis(location);

            double angle = i * 10.0;
            for (int j = 0; j < 3; j++) {
                angle += 360 / 3.0;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                Location loc = location.clone().add(vec);

                TICK.apply(j).play(loc);
            }
        }
    }
}
