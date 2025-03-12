package com.dace.dmgr.combat.combatant.silia.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.combatant.silia.Silia;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class SiliaA3Info extends ActiveSkillInfo<SiliaA3> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(1);
    /** 강제 해제 쿨타임 */
    public static final Timespan COOLDOWN_FORCE = Timespan.ofSeconds(5);
    /** 강제 해제 피해량 비율 */
    public static final double CANCEL_DAMAGE_RATIO = 0.1;
    /** 이동속도 증가량 */
    public static final int SPEED = 20;
    /** 최대 지속시간 */
    public static final Timespan MAX_DURATION = Timespan.ofSeconds(10);
    /** 일격 활성화 시간 */
    public static final Timespan ACTIVATE_DURATION = Timespan.ofSeconds(2);
    @Getter
    private static final SiliaA3Info instance = new SiliaA3Info();

    private SiliaA3Info() {
        super(SiliaA3.class, "폭풍전야",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 <:WALK_SPEED_INCREASE:이동 속도>가 빨라지고 발소리 및 모든 행동의 소음이 감소합니다. " +
                                "일정량의 피해를 입으면 해제되며, " + ACTIVATE_DURATION.toSeconds() + "초동안 유지하면 다음 기본 공격 시 <d::일격>을 날립니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN_FORCE.toSeconds())
                        .addValueInfo(TextIcon.DURATION, Format.TIME_WITH_MAX_TIME, MAX_DURATION.toSeconds(), MAX_DURATION.toSeconds())
                        .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                        .addValueInfo(TextIcon.DAMAGE, (int) (Silia.getInstance().getHealth() * CANCEL_DAMAGE_RATIO) + " (강제 해제 피해량)")
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
                        .build(),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_3)
                                .build()
                        )
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
                SoundEffect.SoundInfo.builder(Sound.ENTITY_LLAMA_SWAG).volume(0.2).pitch(1).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(0.15).pitch(1.5).build()
        );
        /** 해제 */
        public static final SoundEffect DISABLE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_LLAMA_SWAG).volume(0.2).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(0.15).pitch(1.7).build()
        );
        /** 일격 활성화 */
        public static final SoundEffect ACTIVATE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.item.trident.return").volume(1).pitch(1.2).build());
    }
}
