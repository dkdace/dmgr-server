package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.*;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public final class JagerWeaponL extends Weapon implements Reloadable, Swappable, Aimable {
    /** 재장전 모듈 객체 */
    private final ReloadModule reloadModule;
    /** 2중 무기 모듈 객체 */
    private final SwapModule swapModule;
    /** 정조준 모듈 객체 */
    private final AimModule aimModule;
    /** 보조무기 객체 */
    @Getter
    private final JagerWeaponR subweapon;

    public JagerWeaponL(CombatUser combatUser) {
        super(combatUser, JagerWeaponInfo.getInstance());
        reloadModule = new ReloadModule(this);
        swapModule = new SwapModule(this);
        aimModule = new AimModule(this);
        subweapon = new JagerWeaponR(combatUser, this);
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP);
    }

    @Override
    public long getDefaultCooldown() {
        return JagerWeaponInfo.COOLDOWN;
    }

    @Override
    public int getRemainingAmmo() {
        return reloadModule.getRemainingAmmo();
    }

    @Override
    public void setRemainingAmmo(int remainingAmmo) {
        reloadModule.setRemainingAmmo(remainingAmmo);
    }

    @Override
    public boolean isReloading() {
        return reloadModule.isReloading();
    }

    @Override
    public void cancelReloading() {
        reloadModule.setReloading(false);
    }

    @Override
    public int getCapacity() {
        return JagerWeaponInfo.CAPACITY;
    }

    @Override
    public long getReloadDuration() {
        return JagerWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public SwapModule.WeaponState getWeaponState() {
        return swapModule.getWeaponState();
    }

    @Override
    public long getSwapDuration() {
        return JagerWeaponInfo.SWAP_DURATION;
    }

    @Override
    public boolean isAiming() {
        return aimModule.isAiming();
    }

    @Override
    public AimModule.ZoomLevel getZoomLevel() {
        return AimModule.ZoomLevel.L4;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && getWeaponState() != SwapModule.WeaponState.SWAPPING;
    }

    @Override
    public void onUse(ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                if (((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isConfirming()) {
                    ((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).confirm();
                    return;
                }
                if (getRemainingAmmo() == 0) {
                    reload();
                    return;
                }

                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                Location location = combatUser.getEntity().getLocation();

                SoundUtil.play("random.gun2.m16_1", location, 0.8F, 1.2F);
                SoundUtil.play("block.lava.extinguish", location, 0.8F, 1.7F);
                CombatUtil.setRecoil(combatUser, JagerWeaponInfo.RECOIL.UP, JagerWeaponInfo.RECOIL.SIDE, JagerWeaponInfo.RECOIL.UP_SPREAD,
                        JagerWeaponInfo.RECOIL.SIDE_SPREAD, 2, 1F);
                setCooldown();
                reloadModule.consume(1);

                new Projectile(combatUser, JagerWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(5).maxDistance(JagerWeaponInfo.DISTANCE).build()) {
                    @Override
                    public void trail(Location location) {
                        Location trailLoc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 1, 0, 0, 0, 137, 185, 240);
                    }

                    @Override
                    public boolean onHitBlock(Location location, Vector direction, Block hitBlock) {
                        return false;
                    }

                    @Override
                    public boolean onHitEntity(Location location, Vector direction, CombatEntity<?> target, boolean isCrit) {
                        target.damage(combatUser, JagerWeaponInfo.DAMAGE, "", false, true);
                        JagerTrait.addFreezeValue(target, JagerWeaponInfo.FREEZE);
                        return false;
                    }
                }.shoot(2.5F);

                break;
            }
            case RIGHT_CLICK: {
                if (((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isConfirming())
                    return;

                aim();
                swap();

                break;
            }
            case DROP: {
                if (((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isConfirming())
                    return;

                reload();

                break;
            }
        }
    }

    @Override
    public void reload() {
        if (getRemainingAmmo() >= getCapacity() && subweapon.getRemainingAmmo() >= subweapon.getCapacity())
            return;
        if (isReloading())
            return;

        if (isAiming()) {
            aim();
            swap();

            new TaskTimer(1, JagerWeaponInfo.SWAP_DURATION) {
                @Override
                public boolean run(int i) {
                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    reloadTask();
                }
            };
        } else
            reloadTask();
    }

    /**
     * 재장전 작업을 실행한다.
     */
    private void reloadTask() {
        reloadModule.reload();

        new TaskTimer(1, JagerWeaponInfo.RELOAD_DURATION - 1) {
            @Override
            public boolean run(int i) {
                if (!isReloading())
                    return false;

                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 3);

                switch (i) {
                    case 3:
                        SoundUtil.play(Sound.ENTITY_WOLF_HOWL, combatUser.getEntity().getLocation(), 0.6F, 1.7F);
                        break;
                    case 4:
                        SoundUtil.play(Sound.BLOCK_FIRE_EXTINGUISH, combatUser.getEntity().getLocation(), 0.6F, 1.2F);
                        break;
                    case 6:
                        SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, combatUser.getEntity().getLocation(), 0.6F, 0.8F);
                        break;
                    case 25:
                        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, combatUser.getEntity().getLocation(), 0.6F, 0.5F);
                        break;
                    case 27:
                        SoundUtil.play(Sound.ENTITY_CAT_PURREOW, combatUser.getEntity().getLocation(), 0.6F, 1.7F);
                        break;
                    case 35:
                        SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, combatUser.getEntity().getLocation(), 0.6F, 1.8F);
                        break;
                    case 37:
                        SoundUtil.play(Sound.BLOCK_IRON_DOOR_OPEN, combatUser.getEntity().getLocation(), 0.6F, 1.7F);
                        break;
                }

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                if (!cancelled)
                    subweapon.setRemainingAmmo(subweapon.getCapacity());
            }
        };
    }

    @Override
    public void aim() {
        combatUser.getSkill(JagerA1Info.getInstance()).setGlobalCooldown((int) JagerWeaponInfo.SWAP_DURATION);
        if (!isAiming())
            combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).addModifier("JagerWeaponL", -JagerWeaponInfo.AIM_SPEED);
        else
            combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).removeModifier("JagerWeaponL");

        aimModule.aim();
    }

    @Override
    public void swap() {
        Location location = combatUser.getEntity().getLocation();

        if (getWeaponState() == SwapModule.WeaponState.PRIMARY)
            SoundUtil.play(Sound.ENTITY_WOLF_HOWL, location, 0.6F, 1.9F);
        else if (getWeaponState() == SwapModule.WeaponState.SECONDARY)
            SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, location, 0.6F, 1.9F);

        setCooldown(JagerWeaponInfo.SWAP_DURATION);
        swapModule.swap();
    }
}
