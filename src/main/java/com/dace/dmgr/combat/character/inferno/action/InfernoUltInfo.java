package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public final class InfernoUltInfo extends UltimateSkillInfo<InfernoUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 보호막 */
    public static final int SHIELD = 3000;
    /** 액티브 1번 쿨타임 단축 (tick) */
    public static final long A1_COOLDOWN_DECREMENT = 3 * 20L;
    /** 지속시간 (tick) */
    public static final long DURATION = 10 * 20L;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    @Getter
    private static final InfernoUltInfo instance = new InfernoUltInfo();

    private InfernoUltInfo() {
        super(InfernoUlt.class, "과부하",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 몸에 화염 방벽을 둘러 <e:HEAL:보호막>을 얻고 <d::점프 부스터>의 <7:COOLDOWN:쿨타임>을 단축하며, 재장전 없이 사격할 수 있게 됩니다. " +
                                "보호막이 파괴되면 사용이 종료됩니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.HEAL, ChatColor.YELLOW, SHIELD)
                        .addValueInfo(TextIcon.COOLDOWN_DECREASE, Format.TIME, -A1_COOLDOWN_DECREMENT / 20.0)
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
                SoundEffect.SoundInfo.builder("new.block.respawn_anchor.ambient").volume(3).pitch(1.2, 1.7).build());
        /** 틱 효과음 */
        public static final SoundEffect TICK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_AMBIENT).volume(2).pitch(0.9).pitchVariance(0.1).build());
        /** 피격 */
        public static final SoundEffect DAMAGE = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_POP).volume(0.3).pitch(1.2).pitchVariance(0.1).build());
        /** 파괴 */
        public static final SoundEffect DEATH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(3).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_LAVA_EXTINGUISH).volume(3).pitch(0.5).build(),
                SoundEffect.SoundInfo.builder("new.block.conduit.deactivate").volume(3).pitch(0.8).build()
        );
    }
}
