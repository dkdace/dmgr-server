package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class PalasUltInfo extends UltimateSkillInfo<PalasUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 40;
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 60;
    /** 이동속도 증가량 */
    public static final int SPEED_INCREMENT = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = 7 * 20L;

    /** 사용 점수 */
    public static final int USE_SCORE = 10;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 30;
    @Getter
    private static final PalasUltInfo instance = new PalasUltInfo();

    private PalasUltInfo() {
        super(PalasUlt.class, "생체 나노봇: 아드레날린",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 아군에게 나노봇을 투여하여 일정 시간동안 <:DAMAGE_INCREASE:공격력>과 <:WALK_SPEED_INCREASE:이동 속도>를 증폭시킵니다. " +
                                "<d::생체 나노봇： 알파-X> 효과를 덮어씁니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.DAMAGE_INCREASE, Format.PERCENT, DAMAGE_INCREMENT)
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED_INCREMENT)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
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
                new DefinedSound.SoundEffect(Sound.ENTITY_FIREWORK_SHOOT, 3, 1.6),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_SWIM, 3, 1.6),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_SWIM, 3, 1.8)
        );
        /** 엔티티 타격 */
        public static final DefinedSound HIT_ENTITY = new DefinedSound(
                new DefinedSound.SoundEffect("new.item.trident.thunder", 3, 1.5));
    }
}
