package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import org.bukkit.Material;

public abstract class Skill extends Action {
    private static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    private static final String PREFIX = "§e§l[스킬] §c";
    private final int number;

    public Skill(int number, String name, String... lore) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 15)
                .setLore(lore)
                .build());
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public abstract long getCooldown();

    public abstract void use(CombatUser combatUser, SkillController skillController);
}
