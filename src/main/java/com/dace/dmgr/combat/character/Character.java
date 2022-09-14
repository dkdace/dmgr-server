package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.SkillController;
import com.dace.dmgr.combat.entity.CombatUser;

public abstract class Character implements ICharacter {
    private final String name;
    private final ICharacterStats stats;
    private final String skinName;

    public Character(String name, ICharacterStats stats, String skinName) {
        this.name = name;
        this.stats = stats;
        this.skinName = skinName;
    }

    @Override
    public ICharacterStats getCharacterStats() {
        return stats;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSkinName() {
        return skinName;
    }

    @Override
    public void usePassive(int number, CombatUser combatUser, SkillController skillController) {
        switch (number) {
            case 1:
                usePassive1(combatUser, skillController);
                break;
            case 2:
                usePassive2(combatUser, skillController);
                break;
            case 3:
                usePassive3(combatUser, skillController);
                break;
        }
    }

    @Override
    public void useActive(int number, CombatUser combatUser, SkillController skillController) {
        switch (number) {
            case 1:
                useActive1(combatUser, skillController);
                break;
            case 2:
                useActive2(combatUser, skillController);
                break;
            case 3:
                useActive3(combatUser, skillController);
                break;
            case 4:
                useUltimate(combatUser, skillController);
                break;
        }
    }

    protected void usePassive1(CombatUser combatUser, SkillController skillController) {
    }

    ;

    protected void usePassive2(CombatUser combatUser, SkillController skillController) {
    }

    ;

    protected void usePassive3(CombatUser combatUser, SkillController skillController) {
    }

    ;

    protected void useActive1(CombatUser combatUser, SkillController skillController) {
    }

    ;

    protected void useActive2(CombatUser combatUser, SkillController skillController) {
    }

    ;

    protected void useActive3(CombatUser combatUser, SkillController skillController) {
    }

    ;

    protected void useUltimate(CombatUser combatUser, SkillController skillController) {
    }

    ;
}
