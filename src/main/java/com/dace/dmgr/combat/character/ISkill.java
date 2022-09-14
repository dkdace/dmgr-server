package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.SkillTrigger;
import org.bukkit.inventory.ItemStack;

public interface ISkill {
    String getName();

    ItemStack getItemStack();

    SkillTrigger getSkillTrigger();

    void setSkillTrigger(SkillTrigger skillTrigger);

    default boolean isUltimate() {
        return false;
    }

    void setUltimate(boolean ultimate);

    default float getDuration() {
        return 0;
    }

    default int getCooldown() {
        return 0;
    }

    default int getCost() {
        return 0;
    }
}
