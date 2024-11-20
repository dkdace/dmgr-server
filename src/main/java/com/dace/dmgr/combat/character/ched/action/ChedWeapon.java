package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

@Setter
public final class ChedWeapon extends AbstractWeapon {
    /** 활 충전량 */
    private double power;

    public ChedWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, ChedWeaponInfo.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return ChedWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                ChedA1 skill1 = combatUser.getSkill(ChedA1Info.getInstance());

                if (skill1.isEnabled()) {
                    setCooldown(ChedA1Info.COOLDOWN);

                    skill1.shoot();
                } else {
                    setCooldown();
                    setCanShoot(true);

                    if (combatUser.getEntity().isHandRaised()) {
                        combatUser.getWeapon().setVisible(false);
                        combatUser.getWeapon().setVisible(true);
                    }

                    SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_WEAPON_CHARGE, combatUser.getEntity().getLocation());
                }

                break;
            }
            case PERIODIC_1: {
                new ChedWeaponProjectile(power).shoot();
                setCanShoot(false);

                SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_WEAPON_USE, combatUser.getEntity().getLocation(), power + 0.5, power * 0.3);

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

    private final class ChedWeaponProjectile extends Projectile {
        private final double power;

        private ChedWeaponProjectile(double power) {
            super(combatUser, (int) (power * ChedWeaponInfo.MAX_VELOCITY), ProjectileOption.builder().trailInterval(9).hasGravity(true)
                    .condition(combatUser::isEnemy).build());
            this.power = power;
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, 0, 0);
            ParticleUtil.play(Particle.CRIT, loc, 1, 0, 0, 0, 0, combatUser.getEntity());
        }

        @Override
        protected void onHit() {
            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_WEAPON_HIT, getLocation(), power + 0.5);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            CombatEffectUtil.playBlockHitSound(getLocation(), hitBlock, power);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), getLocation(),
                    (int) (power * 5), 0, 0, 0, 0.1);
            ParticleUtil.play(Particle.TOWN_AURA, getLocation(), (int) (power * 15), 0, 0, 0, 0);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, power * ChedWeaponInfo.MAX_DAMAGE, DamageType.NORMAL, getLocation(),
                    isCrit, true) && target instanceof CombatUser && isCrit)
                combatUser.addScore("치명타", power * ChedWeaponInfo.CRIT_SCORE);
            return false;
        }
    }
}
