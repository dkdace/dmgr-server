package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Setter
public final class ChedWeapon extends AbstractWeapon {
    /** 활 충전량 */
    private double power;

    public ChedWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, ChedWeaponInfo.getInstance(), ChedWeaponInfo.COOLDOWN);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.PERIODIC_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getActionManager().getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                ActionManager actionManager = combatUser.getActionManager();
                ChedA1 skill1 = actionManager.getSkill(ChedA1Info.getInstance());

                if (skill1.isEnabled()) {
                    setCooldown(ChedA1Info.COOLDOWN);

                    skill1.shot();
                } else {
                    setCooldown();
                    setCanShoot(true);

                    if (combatUser.getEntity().isHandRaised()) {
                        Weapon weapon = actionManager.getWeapon();
                        weapon.setVisible(false);
                        weapon.setVisible(true);
                    }

                    ChedWeaponInfo.Sounds.CHARGE.play(combatUser.getLocation());
                }

                break;
            }
            case PERIODIC_1: {
                new ChedWeaponProjectile(power).shot();
                setCanShoot(false);

                ChedWeaponInfo.Sounds.USE.play(combatUser.getLocation(), power, power);

                break;
            }
            default:
                break;
        }
    }

    /**
     * 무기의 발사 가능 여부를 설정한다.
     *
     * @param canShoot 발사 가능 여부
     */
    void setCanShoot(boolean canShoot) {
        combatUser.getEntity().getInventory().setItem(ChedWeaponInfo.ARROW_INVENTORY_SLOT, new ItemStack(canShoot ? Material.ARROW : Material.AIR));
    }

    private final class ChedWeaponProjectile extends Projectile<Damageable> {
        private final double power;

        private ChedWeaponProjectile(double power) {
            super(ChedWeapon.this, (int) (power * ChedWeaponInfo.MAX_VELOCITY), EntityCondition.enemy(combatUser));
            this.power = power;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            ChedWeaponInfo.Sounds.HIT.play(location, power);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(9, location -> {
                        Location loc = LocationUtil.getLocationFromOffset(location, 0.2, 0, 0);
                        CombatEffectUtil.BULLET_TRAIL_PARTICLE.play(loc, combatUser.getEntity());
                    }));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playHitBlockSound(location, hitBlock, power);
                CombatEffectUtil.playSmallHitBlockParticle(location, hitBlock, power * 1.5);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                if (target.getDamageModule().damage(this, power * ChedWeaponInfo.MAX_DAMAGE, DamageType.NORMAL, location, isCrit, true)
                        && target.isGoalTarget() && isCrit)
                    combatUser.addScore("치명타", power * ChedWeaponInfo.CRIT_SCORE);

                return false;
            });
        }
    }
}
