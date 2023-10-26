package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.StringFormUtil;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * 재장전 가능한 무기의 인터페이스.
 */
public interface Reloadable extends Weapon {
    /**
     * @return 남은 탄약 수
     */
    int getRemainingAmmo();

    /**
     * @param remainingAmmo 남은 탄약 수
     */
    void setRemainingAmmo(int remainingAmmo);

    /**
     * @return 재장전 상태
     */
    boolean isReloading();

    /**
     * @param isReloading 재장전 상태
     */
    void setReloading(boolean isReloading);

    /**
     * 지정한 양만큼 무기의 탄약을 소모한다.
     *
     * <p>탄약을 전부 소진하면 {@link Reloadable#reload()}를 호출한다.</p>
     *
     * @param amount 탄약 소모량
     */
    default void consume(int amount) {
        setRemainingAmmo(Math.max(0, getRemainingAmmo() - amount));

        if (isReloading())
            setReloading(false);
        else if (getRemainingAmmo() == 0)
            reload();
    }

    /**
     * 무기의 장탄수를 반환한다.
     *
     * @return 장탄수
     */
    int getCapacity();

    /**
     * 무기의 장전 시간을 반환한다.
     *
     * @return 장전 시간 (tick)
     */
    long getReloadDuration();

    /**
     * 재장전을 할 수 있는 지 확인한다.
     *
     * @return 재장전 가능 여부
     */
    default boolean canReload() {
        return getRemainingAmmo() < getCapacity();
    }

    /**
     * 무기를 재장전한다.
     */
    default void reload() {
        if (!canReload() || isReloading())
            return;
        if (this instanceof Swappable && ((Swappable<?>) this).getSwapState() == Swappable.SwapState.SWAPPING)
            return;

        setReloading(true);
        CooldownManager.setCooldown(this, Cooldown.WEAPON_RELOAD, getReloadDuration());

        TaskManager.addTask(this, new ActionTaskTimer(getCombatUser(), 1, getReloadDuration()) {
            @Override
            public boolean onTickAction(int i) {
                if (!isReloading())
                    return false;
                if (Reloadable.this instanceof Aimable && ((Aimable) Reloadable.this).isAiming())
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                getCombatUser().sendActionBar(MessageFormat.format(MESSAGES.RELOADING, StringFormUtil.getProgressBar(i, getReloadDuration(),
                        ChatColor.WHITE), time), 2);
                onReloadTick(i);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(getCombatUser(), Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                getCombatUser().sendActionBar(MESSAGES.RELOAD_COMPLETE, 8);

                setRemainingAmmo(getCapacity());
                setReloading(false);
                onReloadFinished();
            }
        });
    }

    /**
     * {@link Reloadable#reload()}에서 재장전을 진행할 때 (매 tick마다) 실행할 작업.
     *
     * @param i 인덱스
     */
    void onReloadTick(int i);

    /**
     * {@link Reloadable#reload()}에서 재장전이 끝났을 때 실행할 작업.
     */
    void onReloadFinished();

    /**
     * 메시지 목록.
     */
    class MESSAGES {
        /** 재장전 중 메시지 */
        static final String RELOADING = "§c§l재장전... {0} §f[{1}초]";
        /** 재장전 완료 메시지 */
        static final String RELOAD_COMPLETE = "§a§l재장전 완료";
    }
}
