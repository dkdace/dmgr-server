package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.HasDuration;
import com.dace.dmgr.combat.action.PassiveSkill;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.entity.CombatUser;

public class ArkaceP1 extends PassiveSkill implements HasDuration {
    public static final int SPRINT_SPEED = 30;
    private static final ArkaceP1 instance = new ArkaceP1();

    public ArkaceP1() {
        super(1, "강화된 신체",
                "",
                "§f달리기의 §e⬆§b➠ 속도§f가 빨라집니다.",
                "",
                "§e⬆§b➠ §f30%");
    }

    public static ArkaceP1 getInstance() {
        return instance;
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
