package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
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
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 클릭하여 벽을 오를 수 있습니다.")
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.1, 0.9).pitch(0.55, 0.8).pitchVariance(0.05).build());
    }
}
