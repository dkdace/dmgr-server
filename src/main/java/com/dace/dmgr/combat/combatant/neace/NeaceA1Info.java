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
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

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

        /** 사용 - 1 */
        public static final PlayableEffect USE_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(2).pitch(1.6).build(),
                SoundEffect.builder("new.block.respawn_anchor.charge").volume(2).pitch(1.4).build(),
                SoundEffect.builder("new.block.note_block.chime").volume(2).pitch(1.6).build(),
                SoundEffect.builder("new.block.note_block.chime").volume(2).pitch(1.2).build());
        /** 사용 - 2 */
        public static final PlayableEffect USE_2 = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(2).horizontalSpread(0.1).verticalSpread(0.1)
                        .build(),
                ParticleEffect.Normal.builder(Particle.VILLAGER_HAPPY).build());
        /** 사용 - 3 */
        public static final ParticleEffect USE_3 =
                ParticleEffect.Normal.builder(Particle.VILLAGER_HAPPY).count(2).build();
        /** 표식 */
        public static final PlayableEffect MARK = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(4).horizontalSpread(0.2).verticalSpread(0.2)
                        .build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).build());

        /**
         * 사용 효과를 재생한다.
         *
         * @param location 사용 위치
         * @param start    시작 위치
         * @param end      끝 위치
         */
        public static void playUse(@NonNull Location location, @NonNull Location start, @NonNull Location end) {
            USE_1.play(location);

            start = LocationUtil.getLocationFromOffset(start, 0, 0, 1.5);

            for (Location loc : LocationUtil.getLine(start, end, 0.4))
                USE_2.play(loc);

            Vector vector = VectorUtil.getYawAxis(start).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(start);

            for (int i = 0; i < 8; i++) {
                double angle = i * 10.0;

                for (int j = 0; j < 10; j++) {
                    angle += 360 / 5.0;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 5 ? angle : -angle).multiply(1 + i * 0.2);
                    Location loc = start.clone().add(vec);

                    USE_3.play(loc);
                }
            }
            for (int i = 0; i < 7; i++) {
                Location loc1 = LocationUtil.getLocationFromOffset(start, -0.525 + i * 0.15, 0, 0);
                Location loc2 = LocationUtil.getLocationFromOffset(start, 0, -0.525 + i * 0.15, 0);
                USE_3.play(loc1);
                USE_3.play(loc2);
            }
        }
    }
}
