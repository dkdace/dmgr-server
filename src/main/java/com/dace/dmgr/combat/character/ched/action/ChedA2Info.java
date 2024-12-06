package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class ChedA2Info extends ActiveSkillInfo<ChedA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 4 * 20L;
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.25;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 0.75;
    @Getter
    private static final ChedA2Info instance = new ChedA2Info();

    private ChedA2Info() {
        super(ChedA2.class, "윈드스텝",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("이동 방향으로 짧게 도약합니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2, ActionKey.SPACE)
                        .build()
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ENDERDRAGON_FLAP).volume(1).pitch(1.3).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_LLAMA_SWAG).volume(1).pitch(1).build()
        );
    }
}
