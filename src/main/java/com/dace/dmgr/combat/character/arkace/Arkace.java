package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.CombatUser;
import com.dace.dmgr.combat.Weapon;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.GunCharacter;
import com.dace.dmgr.combat.character.ICharacter;

public class Arkace extends Character implements ICharacter, GunCharacter {
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super("아케이스", Weapon.ARKACE, 1000, 1.0F, 1.0F, "DVArkace");
    }

    public static Arkace getInstance() {
        return instance;
    }

    @Override
    public void useWeaponShoot(CombatUser combatUser) {
        combatUser.getPlayer().sendMessage("test!");
    }

    @Override
    public void useWeaponLeft(CombatUser combatUser) {

    }

    @Override
    public void useWeaponRight(CombatUser combatUser) {

    }

    @Override
    public void useSkill1(CombatUser combatUser) {

    }

    @Override
    public void useSkill2(CombatUser combatUser) {

    }

    @Override
    public void useSkill3(CombatUser combatUser) {

    }

    @Override
    public void useSkill4(CombatUser combatUser) {

    }

}
