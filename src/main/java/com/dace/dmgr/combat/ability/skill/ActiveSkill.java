package com.dace.dmgr.combat.ability.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.handler.SlotHandler;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.function.LongConsumer;

/**
 * 인벤토리 슬롯에서 사용하는 액티브 스킬의 상태를 관리하는 클래스.
 */
public abstract class ActiveSkill extends AbstractSkill implements SlotHandler {
    /** 스킬 준비 효과음 */
    static final SoundEffect READY_SOUND = SoundEffect.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(0.2).pitch(2).build();

    /** 스킬 인벤토리 슬롯 */
    private final int slot;
    /** 원본 스킬 아이템 인스턴스 */
    private final ItemStack originalItemStack;
    /** 스킬 아이템 인스턴스 */
    private ItemStack itemStack;

    /**
     * 액티브 스킬 인스턴스를 생성한다.
     *
     * @param combatUser      사용자 플레이어
     * @param activeSkillInfo 액티브 스킬 정보 인스턴스
     * @param defaultCooldown 기본 쿨타임
     * @param defaultDuration 기본 지속시간
     */
    protected ActiveSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo<?> activeSkillInfo, @NonNull Timespan defaultCooldown,
                          @NonNull Timespan defaultDuration) {
        super(combatUser, activeSkillInfo, defaultCooldown, defaultDuration);

        this.originalItemStack = activeSkillInfo.getDefinedItem().getItemStack();
        this.itemStack = originalItemStack.clone();
        this.slot = Integer.parseInt(getSlot().toActionKey().toString()) - 1;

        addTask(new IntervalTask((LongConsumer) i -> onTick(), 1));
        addOnRemove(() -> combatUser.getEntity().getInventory().clear(slot));
    }

    @Override
    @NonNull
    protected ChatColor getDisplayNameColor() {
        return ChatColor.RED;
    }

    /**
     * 매 틱마다 실행할 작업.
     */
    void onTick() {
        if (isDurationFinished()) {
            if (isCooldownFinished())
                displayReady(1);
            else
                displayCooldown((int) Math.ceil(getCooldown().toSeconds()));
        } else
            displayUsing((int) Math.ceil(getDuration().toSeconds()));
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownFinished() {
        READY_SOUND.play(combatUser.getEntity());
    }

    @Override
    @MustBeInvokedByOverriders
    protected boolean canUse() {
        return super.canUse() && combatUser.isGlobalCooldownFinished();
    }

    /**
     * 스킬 설명 아이템을 쿨타임 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    final void displayCooldown(int amount) {
        itemStack = originalItemStack.clone();
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
        itemStack = originalItemStack.clone();
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 사용 중인 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    final void displayUsing(int amount) {
        itemStack = originalItemStack.clone();
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
