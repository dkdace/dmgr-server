package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.action.*;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.arkace.action.*;
import lombok.Getter;

/**
 * 전투원 - 아케이스 클래스.
 *
 * @see ArkaceWeapon
 * @see ArkaceP1
 * @see ArkaceA1
 * @see ArkaceA2
 * @see ArkaceUlt
 */
public class Arkace extends Character {
    @Getter
    private static final Arkace instance = new Arkace();
    private static final ActionKeyMap keymap = new ActionKeyMap()
            .put(ActionKey.LEFT_CLICK, instance.getActive(2))
            .put(ActionKey.CS_PRE_USE, instance.getWeapon())
            .put(ActionKey.CS_USE, instance.getWeapon())
            .put(ActionKey.SLOT_2, instance.getActive(2))
            .put(ActionKey.SLOT_3, instance.getActive(3))
            .put(ActionKey.SLOT_4, instance.getUltimate())
            .put(ActionKey.DROP, instance.getWeapon())
            .put(ActionKey.SPRINT, instance.getPassive(1));

    private Arkace() {
        super("아케이스", "DVArkace", 1000, 1.0F, 1.0F);
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
