package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
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

public final class ChedP1Info extends PassiveSkillInfo<ChedP1> {
    /** 벽타기 이동 강도 */
    public static final double PUSH = 0.45;
    /** 벽타기 최대 횟수 */
    public static final int USE_COUNT = 10;
    /** 매달리기 최대 시간 */
    public static final Timespan HANG_DURATION = Timespan.ofSeconds(6);

    @Getter
    private static final ChedP1Info instance = new ChedP1Info();

    private ChedP1Info() {
        super(ChedP1.class, "궁사의 날렵함",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 클릭하여 벽을 오를 수 있습니다. 오르는 도중 <3::매달리기>를 할 수 있습니다.")
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("매달리기", ActionInfoLore.Section
                                .builder("벽에 매달려 위치를 고정합니다.")
                                .addValueInfo(TextIcon.DURATION, Format.TIME, HANG_DURATION.toSeconds())
                                .addActionKeyInfo("사용", ActionKey.SNEAK)
                                .build()),
                        new ActionInfoLore.NamedSection("매달리기: 지속시간 종료/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addActionKeyInfo("해제", ActionKey.SNEAK)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(186, 55, 30);

        /** 사용 */
        public static final SoundEffect USE =
                SoundEffect.builder(Sound.BLOCK_STONE_STEP).volume(1).pitch(0.525).pitchVariance(0.05).build();
        /** 매달리기 - 사용 */
        public static final ParticleEffect HANG_USE =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, COLOR).count(40).horizontalSpread(0.65).build();
        /** 매달리기 - 활성화 */
        public static final PlayableEffect HANG_ON = PlayableEffect.list(
                SoundEffect.builder("new.entity.phantom.flap").volume(1).pitch(1.7).build(),
                SoundEffect.builder(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL).volume(0.6).pitch(0.85).build());
        /** 매달리기 - 틱 효과 */
        public static final ParticleEffect HANG_TICK =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).horizontalSpread(0.24).build();
        /** 매달리기 - 비활성화 */
        public static final PlayableEffect HANG_OFF = PlayableEffect.list(
                SoundEffect.builder("new.entity.phantom.flap").volume(1).pitch(1.8).build(),
                SoundEffect.builder(Sound.ENTITY_LLAMA_SWAG).volume(0.6).pitch(1.4).build());

        /**
         * 매달리기 - 틱 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playHangTick(@NonNull Location location) {
            location = location.clone();
            location.setYaw(0);
            location.setPitch(0);

            Vector vector = VectorUtil.getRollAxis(location).multiply(0.65);
            Vector axis = VectorUtil.getYawAxis(location);

            for (int i = 0; i < 7; i++) {
                double angle = 360 / 7.0 * i;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                Location loc = location.clone().add(vec);

                HANG_TICK.play(loc);
            }
        }
    }
}
