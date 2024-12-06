package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class SiliaT2Info extends TraitInfo {
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 350;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3.7;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 5;
    @Getter
    private static final SiliaT2Info instance = new SiliaT2Info();

    private SiliaT2Info() {
        super("일격",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("특수 공격으로, 칼을 휘둘러 근거리에 <:DAMAGE:광역 피해>를 입히고 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
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
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_SWEEP).volume(1.5).pitch(1, 1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_ATTACK).volume(1.5).pitch(0.8, 1).build(),
                SoundEffect.SoundInfo.builder("random.swordhit").volume(1.5).pitch(0.7, 0.9).build()
        );
    }
}
