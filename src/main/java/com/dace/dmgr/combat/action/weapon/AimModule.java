package com.dace.dmgr.combat.action.weapon;

import com.comphenix.packetwrapper.WrapperPlayServerAbilities;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * 무기의 정조준 모듈 클래스.
 *
 * <p>무기가 {@link Aimable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Aimable
 */
public final class AimModule {
    /** 무기 객체 */
    private final Weapon weapon;

    /** 정조준 상태 */
    @Getter
    private boolean aiming = false;

    public AimModule(Weapon weapon) {
        this.weapon = weapon;
    }

    /**
     * 지정한 플레이어에게 화면 확대 효과를 재생한다.
     *
     * @param player 대상 플레이어
     * @param value  값
     */
    private void playZoomEffect(Player player, float value) {
        WrapperPlayServerAbilities packet = new WrapperPlayServerAbilities();

        packet.setWalkingSpeed(player.getWalkSpeed() * value);

        packet.sendPacket(player);
    }

    /**
     * 무기를 정조준한다.
     */
    public void aim() {
        if (weapon instanceof Swappable && ((Swappable) weapon).getWeaponState() == SwapModule.WeaponState.SWAPPING)
            return;

        aiming = !aiming;

        if (aiming) {
            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    if (EntityInfoRegistry.getCombatUser(weapon.getCombatUser().getEntity()) == null)
                        return false;
                    if (!aiming)
                        return false;
                    if (weapon instanceof Reloadable && ((Reloadable) weapon).isReloading())
                        return false;

                    playZoomEffect(weapon.getCombatUser().getEntity(), ((Aimable) weapon).getZoomLevel().getValue());

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    aiming = false;
                }
            };
        }
    }

    /**
     * 조준 시 확대 레벨(화면이 확대되는 정도) 목록.
     */
    @Getter
    public enum ZoomLevel {
        L1(1.2F),
        L2(6F),
        L3(-4.2F),
        L4(-1.8F),
        L5(-1.2F),
        L6(-0.93F),
        L7(-0.8F),
        L8(-0.73F),
        L9(-0.68F),
        L10(-0.64F);

        /** 실제 값 */
        private final float value;

        ZoomLevel(float value) {
            this.value = value;
        }
    }
}
