package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter
public final class NeaceWeapon extends AbstractWeapon implements FullAuto {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "TargetResetDelay";
    /** 연사 모듈 */
    @NonNull
    private final FullAutoModule fullAutoModule;
    /** 시야 대상 */
    private Healable sightTarget = null;
    /** 현재 사용 대상 */
    private Healable target = null;

    NeaceWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceWeaponInfo.getInstance());
        fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceWeaponInfo.COOLDOWN;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case PERIODIC_1: {
                new NeaceTarget().shoot();

                if (target != null) {
                    if (CooldownUtil.getCooldown(this, COOLDOWN_ID) == 0)
                        target = null;
                } else if (sightTarget != null)
                    GlowUtil.setGlowing(sightTarget.getEntity(), ChatColor.GREEN, combatUser.getEntity(), 3);

                break;
            }
            case LEFT_CLICK: {
                setCooldown();
                combatUser.playMeleeAttackAnimation(-3, 6, true);

                new NeaceWeaponProjectile().shoot();

                SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_WEAPON_USE, combatUser.getEntity().getLocation());

                break;
            }
            case RIGHT_CLICK: {
                if (target == null) {
                    target = sightTarget;
                    if (sightTarget == null)
                        return;
                }

                CooldownUtil.setCooldown(this, COOLDOWN_ID, 4);
                SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_WEAPON_USE_HEAL, combatUser.getEntity().getLocation());
                combatUser.getUser().sendTitle("", "§a" + TextIcon.HEAL + " §f치유 중 : §e" + target.getName(), 0, 5, 5);
                target.getDamageModule().heal(combatUser, NeaceWeaponInfo.HEAL.HEAL_PER_SECOND / 20, true);

                Location location = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0.2, -0.4, 0);
                for (Location loc : LocationUtil.getLine(location, target.getEntity().getLocation().add(0, 1, 0), 0.8)) {
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1, 0, 0, 0,
                            255, 255, 140);
                }

                break;
            }
        }
    }

    private final class NeaceTarget extends Hitscan {
        private Healable target = null;

        private NeaceTarget() {
            super(combatUser, HitscanOption.builder().size(0.8).maxDistance(NeaceWeaponInfo.HEAL.MAX_DISTANCE)
                    .condition(combatEntity -> combatEntity instanceof Healable && !combatEntity.isEnemy(NeaceWeapon.this.combatUser) &&
                            combatEntity != NeaceWeapon.this.combatUser).build());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            this.target = (Healable) target;
            return false;
        }

        @Override
        protected void onDestroy() {
            sightTarget = target;
        }
    }

    private final class NeaceWeaponProjectile extends Projectile {
        private NeaceWeaponProjectile() {
            super(combatUser, NeaceWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10).size(NeaceWeaponInfo.SIZE)
                    .maxDistance(NeaceWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
            Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc, 1, 0.05, 0.05, 0.05,
                    255, 255, 235);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 3, 0.1, 0.1, 0.1,
                    255, 255, 200);
        }

        @Override
        protected void onHit() {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, location, 15, 0.2, 0.2, 0.2,
                    255, 255, 200);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(combatUser, NeaceWeaponInfo.DAMAGE, DamageType.NORMAL, location, isCrit, true);
            return false;
        }
    }
}
