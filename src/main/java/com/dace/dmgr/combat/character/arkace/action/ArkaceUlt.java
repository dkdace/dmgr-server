package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.action.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;

public class ArkaceUlt extends UltimateSkill {
    private static final ArkaceUlt instance = new ArkaceUlt();
    public static int COST = 7000;
    public static long DURATION = (long) 12 * 20;

    public ArkaceUlt() {
        super("인피니버스터",
                "",
                "§6" + TextIcon.DURATION + " 지속시간§f동안 기본 무기에 장탄수 무한, 탄퍼짐 제거, 거리별",
                "§f피해 감소 제거 효과가 적용됩니다.",
                "",
                "§6" + TextIcon.DURATION + "§f 12초",
                "§f" + TextIcon.ULTIMATE + "§f 7000",
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
    public void use(CombatUser combatUser, SkillController skillController, ActionKey actionKey) {
        if (!skillController.isUsing()) {
            skillController.setDuration(DURATION);
            combatUser.getWeaponController().setRemainingAmmo(ArkaceWeapon.CAPACITY);
        }
    }
}
