package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.GradualSpreadModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.GunHitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

@Getter
public final class ArkaceWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    private final GradualSpreadModule fullAutoModule;

    public ArkaceWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceWeaponInfo.getInstance());

        reloadModule = new ReloadModule(this, ArkaceWeaponInfo.CAPACITY, ArkaceWeaponInfo.RELOAD_DURATION);
        fullAutoModule = new GradualSpreadModule(this, ActionKey.RIGHT_CLICK, ArkaceWeaponInfo.FIRE_RATE, ArkaceWeaponInfo.SPREAD.INCREMENT,
                ArkaceWeaponInfo.SPREAD.START, ArkaceWeaponInfo.SPREAD.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }
                if (cancelP1()) {
                    setCooldown(ArkaceWeaponInfo.SPRINT_READY_DURATION);
                    return;
                }

                Location loc = combatUser.getEntity().getLocation();
                if (combatUser.getSkill(ArkaceUltInfo.getInstance()).isDurationFinished()) {
                    double spread = combatUser.isMoving() ? fullAutoModule.increaseSpread() : 0;
                    if (combatUser.getEntity().isSprinting() || !combatUser.getEntity().isOnGround())
                        spread *= ArkaceWeaponInfo.SPREAD.SPRINT_MULTIPLIER;

                    Vector dir = VectorUtil.getSpreadedVector(loc.getDirection(), spread);
                    new ArkaceWeaponHitscan(false).shoot(dir);
                    reloadModule.consume(1);

                    CombatUtil.setRecoil(combatUser, ArkaceWeaponInfo.RECOIL.UP, ArkaceWeaponInfo.RECOIL.SIDE, ArkaceWeaponInfo.RECOIL.UP_SPREAD,
                            ArkaceWeaponInfo.RECOIL.SIDE_SPREAD, 2, 2);
                    SoundUtil.playNamedSound(NamedSound.COMBAT_ARKACE_WEAPON_USE, loc);
                    TaskUtil.addTask(this, new DelayTask(() -> SoundUtil.playNamedSound(NamedSound.COMBAT_GUN_SHELL_DROP, loc), 8));
                } else {
                    new ArkaceWeaponHitscan(true).shoot();

                    SoundUtil.playNamedSound(NamedSound.COMBAT_ARKACE_WEAPON_USE_ULT, loc);
                }

                break;
            }
            case DROP: {
                onAmmoEmpty();

                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        reloadModule.cancel();
    }

    /**
     * 패시브 1번 스킬을 취소시킨다.
     *
     * @return 무기 사용 취소 여부
     */
    private boolean cancelP1() {
        ArkaceP1 skillp1 = combatUser.getSkill(ArkaceP1Info.getInstance());
        long skillp1Cooldown = ArkaceWeaponInfo.SPRINT_READY_DURATION + 2;

        if (skillp1.isCancellable()) {
            skillp1.onCancelled();
            skillp1.setCooldown(skillp1Cooldown);

            return true;
        }

        skillp1.setCooldown(skillp1Cooldown);
        return false;
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < ArkaceWeaponInfo.CAPACITY;
    }

    @Override
    public void onAmmoEmpty() {
        if (reloadModule.isReloading())
            return;

        onCancelled();
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        switch ((int) i) {
            case 3:
                SoundUtil.play(Sound.BLOCK_PISTON_CONTRACT, combatUser.getEntity().getLocation(), 0.6, 1.6);
                break;
            case 4:
                SoundUtil.play(Sound.ENTITY_VILLAGER_NO, combatUser.getEntity().getLocation(), 0.6, 1.9);
                break;
            case 18:
                SoundUtil.play(Sound.ENTITY_PLAYER_HURT, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 19:
                SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, combatUser.getEntity().getLocation(), 0.6, 1);
                break;
            case 20:
                SoundUtil.play(Sound.ENTITY_VILLAGER_YES, combatUser.getEntity().getLocation(), 0.6, 1.8);
                break;
            case 26:
                SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, combatUser.getEntity().getLocation(), 0.6, 1.7);
                break;
            case 27:
                SoundUtil.play(Sound.BLOCK_IRON_DOOR_OPEN, combatUser.getEntity().getLocation(), 0.6, 1.8);
                break;
            default:
                break;
        }
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    public void reset() {
        super.reset();

        reloadModule.setRemainingAmmo(ArkaceWeaponInfo.CAPACITY);
    }

    private final class ArkaceWeaponHitscan extends GunHitscan {
        private final boolean isUlt;
        private double distance = 0;

        private ArkaceWeaponHitscan(boolean isUlt) {
            super(combatUser, HitscanOption.builder().condition(combatUser::isEnemy).build());
            this.isUlt = isUlt;
        }

        @Override
        protected boolean onInterval() {
            distance += getVelocity().length();
            return super.onInterval();
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, -0.2, 0);
            if (isUlt)
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1,
                        0, 0, 0, 0, 230, 255);
            else
                ParticleUtil.play(Particle.CRIT, loc, 1, 0, 0, 0, 0);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (isUlt)
                target.getDamageModule().damage(combatUser, ArkaceWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), isCrit, false);
            else {
                int damage = CombatUtil.getDistantDamage(ArkaceWeaponInfo.DAMAGE, distance, ArkaceWeaponInfo.DAMAGE_WEAKENING_DISTANCE);
                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, getLocation(), isCrit, true);
            }

            return false;
        }
    }
}
