package com.dace.dmgr.combat;

import com.dace.dmgr.combat.character.ISkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static com.dace.dmgr.system.HashMapList.combatUserHashMap;

public class SkillController {
    private static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    private final CombatUser combatUser;
    private final ISkill skill;
    private final int slot;
    private final ItemStack itemStack;

    public SkillController(CombatUser combatUser, ISkill skill, int slot) {
        this.combatUser = combatUser;
        this.skill = skill;
        this.slot = slot;
        this.itemStack = new ItemBuilder(skill.getItemStack())
                .setDamage((short) 15)
                .setAmount(skill.getCooldown())
                .build();
        runCooldown();
    }

    public SkillController(CombatUser combatUser, ISkill skill) {
        this(combatUser, skill, -1);
    }

    public ISkill getSkill() {
        return skill;
    }

    private void apply() {
        if (slot != -1)
            combatUser.getEntity().getInventory().setItem(slot, itemStack);
    }

    public void runDuration() {
        runDuration(getSkill().getDuration());
    }

    public void runDuration(float duration) {
        CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, (long) (duration * 20));
        CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, 0);

        if (duration == 0) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, 99999);
            itemStack.setAmount(1);
            itemStack.setDurability((short) 5);
            apply();
        } else {
            SkillController skillController = this;
            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    if (combatUserHashMap.get(combatUser.getEntity()) == null)
                        return false;

                    itemStack.setAmount((int) Math.ceil((float) CooldownManager.getCooldown(skillController, Cooldown.SKILL_DURATION) / 20));
                    itemStack.setDurability((short) 5);
                    apply();

                    if (!isUsing()) {
                        runCooldown();
                        return false;
                    }

                    return true;
                }
            };
        }
    }

    public void addDuration(long duration) {
        if (isUsing())
            CooldownManager.addCooldown(this, Cooldown.SKILL_DURATION, duration);
    }

    public void runCooldown() {
        if (skill.isUltimate()) {
            itemStack.setAmount(1);
            itemStack.setDurability((short) 15);
            itemStack.removeEnchantment(Enchantment.LUCK);
            apply();
        } else
            runCooldown(skill.getCooldown());
    }

    public void runCooldown(int cooldown) {
        CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown * 20L);
        CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, 0);

        if (cooldown == 0)
            reset();
        else {
            SkillController skillController = this;
            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    if (combatUserHashMap.get(combatUser.getEntity()) == null)
                        return false;

                    itemStack.setAmount((int) Math.ceil((float) CooldownManager.getCooldown(skillController, Cooldown.SKILL_COOLDOWN) / 20));
                    itemStack.setDurability((short) 15);
                    apply();

                    if (isCooldownFinished()) {
                        if (!isUsing())
                            reset();
                        return false;
                    }

                    return true;
                }
            };
        }
    }

    public void addCooldown(long cooldown) {
        if (!isCooldownFinished())
            CooldownManager.addCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
    }

    public boolean isCooldownFinished() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_COOLDOWN) == 0;
    }

    public boolean isUsing() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION) > 0;
    }

    private void reset() {
        itemStack.setAmount(1);
        itemStack.setDurability((short) 14);
        apply();
    }

    public void ultimateCharge() {
        itemStack.setAmount(1);
        itemStack.setDurability((short) 10);
        itemStack.addEnchantment(Enchantment.LUCK, 0);
        apply();
    }

    public void clear() {
        CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, 0);
        CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, 0);
    }
}
