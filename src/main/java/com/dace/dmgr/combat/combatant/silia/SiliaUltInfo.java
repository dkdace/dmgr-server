package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Particle;

public final class SiliaUltInfo extends UltimateSkillInfo<SiliaUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(1);
    /** 지속시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(4);
    /** 처치 시 지속시간 증가 */
    public static final Timespan DURATION_ADD_ON_KILL = Timespan.ofSeconds(2);
    /** 이동속도 증가량 */
    public static final int SPEED = 30;
    /** 일격 쿨타임 */
    public static final Timespan STRIKE_COOLDOWN = Timespan.ofSeconds(0.55);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 25;

    @Getter
    private static final SiliaUltInfo instance = new SiliaUltInfo();

    private SiliaUltInfo() {
        super(SiliaUlt.class, "폭풍의 부름",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 <:WALK_SPEED_INCREASE:이동 속도>가 빨라지고 기본 공격 시 <d::일격>을 날립니다. " +
                                "적 처치 시 <7:DURATION:지속 시간>이 늘어나며, 사용 중에는 <d::진권풍>, <d::폭풍전야>를 사용할 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME + " (+{1}초)", DURATION.toSeconds(), DURATION_ADD_ON_KILL.toSeconds())
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 시 틱 효과 - 1 */
        public static final ParticleEffect USE_TICK_1 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, SiliaWeaponInfo.Effects.COLOR).count(2)
                        .horizontalSpread(0.15).verticalSpread(0.15).build();
        /** 사용 시 틱 효과 - 2 */
        public static final ParticleEffect USE_TICK_2 =
                ParticleEffect.Normal.builder(Particle.CRIT).count(2).horizontalSpread(0.08).verticalSpread(0.08).speed(0.08).build();
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder("random.swordhit").volume(2).pitch(1).build(),
                SoundEffect.builder("random.swordhit").volume(2).pitch(0.7).build(),
                SoundEffect.builder("new.item.trident.return").volume(2.5).pitch(1.4).build(),
                SoundEffect.builder("new.item.trident.return").volume(2.5).pitch(1.2).build());

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 사용 위치
         * @param prev     이전 위치
         */
        public static void playUseTick(long i, @NonNull Location location, @NonNull Location prev) {
            location = location.clone().add(0, 1, 0);

            for (int j = 0; j < 6; j++) {
                long index = i * 6 + j;
                long index1 = Math.min(index, 66);
                double angle = index1 * 1.7;
                double forward = -1.4;
                float pitch = index1 * 4F;

                if (index1 == 66) {
                    long subIndex = index - index1;
                    angle -= subIndex * 2.3;
                    forward += subIndex * 0.0025;
                    pitch += subIndex * 5;
                }

                location.setYaw((float) (prev.getYaw() + angle));
                location.setPitch(pitch);

                for (int k = 0; k < 3; k++) {
                    Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, forward * k);

                    if (k == 2)
                        USE_TICK_2.play(loc);
                    else
                        USE_TICK_1.play(loc);
                }
            }
        }
    }
}
