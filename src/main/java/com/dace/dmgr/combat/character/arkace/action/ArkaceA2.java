package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActiveSkill;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.entity.CombatUser;

public class ArkaceA2 extends ActiveSkill {
    public static final long COOLDOWN = 7 * 20;
    public static final int DAMAGE_DIRECT = 50;
    public static final int DAMAGE_EXPLODE = 100;
    public static final float RADIUS = 3.5F;
    private static final ArkaceA2 instance = new ArkaceA2();

    public ArkaceA2() {
        super(2, "다이아코어 미사일",
                "",
                "§f소형 미사일을 3회 연속으로 발사하여 §c⚔ 광역 피해",
                "§f를 입힙니다.",
                "",
                "§c⚔ §f직격 50 + 폭발 100",
                "§c✸ §f3.5m",
                "§f⟳ §f7초",
                "",
                "§7§l[2] [좌클릭] §f사용");
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
        combatUser.getEntity().sendMessage("skill 2");
        if (skillController.isCooldownFinished())
            skillController.setCooldown();
        else
            combatUser.getEntity().sendMessage("스킬 쿨타임");
    }
}
