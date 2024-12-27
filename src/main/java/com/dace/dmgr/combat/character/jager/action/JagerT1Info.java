package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.effect.ParticleEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public final class JagerT1Info extends TraitInfo {
    /** 지속시간 (tick) */
    public static final long DURATION = 2 * 20L;
    /** 달리기 불가능 수치 */
    public static final int NO_SPRINT = 60;
    /** 점프 불가능 수치 */
    public static final int NO_JUMP = 80;
    /** 최대치 */
    public static final int MAX = 100;
    @Getter
    private static final JagerT1Info instance = new JagerT1Info();

    private JagerT1Info() {
        super("빙결",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("<5:WALK_SPEED_DECREASE:수치>에 비례하여 <:WALK_SPEED_DECREASE:이동 속도>가 느려지는 상태이상입니다. " +
                                "수치가 " + NO_SPRINT + "을 넘으면 달리기가 불가능해지며, " + NO_JUMP + "을 넘으면 점프가 불가능해집니다.")
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, "최대 " + MAX, ChatColor.DARK_PURPLE)
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, "(빙결)%")
                        .build()
                )
        );
    }

    /**
     * 입자 효과 정보.
     */
    @UtilityClass
    public static final class PARTICLE {
        /** 틱 입자 효과 */
        public static final ParticleEffect TICK_PARTICLE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.CONCRETE, 3)
                        .horizontalSpread(0, 0, 0.5)
                        .build());
    }
}
