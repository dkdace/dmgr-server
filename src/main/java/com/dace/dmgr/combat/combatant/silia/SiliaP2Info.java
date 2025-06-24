package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.PassiveSkillInfo;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class SiliaP2Info extends PassiveSkillInfo<SiliaP2> {
    /** 벽타기 이동 강도 */
    public static final double PUSH = 0.45;
    /** 벽타기 최대 횟수 */
    public static final int USE_COUNT = 10;

    @Getter
    private static final SiliaP2Info instance = new SiliaP2Info();

    private SiliaP2Info() {
        super(SiliaP2.class, "상승 기류 - 2",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("벽을 클릭하여 벽을 오를 수 있습니다.")
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final SoundEffect USE =
                SoundEffect.builder(Sound.BLOCK_STONE_STEP).volume(0.9).pitch(0.55).pitchVariance(0.05).build();
        /** 액티브 3번 사용 중 사용 */
        public static final SoundEffect USE_A3 =
                SoundEffect.builder(Sound.BLOCK_STONE_STEP).volume(0.1).pitch(0.8).pitchVariance(0.05).build();
    }
}
