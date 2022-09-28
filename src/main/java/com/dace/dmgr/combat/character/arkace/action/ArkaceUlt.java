package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.action.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;

public class ArkaceUlt extends UltimateSkill {
    private static final ArkaceUlt instance = new ArkaceUlt();
    public static int COST = 7000;
    public static long DURATION = (long) 12 * 20;

    public ArkaceUlt() {
        super("인피니버스터",
                "",
                "§6⌛ 지속시간§f동안 기본 무기에 장탄수 무한, 탄퍼짐 제거, 거리별",
                "§f피해 감소 제거 효과가 적용됩니다.",
                "",
                "§6⌛ §f12초",
                "§f⚡ §f7000",
                "",
                "§7§l[4] §f사용");
    }

    public static ArkaceUlt getInstance() {
        return instance;
    }

    @Override
    public int getCost() {
        return COST;
    }

    @Override
    public void use(CombatUser combatUser, SkillController skillController) {
        combatUser.getEntity().sendMessage("ultimate");
        if (skillController.isCharged() && !skillController.isUsing())
            skillController.setDuration(DURATION);
        else
            combatUser.getEntity().sendMessage("궁극기 미충전");
    }
}
