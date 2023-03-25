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

/**
 * 스킬 상태를 관리하는 컨트롤러 클래스.
 *
 * @see Skill
 */
public class SkillController {
    /** 플레이어 객체 */
    private final CombatUser combatUser;
    /** 스킬 객체 */
    private final Skill skill;
    /** 스킬 슬롯 */
    private final int slot;
    /** 스킬 설명 아이템 */
    private ItemStack itemStack;

    /**
     * 스킬 컨트롤러 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param skill      스킬 객체
     * @param slot       슬롯
     */
    public SkillController(CombatUser combatUser, Skill skill, int slot) {
        this.combatUser = combatUser;
        this.skill = skill;
        this.itemStack = skill.getItemStack().clone();
        this.slot = slot;
        setCooldown();
    }

    /**
     * 플레이어의 인벤토리에 스킬 설명 아이템을 적용한다.
     */
    public void apply() {
        if (slot != -1)
            combatUser.getEntity().getInventory().setItem(slot, itemStack);
    }

    /**
     * 스킬의 쿨타임을 실행한다.
     */
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

    /**
     * 스킬의 지속시간을 실행한다.
     */
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

    /**
     * 스킬의 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    public void setCooldown(long cooldown) {
        if (cooldown == 0) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, 0);
            setItemReady(1);
        } else if (cooldown == -1) {
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

    /**
     * 스킬의 쿨타임을 스킬 정보에 설정된 기본 쿨타임으로 설정한다.
     *
     * @see Skill#getCooldown()
     */
    public void setCooldown() {
        setCooldown(skill.getCooldown());
    }

    /**
     * 스킬의 쿨타임을 증가시킨다.
     *
     * @param cooldown 추가할 쿨타임 (tick)
     */
    public void addCooldown(long cooldown) {
        if (!isCooldownFinished())
            CooldownManager.addCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
    }

    /**
     * 스킬의 지속시간을 설정한다.
     *
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
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

    /**
     * 스킬의 지속시간을 스킬 정보에 설정된 기본 지속시간으로 설정한다.
     *
     * <p>스킬이 {@link HasDuration}을 상속받는 클래스여야 한다.</p>
     *
     * @see HasDuration
     */
    public void setDuration() {
        if (skill instanceof HasDuration)
            setDuration(((HasDuration) skill).getDuration());
    }

    /**
     * 스킬의 지속시간을 증가시킨다.
     *
     * @param duration 추가할 지속시간 (tick)
     */
    public void addDuration(long duration) {
        if (isUsing())
            CooldownManager.addCooldown(this, Cooldown.SKILL_DURATION, duration);
    }

    /**
     * 스킬의 쿨타임이 끝났는 지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    public boolean isCooldownFinished() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_COOLDOWN) == 0;
    }

    /**
     * 스킬이 사용 중인 상태인지 확인한다.
     *
     * @return 사용 중 여부
     */
    public boolean isUsing() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION) > 0;
    }

    /**
     * 스킬 설명 아이템에 쿨타임을 적용한다.
     *
     * @param amount 아이템 수량
     */
    private void setItemCooldown(int amount) {
        itemStack.setAmount(amount);
        itemStack.setDurability((short) 15);
        itemStack.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
        apply();
    }

    /**
     * 스킬 설명 아이템에 지속시간을 적용한다.
     *
     * @param amount 아이템 수량
     */
    private void setItemDuration(int amount) {
        itemStack.setAmount(amount);
        itemStack.setDurability((short) 5);
        apply();
    }

    /**
     * 스킬 설명 아이템을 준비 상태로 만든다.
     *
     * @param amount 아이템 수량
     */
    private void setItemReady(int amount) {
        itemStack = skill.getItemStack().clone();
        itemStack.setAmount(amount);
        apply();

        if (skill instanceof UltimateSkill)
            SoundUtil.play(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2F, combatUser.getEntity());
        else if (skill instanceof ActiveSkill)
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 2F, combatUser.getEntity());
    }

    /**
     * 스킬 전역 쿨타임이 끝났는 지 확인한다.
     *
     * @return 전역 쿨타임 종료 여부
     */
    public boolean isGlobalCooldownFinished() {
        return combatUser.getEntity().getCooldown(Skill.MATERIAL) == 0;
    }

    /**
     * 스킬 전역 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    public void setGlobalCooldown(int cooldown) {
        if (cooldown == -1)
            cooldown = 9999;
        combatUser.getEntity().setCooldown(Skill.MATERIAL, cooldown);
    }

    /**
     * 스킬의 쿨타임과 지속시간을 초기화한다.
     */
    public void reset() {
        CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, 0);
        CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, 0);
    }
}
