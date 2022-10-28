package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.action.*;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.arkace.action.*;

public class Arkace extends Character {
    private static final Arkace instance = new Arkace();
    private static final ActionKeyMap keymap = new ActionKeyMap()
            .set(ActionKey.SPRINT, instance.getPassive(1))
            .set(ActionKey.LEFT_CLICK, instance.getActive(2))
            .set(ActionKey.CS_PRE_USE, instance.getWeapon())
            .set(ActionKey.CS_USE, instance.getWeapon())
            .set(ActionKey.SLOT_2, instance.getActive(2))
            .set(ActionKey.SLOT_3, instance.getActive(3))
            .set(ActionKey.SLOT_4, instance.getUltimate());

    private Arkace() {
        super("아케이스", "DVArkace", 1000, 1.0F, 1.0F);
    }

    public static Arkace getInstance() {
        return instance;
    }

    @Override
    public ActionKeyMap getActionKeyMap() {
        return keymap;
    }

    @Override
    public Weapon getWeapon() {
        return ArkaceWeapon.getInstance();
    }

    @Override
    public PassiveSkill getPassive(int number) {
        switch (number) {
            case 1:
                return ArkaceP1.getInstance();
            default:
                return null;
        }
    }

    @Override
    public ActiveSkill getActive(int number) {
        switch (number) {
            case 2:
                return ArkaceA1.getInstance();
            case 3:
                return ArkaceA2.getInstance();
            case 4:
                return ArkaceUlt.getInstance();
            default:
                return null;
        }
    }

    @Override
    public UltimateSkill getUltimate() {
        return ArkaceUlt.getInstance();
    }
}
