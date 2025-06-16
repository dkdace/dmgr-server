package com.dace.dmgr.combat.combatant.magritta;

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
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class MagrittaA2Info extends ActiveSkillInfo<MagrittaA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(10);
    /** 이동속도 증가량 */
    public static final int SPEED = 60;
    /** 지속 시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(1);

    @Getter
    private static final MagrittaA2Info instance = new MagrittaA2Info();

    private MagrittaA2Info() {
        super(MagrittaA2.class, "불꽃의 그림자",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("짧은 시간동안 <:WALK_SPEED_INCREASE:이동 속도>가 빨라지며 모든 공격을 받지 않습니다. " +
                                "사용 후 기본 무기를 재장전합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.RIGHT_CLICK)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_FIRE_EXTINGUISH).volume(1.5).pitch(2).build(),
                SoundEffect.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1.5).pitch(0.5).build());
        /** 틱 효과 - 1 */
        public static final PlayableEffect TICK_1 = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.SMOKE_LARGE).count(6).horizontalSpread(0.5).build(),
                ParticleEffect.Normal.builder(Particle.FLAME).count(4).horizontalSpread(0.4).build());
        /** 틱 효과 - 2 */
        public static final ParticleEffect TICK_2 =
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, Color.fromRGB(255, 70, 0)).count(6)
                        .horizontalSpread(1).verticalSpread(1.5).build();

        /**
         * 틱 효과를 재생한다.
         *
         * @param location 위치
         */
        public static void playTick(@NonNull Location location) {
            TICK_1.play(location.clone().add(0, 0.1, 0));
            TICK_2.play(location.clone().add(0, 1, 0));
        }
    }
}
