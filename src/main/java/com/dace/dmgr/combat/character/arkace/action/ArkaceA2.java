package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActiveSkill;
import com.dace.dmgr.combat.action.HasCooldown;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.entity.CombatUser;

public class ArkaceA2 extends ActiveSkill implements HasCooldown {
    public static final int COOLDOWN = 12 * 20;
    public static final int HEAL = 350;
    public static final long DURATION = (long) (2.5 * 20);
    private static final ArkaceA2 instance = new ArkaceA2();

    public ArkaceA2() {
        super(2, "생체 회복막",
                "",
                "§6⌛ 지속시간§f동안 동안 회복막을 활성화하여 §a✚ 회복§f합니다.",
                "",
                "§6⌛ §f2.5초",
                "§a✚ §f350",
                "§f⟳ §f12초",
                "",
                "§7§l[3] §f사용");
    }

    public static ArkaceA2 getInstance() {
        return instance;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public void use(CombatUser combatUser, SkillController skillController) {
        combatUser.getEntity().sendMessage("skill 3");
        if (skillController.isCooldownFinished() && !skillController.isUsing())
            skillController.setDuration(DURATION);
        else
            combatUser.getEntity().sendMessage("스킬 쿨타임");
    }
}
