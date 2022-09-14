package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.SkillTrigger;
import com.dace.dmgr.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Skill implements ISkill {
    private static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    private static final String PREFIX = "§e§l[스킬] §c";

    private final String name;
    private final ItemStack itemStack;
    protected SkillTrigger skillTrigger;
    private boolean ultimate;

    public Skill(String name, String... lore) {
        this.name = name;
        this.itemStack = new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setLore(lore)
                .build();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public SkillTrigger getSkillTrigger() {
        return skillTrigger;
    }

    @Override
    public void setSkillTrigger(SkillTrigger skillTrigger) {
        this.skillTrigger = skillTrigger;
    }

    @Override
    public boolean isUltimate() {
        return ultimate;
    }

    @Override
    public void setUltimate(boolean ultimate) {
        this.ultimate = ultimate;
    }
}
