package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 직접 사용하는 액티브 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class ActiveSkill extends AbstractSkill {
    /** 스킬 슬롯 */
    protected final int slot;

    /**
     * 액티브 스킬 인스턴스를 생성한다.
     *
     * @param combatUser      대상 플레이어
     * @param activeSkillInfo 액티브 스킬 정보 객체
     * @param slot            슬롯 번호
     */
    protected ActiveSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo activeSkillInfo, int slot) {
        super(combatUser, activeSkillInfo);
        this.slot = slot;

        TaskUtil.addTask(this, new IntervalTask(i -> {
            onTick();
            return true;
        }, 1));
    }

    /**
     * 매 틱마다 실행할 작업.
     */
    protected void onTick() {
        if (isDurationFinished()) {
            if (isCooldownFinished())
                displayReady(1);
            else {
                long cooldown = CooldownUtil.getCooldown(this, ACTION_COOLDOWN_ID);
                displayCooldown((int) Math.ceil(cooldown / 20.0));
            }
        } else {
            long duration = CooldownUtil.getCooldown(this, SKILL_DURATION_COOLDOWN_ID);
            displayUsing((int) Math.ceil(duration / 20.0));
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownFinished() {
        SoundUtil.playNamedSound(NamedSound.COMBAT_ACTIVE_SKILL_READY, combatUser.getEntity());
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.isGlobalCooldownFinished();
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();

        combatUser.getEntity().getInventory().clear(slot);
    }

    /**
     * 스킬 설명 아이템을 쿨타임 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    final void displayCooldown(int amount) {
        itemStack = skillInfo.getItemStack();
        itemStack.setDurability((short) 15);
        itemStack.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 준비 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    final void displayReady(int amount) {
        itemStack = skillInfo.getItemStack();
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 사용 중인 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    final void displayUsing(int amount) {
        itemStack = skillInfo.getItemStack();
        itemStack.setDurability((short) 5);
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 적용한다.
     *
     * @param amount 아이템 수량
     */
    private void display(int amount) {
        itemStack.setAmount(amount <= 127 ? amount : 1);

        ItemStack slotItem = combatUser.getEntity().getInventory().getItem(slot);
        if (slotItem == null || !slotItem.equals(itemStack))
            combatUser.getEntity().getInventory().setItem(slot, itemStack);
    }
}
