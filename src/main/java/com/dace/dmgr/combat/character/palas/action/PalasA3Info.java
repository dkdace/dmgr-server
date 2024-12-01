package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public final class PalasA3Info extends ActiveSkillInfo<PalasA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 16 * 20L;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 최대 체력 증가 비율 */
    public static final double HEALTH_INCREASE_RATIO = 0.3;
    /** 최대 체력 감소 비율 */
    public static final double HEALTH_DECREASE_RATIO = 0.3;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 지속시간 (tick) */
    public static final long DURATION = 6 * 20L;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 10;
    @Getter
    private static final PalasA3Info instance = new PalasA3Info();

    private PalasA3Info() {
        super(PalasA3.class, "R.S.K. 생체 제어 수류탄",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("특수 수류탄을 던져 범위의 적에게는 <c:HEAL_DECREASE:최대 체력>을 감소시키고, 아군에게는 <:HEAL_INCREASE:최대 체력>을 증가시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.HEAL_DECREASE, Format.PERCENT, ChatColor.RED, (int) (100 * HEALTH_DECREASE_RATIO))
                        .addValueInfo(TextIcon.HEAL_INCREASE, Format.PERCENT, (int) (100 * HEALTH_INCREASE_RATIO))
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
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
        public static final DefinedSound USE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_CAT_PURREOW, 0.5, 1.6));
        /** 폭발 */
        public static final DefinedSound EXPLODE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.BLOCK_FIRE_EXTINGUISH, 2, 0.7),
                new DefinedSound.SoundEffect(Sound.BLOCK_BREWING_STAND_BREW, 2, 1.2),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_SWIM, 2, 1.2),
                new DefinedSound.SoundEffect(Sound.BLOCK_GLASS_BREAK, 2, 1.2)
        );
    }
}
