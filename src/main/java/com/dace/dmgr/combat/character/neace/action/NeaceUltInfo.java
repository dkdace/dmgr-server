package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class NeaceUltInfo extends UltimateSkillInfo<NeaceUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.8 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 지속시간 (tick) */
    public static final long DURATION = 12 * 20L;
    @Getter
    private static final NeaceUltInfo instance = new NeaceUltInfo();

    private NeaceUltInfo() {
        super(NeaceUlt.class, "치유의 성역",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("체력을 최대치로 즉시 <:HEAL:회복>하고 일정 시간동안 여러 대상을 자동으로 치유합니다. " +
                                "사용 중에는 공격할 수 없습니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
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
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder("new.block.respawn_anchor.charge").volume(2).pitch(0.7).build()
        );
        /** 사용 준비 */
        public static final SoundEffect USE_READY = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON).volume(3).pitch(1.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON).volume(3).pitch(1.1).build()
        );
    }
}
