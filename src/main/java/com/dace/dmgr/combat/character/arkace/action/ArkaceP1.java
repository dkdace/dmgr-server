package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.HasDuration;
import com.dace.dmgr.combat.action.PassiveSkill;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;
import lombok.Getter;

public class ArkaceP1 extends PassiveSkill implements HasDuration {
    /** 이동속도 증가량 */
    public static final int SPRINT_SPEED = 30;
    @Getter
    private static final ArkaceP1 instance = new ArkaceP1();

    public ArkaceP1() {
        super(1, "강화된 신체",
                "",
                "§f달리기의 §b" + TextIcon.WALK_SPEED_INCREASE + " 속도§f가 빨라집니다.",
                "",
                "§b" + TextIcon.WALK_SPEED_INCREASE + "§f 30%");
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public long getDuration() {
        return -1;
    }

    @Override
    public void use(CombatUser combatUser, SkillController skillController, ActionKey actionKey) {
        if (!skillController.isUsing()) {
            skillController.setDuration();
            combatUser.addSpeedIncrement(SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability((short) (combatUser.getCharacter().getWeapon().getItemStack().getDurability() + 1000));
        } else {
            skillController.setDuration(0);
            combatUser.addSpeedIncrement(-SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability(combatUser.getCharacter().getWeapon().getItemStack().getDurability());
        }
    }
}
