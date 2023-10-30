package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.ActionModule;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * 무기의 재장전 모듈 클래스.
 *
 * <p>무기가 {@link Reloadable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Reloadable
 */
public final class ReloadModule implements ActionModule {
    /** 무기 객체 */
    private final Reloadable weapon;
    /** 장탄수 */
    private final int capacity;
    /** 장전 시간 (tick) */
    private final long reloadDuration;

    /** 남은 탄약 수 */
    @Getter
    @Setter
    private int remainingAmmo;
    /** 재장전 상태 */
    @Getter
    @Setter
    private boolean isReloading = false;

    public ReloadModule(Reloadable weapon, int capacity, long reloadDuration) {
        this.weapon = weapon;
        this.remainingAmmo = capacity;
        this.capacity = capacity;
        this.reloadDuration = reloadDuration;
    }

    /**
     * 지정한 양만큼 무기의 탄약을 소모한다.
     *
     * <p>탄약을 전부 소진하면 {@link ReloadModule#reload()}를 호출한다.</p>
     *
     * @param amount 탄약 소모량
     */
    public void consume(int amount) {
        remainingAmmo = Math.max(0, remainingAmmo - amount);

        if (isReloading)
            isReloading = false;
        else if (remainingAmmo == 0)
            weapon.onAmmoEmpty();
    }

    /**
     * 무기를 재장전한다.
     */
    public void reload() {
        if (!weapon.canReload() || isReloading)
            return;
        if (weapon instanceof Swappable && ((Swappable<?>) weapon).getSwapModule().getSwapState() == Swappable.SwapState.SWAPPING)
            return;

        isReloading = true;
        CooldownManager.setCooldown(this, Cooldown.WEAPON_RELOAD, reloadDuration);

        TaskManager.addTask(weapon, new ActionTaskTimer(weapon.getCombatUser(), 1, reloadDuration) {
            @Override
            public boolean onTickAction(int i) {
                if (!isReloading)
                    return false;
                if (weapon instanceof Aimable && ((Aimable) weapon).getAimModule().isAiming())
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                weapon.getCombatUser().sendActionBar(MessageFormat.format(MESSAGES.RELOADING, StringFormUtil.getProgressBar(i,
                        reloadDuration, ChatColor.WHITE), time), 2);
                weapon.onReloadTick(i);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                weapon.getCombatUser().sendActionBar(MESSAGES.RELOAD_COMPLETE, 8);

                remainingAmmo = capacity;
                isReloading = false;
                weapon.onReloadFinished();
            }
        });
    }

    /**
     * 메시지 목록.
     */
    private interface MESSAGES {
        /** 재장전 중 메시지 */
        String RELOADING = "§c§l재장전... {0} §f[{1}초]";
        /** 재장전 완료 메시지 */
        String RELOAD_COMPLETE = "§a§l재장전 완료";
    }
}