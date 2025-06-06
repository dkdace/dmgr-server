package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class SiliaP1Info extends PassiveSkillInfo<SiliaP1> {
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.55;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 0.35;

    @Getter
    private static final SiliaP1Info instance = new SiliaP1Info();

    private SiliaP1Info() {
        super(SiliaP1.class, "상승 기류 - 1",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("공중 점프가 가능합니다.")
                        .addActionKeyInfo("사용", ActionKey.SPACE)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_LLAMA_SWAG).volume(0.8).pitch(1.2).build(),
                SoundEffect.builder(Sound.BLOCK_CLOTH_STEP).volume(0.8).pitch(1.2).build());
        /** 액티브 3번 사용 중 사용 */
        public static final PlayableEffect USE_A3 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_LLAMA_SWAG).volume(0.1).pitch(1.4).build(),
                SoundEffect.builder(Sound.BLOCK_CLOTH_STEP).volume(0.1).pitch(1.4).build());
    }
}
