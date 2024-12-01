package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class VellionUltInfo extends UltimateSkillInfo<VellionUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = 20;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 이동 속도 감소량 */
    public static final int SLOW = 50;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);
    /** 피해량 비율 */
    public static final double DAMAGE_RATIO = 0.5;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = 20;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    @Getter
    private static final VellionUltInfo instance = new VellionUltInfo();

    private VellionUltInfo() {
        super(VellionUlt.class, "나락의 결계",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 무적이 되어 주변 적의 <:WALK_SPEED_DECREASE:이동 속도>를 느리게 하고 <:GROUNDING:고정>시킵니다. " +
                                "일정 시간 후 결계가 폭발하여 탈출하지 못한 적은 <:DAMAGE:광역 피해>를 입고 <:STUN:기절>합니다. " +
                                "사용 중에는 움직일 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.PERCENT, SLOW)
                        .addValueInfo(TextIcon.DAMAGE, "적 최대 체력의 {0}%", (int) (100 * DAMAGE_RATIO))
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION / 20.0)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
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
                new DefinedSound.SoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.8),
                new DefinedSound.SoundEffect(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.8),
                new DefinedSound.SoundEffect(Sound.ENTITY_GUARDIAN_HURT, 2, 1.8)
        );
        /** 사용 준비 */
        public static final DefinedSound USE_READY = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS, 3, 0.7),
                new DefinedSound.SoundEffect(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS, 3, 0.8),
                new DefinedSound.SoundEffect(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK, 3, 0.85),
                new DefinedSound.SoundEffect(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON, 3, 0.7)
        );
        /** 폭발 */
        public static final DefinedSound EXPLODE = new DefinedSound(
                new DefinedSound.SoundEffect("new.block.conduit.deactivate", 3, 0.6),
                new DefinedSound.SoundEffect("new.block.respawn_anchor.deplete", 3, 0.6),
                new DefinedSound.SoundEffect("new.block.respawn_anchor.deplete", 3, 0.8)
        );
    }
}
