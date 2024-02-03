package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
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
     * @param number          번호
     * @param combatUser      대상 플레이어
     * @param activeSkillInfo 액티브 스킬 정보 객체
     * @param slot            슬롯 번호
     */
    protected ActiveSkill(int number, @NonNull CombatUser combatUser, @NonNull ActiveSkillInfo activeSkillInfo, int slot) {
        super(number, combatUser, activeSkillInfo);
        this.slot = slot;
    }

    @Override
    protected void onCooldownTick() {
        long cooldown = CooldownUtil.getCooldown(this, Cooldown.SKILL_COOLDOWN);

        displayCooldown((int) Math.ceil(cooldown / 20.0));
    }

    @Override
    protected void onCooldownFinished() {
        displayReady(1);
        playCooldownFinishSound();
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.isGlobalCooldownFinished();
    }

    @Override
    protected void onDurationTick() {
        long duration = CooldownUtil.getCooldown(this, Cooldown.SKILL_DURATION);

        displayUsing((int) Math.ceil(duration / 20.0));
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();

        combatUser.getEntity().getInventory().clear(slot);
    }

    /**
     * 쿨타임이 끝났을 때 효과음을 재생한다.
     */
    protected void playCooldownFinishSound() {
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, combatUser.getEntity(), 0.2, 2);
    }

    /**
     * 스킬 설명 아이템을 쿨타임 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    final void displayCooldown(int amount) {
        itemStack = actionInfo.getItemStack().clone();
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
        itemStack = actionInfo.getItemStack().clone();
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 사용 중인 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    final void displayUsing(int amount) {
        itemStack = actionInfo.getItemStack().clone();
        itemStack.setDurability((short) 5);
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 적용한다.
     *
     * @param amount 아이템 수량
     */
    private void display(int amount) {
        if (slot == -1)
            return;

        itemStack.setAmount(amount <= 127 ? amount : 1);
        combatUser.getEntity().getInventory().setItem(slot, itemStack);
    }
}
