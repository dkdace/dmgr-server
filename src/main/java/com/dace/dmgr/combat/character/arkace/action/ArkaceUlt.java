package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.HasDuration;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.action.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;
import lombok.Getter;

public class ArkaceUlt extends UltimateSkill implements HasDuration {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7000;
    /** 지속시간 */
    public static final long DURATION = (long) 12 * 20;
    @Getter
    private static final ArkaceUlt instance = new ArkaceUlt();

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

    @Override
    public int getCost() {
        return COST;
    }

    @Override
    public long getDuration() {
        return DURATION;
    }

    @Override
    public void use(CombatUser combatUser, SkillController skillController, ActionKey actionKey) {
        if (!skillController.isUsing()) {
            skillController.use();
            combatUser.getWeaponController().setRemainingAmmo(ArkaceWeapon.CAPACITY);
        }
    }
}
