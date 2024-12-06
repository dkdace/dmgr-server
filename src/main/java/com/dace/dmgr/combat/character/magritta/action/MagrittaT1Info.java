package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public final class MagrittaT1Info extends TraitInfo {
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.2 * 20);
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 8;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 70;
    /** 최대치 */
    public static final int MAX = 4;

    /** 최대치 피해 점수 */
    public static final double MAX_DAMAGE_SCORE = 1;
    @Getter
    private static final MagrittaT1Info instance = new MagrittaT1Info();

    private MagrittaT1Info() {
        super("파쇄",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("<5:DAMAGE_INCREASE:수치>에 비례하여 마그리타로부터 <:DAMAGE_INCREASE:받는 피해>가 증가하는 상태이상입니다. " +
                                "최대치에 도달하면 불이 붙어 <:FIRE:화염 피해>를 받습니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.DAMAGE_INCREASE, "최대 " + MAX, ChatColor.DARK_PURPLE)
                        .addValueInfo(TextIcon.DAMAGE_INCREASE, "(파쇄)×{0}%", DAMAGE_INCREMENT)
                        .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
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
                SoundEffect.SoundInfo.builder("new.item.trident.hit").volume(2).pitch(0.8).pitchVariance(0.1).build());
        /** 최대치 */
        public static final SoundEffect MAX = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SKELETON_DEATH).volume(2).pitch(1.5).pitchVariance(0.1).build());
    }
}
