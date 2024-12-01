package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class PalasA2Info extends ActiveSkillInfo<PalasA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 15 * 20L;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 사용 점수 */
    public static final int USE_SCORE = 5;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    @Getter
    private static final PalasA2Info instance = new PalasA2Info();

    private PalasA2Info() {
        super(PalasA2.class, "생체 나노봇: 알파-X",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 아군에게 나노봇을 투여하여 일정 시간동안 모든 <:NEGATIVE_EFFECT:해로운 효과>에 면역시킵니다. " +
                                "<d::생체 나노봇： 아드레날린> 효과를 덮어씁니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
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
                new DefinedSound.SoundEffect(Sound.ENTITY_FIREWORK_SHOOT, 2, 1.8),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_SWIM, 2, 1.8),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_SWIM, 2, 2)
        );
        /** 엔티티 타격 */
        public static final DefinedSound HIT_ENTITY = new DefinedSound(
                new DefinedSound.SoundEffect("new.entity.puffer_fish.blow_out", 2, 1.8));
    }
}
