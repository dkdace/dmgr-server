package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.WeaponBase;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.damageable.Damageable;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

@Getter
public final class JagerWeaponL extends WeaponBase implements Reloadable, Swappable<JagerWeaponR>, Aimable {
    /** 보조무기 객체 */
    private final JagerWeaponR subweapon;
    /** 남은 탄약 수 */
    @Setter
    private int remainingAmmo = getCapacity();
    /** 재장전 상태 */
    @Setter
    private boolean reloading;
    /** 무기 전환 상태 */
    @Setter
    private SwapState swapState = SwapState.PRIMARY;
    /** 정조준 상태 */
    @Setter
    private boolean aiming;

    public JagerWeaponL(CombatUser combatUser) {
        super(combatUser, JagerWeaponInfo.getInstance());
        subweapon = new JagerWeaponR(combatUser, this);
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerWeaponInfo.COOLDOWN;
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
    public long getSwapDuration() {
        return JagerWeaponInfo.SWAP_DURATION;
    }

    @Override
    public ZoomLevel getZoomLevel() {
        return ZoomLevel.L4;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && swapState != SwapState.SWAPPING && combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                if (((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isChecking()) {
                    ((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).onAccept();
                    return;
                }
                if (getRemainingAmmo() == 0) {
                    reload();
                    return;
                }

                new JagerWeaponProjectile().shoot(2.5F);

                CooldownManager.setCooldown(combatUser, Cooldown.NO_SPRINT, 7);
                setCooldown();
                consume(1);
                playShootSound(combatUser.getEntity().getLocation());

                break;
            }
            case RIGHT_CLICK: {
                if (((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isChecking())
                    return;

                toggleAim();
                swap();

                break;
            }
            case DROP: {
                if (((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isChecking())
                    return;

                reload();

                break;
            }
        }
    }

    /**
     * 발사 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playShootSound(Location location) {
        SoundUtil.play("random.gun2.m16_1", location, 0.8F, 1.2F);
        SoundUtil.play(Sound.BLOCK_FIRE_EXTINGUISH, location, 0.8F, 1.7F);
        CombatUtil.setRecoil(combatUser, JagerWeaponInfo.RECOIL.UP, JagerWeaponInfo.RECOIL.SIDE, JagerWeaponInfo.RECOIL.UP_SPREAD,
                JagerWeaponInfo.RECOIL.SIDE_SPREAD, 2, 1F);
    }

    @Override
    public boolean canReload() {
        return Reloadable.super.canReload() || subweapon.getRemainingAmmo() < subweapon.getCapacity();
    }

    /**
     * 재장전 작업을 실행한다.
     */
    @Override
    public void onReloadTick(int i) {
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
    }

    @Override
    public void onReloadFinished() {
        subweapon.setRemainingAmmo(subweapon.getCapacity());
    }

    @Override
    public void onSwapStart(SwapState swapState) {
        Location location = combatUser.getEntity().getLocation();
        if (swapState == SwapState.PRIMARY)
            SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, location, 0.6F, 1.9F);
        else if (swapState == SwapState.SECONDARY)
            SoundUtil.play(Sound.ENTITY_WOLF_HOWL, location, 0.6F, 1.9F);

        setCooldown(JagerWeaponInfo.SWAP_DURATION);
    }

    @Override
    public void onSwapFinished(SwapState swapState) {
    }

    @Override
    public void onAimEnable() {
        combatUser.setGlobalCooldown((int) JagerWeaponInfo.SWAP_DURATION);
        combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).addModifier("JagerWeaponL", -JagerWeaponInfo.AIM_SPEED);
    }

    @Override
    public void onAimDisable() {
        combatUser.setGlobalCooldown((int) JagerWeaponInfo.SWAP_DURATION);
        combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).removeModifier("JagerWeaponL");
    }

    private class JagerWeaponProjectile extends Projectile {
        public JagerWeaponProjectile() {
            super(JagerWeaponL.this.combatUser, JagerWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10)
                    .maxDistance(JagerWeaponInfo.DISTANCE).condition(JagerWeaponL.this.combatUser::isEnemy).build());
        }

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
        public boolean onHitEntity(Location location, Vector direction, Damageable target, boolean isCrit) {
            target.damage(combatUser, JagerWeaponInfo.DAMAGE, DamageType.NORMAL, false, true);
            JagerTrait.addFreezeValue(target, JagerWeaponInfo.FREEZE);
            return false;
        }
    }
}
