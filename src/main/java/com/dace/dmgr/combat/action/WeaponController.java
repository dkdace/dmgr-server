package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringFormUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * 무기 상태를 관리하는 컨트롤러 클래스.
 */
public class WeaponController {
    /** 플레이어 객체 */
    private final CombatUser combatUser;
    /** 무기 객체 */
    private final Weapon weapon;
    /** 무기 아이템 */
    private final ItemStack itemStack;
    /** 남은 탄약 수 */
    private int remainingAmmo = -1;
    /** 재장전 상태 */
    private boolean reloading = false;
    /** 보조무기 상태 */
    private Swappable.State swappingState = Swappable.State.PRIMARY;
    

    /**
     * 무기 컨트롤러 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param weapon     무기 객체
     */
    public WeaponController(CombatUser combatUser, Weapon weapon) {
        this.combatUser = combatUser;
        this.weapon = weapon;
        this.itemStack = weapon.getItemStack().clone();
        if (weapon instanceof Reloadable)
            this.remainingAmmo = ((Reloadable) weapon).getCapacity();
        apply();
    }

    /**
     * 플레이어의 인벤토리에 무기 아이템을 적용한다.
     */
    public void apply() {
        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    /**
     * 무기의 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     * @param force    덮어쓰기 여부
     */
    public void setCooldown(int cooldown, boolean force) {
        if (cooldown == -1)
            cooldown = 9999;
        if (force || cooldown > combatUser.getEntity().getCooldown(Weapon.MATERIAL))
            combatUser.getEntity().setCooldown(Weapon.MATERIAL, cooldown);
    }

    /**
     * 무기의 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick)
     */
    public void setCooldown(int cooldown) {
        setCooldown(cooldown, false);
    }

    /**
     * 무기의 쿨타임을 무기 정보에 설정된 기본 쿨타임으로 설정한다.
     */
    public void setCooldown() {
        setCooldown((int) weapon.getCooldown());
    }

    /**
     * 무기의 쿨타임이 끝났는 지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    public boolean isCooldownFinished() {
        return combatUser.getEntity().getCooldown(Weapon.MATERIAL) == 0;
    }

    public boolean isReloading() {
        return reloading;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public int getRemainingAmmo() {
        return remainingAmmo;
    }

    public void setRemainingAmmo(int remainingAmmo) {
        this.remainingAmmo = remainingAmmo;
    }

    /**
     * 지정한 양만큼 무기의 탄약을 소모한다.
     *
     * @param amount 탄약 소모량
     */
    public void consume(int amount) {
        remainingAmmo -= amount;
        if (remainingAmmo < 0)
            remainingAmmo = 0;
    }

    /**
     * 무기를 재장전한다.
     *
     * <p>{@link Reloadable}을 상속받는 클래스여야 한다.</p>
     */
    public void reload() {
        if (!(weapon instanceof Reloadable))
            return;

        if (remainingAmmo >= ((Reloadable) weapon).getCapacity())
            return;
        if (reloading)
            return;

        reloading = true;

        long duration = ((Reloadable) weapon).getReloadDuration();
        CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_RELOAD, duration);

        new TaskTimer(1, duration) {
            @Override
            public boolean run(int i) {
                if (!reloading)
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                combatUser.sendActionBar("§c§l재장전... " + StringFormUtil.getProgressBar(i, duration, ChatColor.WHITE) + " §f[" + time + "초]",
                        2);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                combatUser.sendActionBar("§a§l재장전 완료", 8);

                remainingAmmo = ((Reloadable) weapon).getCapacity();
                reloading = false;
            }
        };
    }

    /**
     * 이중 탄창 무기의 모드를 특정 상태로 교체합니다.
     */
    private void swapTo(Swappable.State targetState) {
        if (!(weapon instanceof Swappable))
            return;
        if (swappingState == targetState || swappingState == Swappable.State.SWAPPING)
            return;
        if (targetState == Swappable.State.SWAPPING)
            return;

        swappingState = Swappable.State.SWAPPING;

        long duration = ((Swappable) weapon).getSwapDuration();
        CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_SWAP, duration);

        new TaskTimer(1, duration) {
            @Override
            public boolean run(int i) {
                if (swappingState != Swappable.State.SWAPPING)
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                combatUser.sendActionBar("§c§l무기 교체 중... " + StringFormUtil.getProgressBar(i, duration, ChatColor.WHITE) + " §f[" + time + "초]",
                        2);
                return true;
            }
            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(combatUser, Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;
                
                combatUser.sendActionBar("§a§l무기 교체 완료", 8);
                swappingState = targetState;
            }
        };
    };

    /**
     * 이중 탄창 무기의 모드를 반대 무기로 교체합니다.
     */
    public void swap() {
        if (swappingState == Swappable.State.PRIMARY)
            swapTo(Swappable.State.SECONDARY);
        else if (swappingState == Swappable.State.SECONDARY)
            swapTo(Swappable.State.PRIMARY);
    }
}
