package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.function.LongConsumer;

/**
 * 인벤토리 슬롯에서 사용하는 액티브 스킬의 상태를 관리하는 클래스.
 */
public abstract class ActiveSkill extends AbstractSkill {
    /** 스킬 준비 효과음 */
    static final SoundEffect READY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(0.2).pitch(2).build());

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
     * @param slot            슬롯 번호. 0~4 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected ActiveSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo<?> activeSkillInfo, @NonNull Timespan defaultCooldown,
                          @NonNull Timespan defaultDuration, int slot) {
        super(combatUser, activeSkillInfo, defaultCooldown, defaultDuration);
        Validate.inclusiveBetween(0, 4, slot, "4 >= slot >= 0 (%d)", slot);

        this.originalItemStack = activeSkillInfo.getDefinedItem().getItemStack();
        this.itemStack = originalItemStack.clone();
        this.slot = slot;

        addTask(new IntervalTask((LongConsumer) i -> onTick(), 1));
        addOnRemove(() -> combatUser.getEntity().getInventory().clear(slot));
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> getSkillInfo() {
        return (ActiveSkillInfo<?>) super.getSkillInfo();
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.isGlobalCooldownFinished();
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
