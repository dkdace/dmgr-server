package com.dace.dmgr.combat.action.skill;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.glow.GlowAPI;

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
        runCooldown(getDefaultCooldown());
    }

    /**
     * 스킬의 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    @Override
    public void setCooldown(long cooldown) {
        if (!isCooldownFinished())
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
    }

    /**
     * 스킬의 쿨타임을 실행한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    protected void runCooldown(long cooldown) {
        CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;

                onCooldownTick();

                if (isCooldownFinished()) {
                    onCooldownFinished();
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * 쿨타임이 진행할 때 (매 tick마다) 실행할 작업.
     *
     * @see Skill#runCooldown(long)
     */
    protected void onCooldownTick() {
        long cooldown = CooldownManager.getCooldown(this, Cooldown.SKILL_COOLDOWN);

        displayCooldown((int) Math.ceil((float) cooldown / 20));
    }

    /**
     * 쿨타임이 끝났을 때 실행할 작업.
     *
     * @see Skill#runCooldown(long)
     */
    protected void onCooldownFinished() {
        displayReady(1);

        if (actionInfo instanceof UltimateSkillInfo)
            SoundUtil.play(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2F, combatUser.getEntity());
        else if (actionInfo instanceof ActiveSkillInfo)
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 2F, combatUser.getEntity());
    }

    /**
     * 스킬의 기본 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    public abstract long getDefaultDuration();

    /**
     * 스킬의 지속시간을 설정한다.
     *
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    public void setDuration(long duration) {
        if (isUsing())
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
    }

    /**
     * 스킬의 지속시간을 실행한다.
     *
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    protected void runDuration(long duration, long cooldown) {
        CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;

                onDurationTick();

                if (!isUsing()) {
                    onDurationFinished();
                    if (isCooldownFinished())
                        runCooldown(cooldown);

                    return false;
                }

                return true;
            }
        };
    }

    /**
     * 지속시간이 진행할 때 (매 tick마다) 실행할 작업.
     *
     * @see Skill#runDuration(long, long)
     */
    protected void onDurationTick() {
        long duration = CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION);

        displayUsing((int) Math.ceil((float) duration / 20));
    }

    /**
     * 지속시간이 끝났을 때 실행할 작업.
     *
     * @see Skill#runDuration(long, long)
     */
    protected void onDurationFinished() {
    }

    /**
     * 스킬을 활성화한다.
     */
    protected void enable() {
        if (getDefaultDuration() != 0)
            runDuration(getDefaultDuration(), getDefaultCooldown());
        else {
            setCooldown(getDefaultCooldown());
            if (isCooldownFinished())
                runCooldown(getDefaultCooldown());
        }
    }

    /**
     * 스킬을 비활성화한다.
     */
    protected void disable() {
        if (isUsing())
            setDuration(0);
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
    @Override
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
     * 스킬을 사용할 수 있는 지 확인한다.
     *
     * @return 사용 가능 여부
     */
    public boolean canUse() {
        return isCooldownFinished();
    }

    /**
     * 스킬 전역 쿨타임이 끝났는 지 확인한다.
     *
     * @return 전역 쿨타임 종료 여부
     */
    public boolean isGlobalCooldownFinished() {
        return combatUser.getEntity().getCooldown(SkillInfo.MATERIAL) == 0;
    }

    /**
     * 스킬 전역 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    public void setGlobalCooldown(int cooldown) {
        if (cooldown == -1)
            cooldown = 9999;
        combatUser.getEntity().setCooldown(SkillInfo.MATERIAL, cooldown);
    }

    /**
     * 스킬 설명 아이템을 쿨타임 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    protected void displayCooldown(int amount) {
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
    protected void displayReady(int amount) {
        itemStack = actionInfo.getItemStack().clone();
        display(amount);
    }

    /**
     * 스킬 설명 아이템을 사용 중인 상태로 표시한다.
     *
     * @param amount 아이템 수량
     */
    protected void displayUsing(int amount) {
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

    /**
     * 스킬 설치 모드를 활성화한다.
     *
     * @param maxDistance 최대 설치 거리
     */
    public void enableDeployMode(int maxDistance) {
        Player player = combatUser.getEntity();

        ItemStack falseItem = new ItemBuilder(Material.CONCRETE).setDamage((short) 14).build();
        ItemStack trueItem = new ItemBuilder(Material.CONCRETE).setDamage((short) 5).build();

        ArmorStand pointer = player.getWorld().spawn(player.getTargetBlock(null, maxDistance).getLocation(), ArmorStand.class);
        pointer.setMarker(true);
        pointer.setAI(false);
        pointer.setInvulnerable(true);
        pointer.setGravity(false);
        pointer.setSilent(true);
        pointer.setVisible(false);
        pointer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false), true);

        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{pointer.getEntityId()});
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            if (player != player2)
                packet.sendPacket(player2);
        });

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;
                if (!isUsing())
                    return false;

                Location location = player.getTargetBlock(null, maxDistance).getLocation().add(0.5, 0, 0.5);

                pointer.teleport(location.clone().add(0, -0.25, 0));
                if (location.clone().add(0, 1, 0).getBlock().isEmpty() && !location.getBlock().isEmpty()) {
                    GlowAPI.setGlowing(pointer, GlowAPI.Color.GREEN, player);
                    pointer.setHelmet(trueItem);
                } else {
                    GlowAPI.setGlowing(pointer, GlowAPI.Color.RED, player);
                    pointer.setHelmet(falseItem);
                }
                pointer.setAI(false);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                pointer.remove();
            }
        };
    }
}
