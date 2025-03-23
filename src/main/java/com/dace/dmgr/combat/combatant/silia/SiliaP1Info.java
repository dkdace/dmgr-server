package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
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
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_LLAMA_SWAG).volume(0.1, 0.8).pitch(1.2, 1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_CLOTH_STEP).volume(0.1, 0.8).pitch(1.2, 1.4).build());
    }
}
