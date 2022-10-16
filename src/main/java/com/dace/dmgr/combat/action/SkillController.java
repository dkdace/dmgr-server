package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class SkillController {
    private final CombatUser combatUser;
    private final Skill skill;
    private final int slot;
    private ItemStack itemStack;

    public SkillController(CombatUser combatUser, Skill skill, int slot) {
        this.combatUser = combatUser;
        this.skill = skill;
        this.itemStack = skill.getItemStack().clone();
        this.slot = slot;
        if (skill instanceof UltimateSkill)
            setCooldown(-1);
        else
            setCooldown();
    }

    public SkillController(CombatUser combatUser, Skill skill) {
        this(combatUser, skill, -1);
    }

    public void apply() {
        if (slot != -1)
            combatUser.getEntity().getInventory().setItem(slot, itemStack);
    }

    private void runCooldown() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatUserMap.get(combatUser.getEntity()) == null)
                    return false;

                long cooldown = CooldownManager.getCooldown(SkillController.this, Cooldown.SKILL_COOLDOWN);
                setItemCooldown((int) Math.ceil((float) cooldown / 20));

                if (isCooldownFinished()) {
                    if (!isUsing())
                        setItemReady(1);
                    return false;
                }

                return true;
            }
        };
    }

    private void runDuration() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatUserMap.get(combatUser.getEntity()) == null)
                    return false;

                long duration = CooldownManager.getCooldown(SkillController.this, Cooldown.SKILL_DURATION);
                setItemDuration((int) Math.ceil((float) duration / 20));

                if (!isUsing()) {
                    setCooldown();
                    return false;
                }

                return true;
            }
        };
    }

    public void setCooldown(long cooldown) {
        if (cooldown == 0)
            setItemReady(1);
        else if (cooldown == -1) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, -1);
            setItemCooldown(1);
        } else {
            if (isCooldownFinished()) {
                CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
                runCooldown();
            } else
                CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
        }
        CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, 0);
    }

    public void setCooldown() {
        if (skill instanceof HasCooldown)
            setCooldown(((HasCooldown) skill).getCooldown());
    }

    public void addCooldown(long cooldown) {
        if (!isCooldownFinished())
            CooldownManager.addCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
    }

    public void setDuration(long duration) {
        if (duration == -1) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, -1);
            setItemDuration(1);
        } else {
            if (!isUsing()) {
                CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
                runDuration();
            } else
                CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
        }
        CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, 0);
    }

    public void setDuration() {
        if (skill instanceof HasDuration)
            setDuration(((HasDuration) skill).getDuration());
    }

    public void addDuration(long duration) {
        if (isUsing())
            CooldownManager.addCooldown(this, Cooldown.SKILL_DURATION, duration);
    }

    public boolean isCooldownFinished() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_COOLDOWN) == 0;
    }

    public boolean isCharged() {
        return combatUser.getUlt() == 1;
    }

    public boolean isUsing() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION) > 0;
    }

    private void setItemCooldown(int amount) {
        itemStack.setAmount(amount);
        itemStack.setDurability((short) 15);
        itemStack.removeEnchantment(Enchantment.LUCK);
        apply();
    }

    private void setItemDuration(int amount) {
        itemStack.setAmount(amount);
        itemStack.setDurability((short) 5);
        apply();
    }

    private void setItemReady(int amount) {
        itemStack = skill.getItemStack().clone();
        itemStack.setAmount(amount);
        apply();
        if (skill instanceof UltimateSkill)
            SoundUtil.play(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2F, combatUser.getEntity());
        else if (skill instanceof ActiveSkill)
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 2F, combatUser.getEntity());
    }

    public void reset() {
        CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, 0);
        CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, 0);
    }
}
