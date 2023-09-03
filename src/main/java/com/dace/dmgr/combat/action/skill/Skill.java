package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;

/**
 * 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class Skill extends Action {
    /** 스킬 슬롯 */
    protected final int slot;
    /** 번호 */
    protected final int number;

    /**
     * 스킬 인스턴스를 생성한다.
     *
     * @param number     번호
     * @param combatUser 대상 플레이어
     * @param skillInfo  스킬 정보 객체
     * @param slot       슬롯 번호
     */
    protected Skill(int number, CombatUser combatUser, SkillInfo skillInfo, int slot) {
        super(combatUser, skillInfo);
        this.number = number;
        this.slot = slot;
        setCooldown(getDefaultCooldown());
    }

    @Override
    protected void onCooldownSet() {
        if (!isDurationFinished())
            setDuration(0);
    }

    @Override
    protected void onCooldownTick() {
        long cooldown = CooldownManager.getCooldown(this, Cooldown.SKILL_COOLDOWN);

        displayCooldown((int) Math.ceil((float) cooldown / 20));
    }

    @Override
    protected void onCooldownFinished() {
        displayReady(1);
        playCooldownFinishSound();
    }

    /**
     * 쿨타임이 끝났을 때 효과음을 재생한다.
     */
    protected void playCooldownFinishSound() {
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 2F, combatUser.getEntity());
    }

    /**
     * 스킬의 기본 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    public abstract long getDefaultDuration();

    /**
     * 스킬의 남은 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    public final long getDuration() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION);
    }

    /**
     * 스킬의 지속시간을 설정한다.
     *
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    public final void setDuration(long duration) {
        if (isDurationFinished()) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
            runDuration();
        } else
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
    }

    /**
     * 스킬의 지속시간을 기본 지속시간으로 설정한다.
     *
     * @see Skill#getDefaultDuration()
     */
    public final void setDuration() {
        setDuration(getDefaultDuration());
    }

    /**
     * 스킬의 지속시간을 증가시킨다.
     *
     * @param duration 추가할 지속시간 (tick)
     */
    public final void addDuration(long duration) {
        setDuration(getDuration() + duration);
    }

    /**
     * 스킬의 지속시간 스케쥴러를 실행한다.
     */
    protected final void runDuration() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;

                onDurationTick();

                if (isDurationFinished()) {
                    onDurationFinished();
                    return false;
                }

                return true;
            }
        };
    }

    /**
     * 지속시간이 진행할 때 (매 tick마다) 실행할 작업.
     */
    protected void onDurationTick() {
        long duration = CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION);

        displayUsing((int) Math.ceil((float) duration / 20));
    }

    /**
     * 지속시간이 끝났을 때 실행할 작업.
     */
    protected void onDurationFinished() {
        if (isCooldownFinished())
            setCooldown();
    }

    /**
     * 스킬의 지속시간이 끝났는 지 확인한다.
     *
     * @return 지속시간 종료 여부
     */
    public final boolean isDurationFinished() {
        return getDuration() == 0;
    }

    /**
     * 스킬 전역 쿨타임이 끝났는 지 확인한다.
     *
     * @return 전역 쿨타임 종료 여부
     */
    public final boolean isGlobalCooldownFinished() {
        return combatUser.getEntity().getCooldown(SkillInfo.MATERIAL) == 0;
    }

    /**
     * 스킬 전역 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    public final void setGlobalCooldown(int cooldown) {
        if (cooldown == -1)
            cooldown = 9999;
        combatUser.getEntity().setCooldown(SkillInfo.MATERIAL, cooldown);
    }

    /**
     * 스킬 설명 아이템을 쿨타임 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    protected final void displayCooldown(int amount) {
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
    protected final void displayReady(int amount) {
        itemStack = actionInfo.getItemStack().clone();
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 사용 중인 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    protected final void displayUsing(int amount) {
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
