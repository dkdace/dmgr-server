package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.RightClickHandler;
import com.dace.dmgr.combat.ability.handler.SystemHandler;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ChedWeapon extends AbstractWeapon implements RightClickHandler, SystemHandler {
    /** 활 충전량 */
    private double power;

    public ChedWeapon(@NonNull CombatUser combatUser, @NonNull ChedWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, ChedWeaponInfo.COOLDOWN);
    }

    @Override
    protected boolean canUse() {
        ChedP1 skillp1 = combatUser.getAbilityManager().getAbility(ChedP1Info.getInstance());
        return super.canUse() && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onRightClick() {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        ChedA1 skill1 = abilityManager.getAbility(ChedA1Info.getInstance());

        if (skill1.isEnabled()) {
            setCooldown(ChedA1Info.COOLDOWN);

            skill1.shot();
        } else {
            setCooldown();
            setCanShoot(true);

            if (combatUser.getEntity().isHandRaised()) {
                Weapon weapon = abilityManager.getWeapon();
                weapon.setVisible(false);
                weapon.setVisible(true);
            }

            ChedWeaponInfo.Effects.CHARGE.play(combatUser.getLocation());
        }
    }

    @Override
    public void onSystemUse() {
        new ChedWeaponProjectile(power).shot();
        setCanShoot(false);

        ChedWeaponInfo.Effects.SHOOT.apply(power).play(combatUser.getLocation());
    }

    /**
     * 무기의 발사 가능 여부를 설정한다.
     *
     * @param canShoot 발사 가능 여부
     */
    void setCanShoot(boolean canShoot) {
        combatUser.getEntity().getInventory().setItem(ChedWeaponInfo.ARROW_INVENTORY_SLOT, new ItemStack(canShoot ? Material.ARROW : Material.AIR));
    }

    /**
     * 무기 발사 전에 실행할 작업.
     *
     * @param power 활 충전량
     */
    public void beforeShoot(double power) {
        this.power = power;
        use(ActionKey.SYSTEM);
    }

    private final class ChedWeaponProjectile extends Projectile<Damageable> {
        private final double power;

        private ChedWeaponProjectile(double power) {
            super(ChedWeapon.this, (int) (power * ChedWeaponInfo.MAX_VELOCITY), EntityCondition.enemy(combatUser));
            this.power = power;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            ChedWeaponInfo.Effects.HIT.apply(power).play(location);
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
                ChedWeaponInfo.Effects.HIT_BLOCK.apply(hitBlock, power).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                if (target.getDamageModule().damage(this, power * ChedWeaponInfo.MAX_DAMAGE, DamageType.NORMAL, location, isCrit, true)
                        && target.isGoalTarget() && isCrit)
                    combatUser.addScore(ChedWeaponInfo.CRIT_SCORE.multiplyScore(power));

                return false;
            });
        }
    }
}
