package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class MetarP1Info extends PassiveSkillInfo<MetarP1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.5);
    @Getter
    private static final MetarP1Info instance = new MetarP1Info();

    private MetarP1Info() {
        super(MetarP1.class, "중기갑",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("웅크리기 상태일 때 <:KNOCKBACK:밀쳐내기> 효과를 받지 않습니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SNEAK)
                        .build()));
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.8).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.8).pitch(0.7).build());
        /** 해제 */
        public static final SoundEffect DISABLE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_STEP).volume(0.8).pitch(1.6).build());
    }
}
