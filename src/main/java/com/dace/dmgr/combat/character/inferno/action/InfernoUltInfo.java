package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class InfernoUltInfo extends UltimateSkillInfo<InfernoUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 보호막 */
    public static final int SHIELD = 3000;
    /** 액티브 1번 쿨타임 단축 (tick) */
    public static final int A1_COOLDOWN_DECREMENT = 3 * 20;
    /** 지속시간 (tick) */
    public static final long DURATION = 10 * 20;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    @Getter
    private static final InfernoUltInfo instance = new InfernoUltInfo();

    private InfernoUltInfo() {
        super(InfernoUlt.class, "과부하",
                "",
                "§f▍ 일정 시간동안 몸에 화염 방벽을 둘러 §e" + TextIcon.HEAL + " 보호막",
                "§f▍ 을 얻고 §d점프 부스터§f의 " + TextIcon.COOLDOWN + " §7쿨타임§f을 단축하며,",
                "§f▍ 재장전 없이 사격할 수 있게 됩니다.",
                "§f▍ 보호막이 파괴되면 사용이 종료됩니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§e{0}§f {1}", TextIcon.HEAL, SHIELD),
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN_DECREASE, A1_COOLDOWN_DECREMENT / 20.0),
                "",
                "§7§l[4] §f사용");
    }
}
