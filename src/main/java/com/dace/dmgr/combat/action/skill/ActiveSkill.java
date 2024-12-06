package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.SoundEffect;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.function.LongConsumer;

/**
 * 직접 사용하는 액티브 스킬의 상태를 관리하는 클래스.
 */
public abstract class ActiveSkill extends AbstractSkill {
    /** 스킬 준비 효과음 */
    static final SoundEffect READY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(0.2).pitch(2).build());

    /** 스킬 슬롯 */
    private final int slot;
    /** 원본 스킬 아이템 객체 */
    private final ItemStack originalItemStack;
    /** 스킬 아이템 객체 */
    private ItemStack itemStack;

    /**
     * 액티브 스킬 인스턴스를 생성한다.
     *
     * @param combatUser      대상 플레이어
     * @param activeSkillInfo 액티브 스킬 정보 객체
     * @param slot            슬롯 번호. 0~8 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected ActiveSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo<? extends ActiveSkill> activeSkillInfo, int slot) {
        super(combatUser);
        if (slot < 0 || slot > 8)
            throw new IllegalArgumentException("'slot'이 0에서 8 사이여야 함");

        this.originalItemStack = activeSkillInfo.getStaticItem().getItemStack();
        this.itemStack = originalItemStack.clone();
        this.slot = slot;

        TaskUtil.addTask(this, new IntervalTask((LongConsumer) i -> onTick(), 1));
    }

    /**
     * 매 틱마다 실행할 작업.
     */
    protected void onTick() {
        if (isDurationFinished()) {
            if (isCooldownFinished())
                displayReady(1);
            else
                displayCooldown((int) Math.ceil(getCooldown() / 20.0));
        } else
            displayUsing((int) Math.ceil(getDuration() / 20.0));
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownFinished() {
        READY_SOUND.play(combatUser.getEntity());
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.isGlobalCooldownFinished();
    }

    @Override
    @MustBeInvokedByOverriders
    public void reset() {
        super.reset();
        setDuration(0);
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
