package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.handler.DropHandler;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.handler.RightClickHandler;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.Aimable;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.module.AimModule;
import com.dace.dmgr.combat.ability.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

public final class PalasWeapon extends AbstractWeapon implements Reloadable, Aimable, LeftClickHandler, RightClickHandler, DropHandler {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-PalasWeaponInfo.AIM_SLOW);

    /** 재장전 모듈 */
    @NonNull
    @Getter
    private final ReloadModule reloadModule;
    /** 정조준 모듈 */
    @NonNull
    @Getter
    private final AimModule aimModule;
    /** 사용 후 쿨타임 진행 여부 */
    private boolean isActionCooldown = true;

    public PalasWeapon(@NonNull CombatUser combatUser, @NonNull PalasWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, PalasWeaponInfo.COOLDOWN);

        this.reloadModule = new ReloadModule(this);
        this.aimModule = new AimModule(this);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(getCapacity(), ActionBarDisplay.AMMO_BAR_BIG_SYMBOL)
                .suffix(isActionCooldown ? "§a■" : "§c□").build();
    }

    @Override
    public void onLeftClick() {
        if (!reloadModule.consume(0))
            return;
        if (!isActionCooldown) {
            action();
            return;
        }

        setCooldown();

        new PalasWeaponHitscan(aimModule.isAiming()).shot();
        new PalasWeaponHealHitscan(aimModule.isAiming()).shot();

        reloadModule.cancel();
        isActionCooldown = false;

        PalasWeaponInfo.RECOIL.send(combatUser);
        PalasWeaponInfo.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(this::action, getDefaultCooldown().toTicks()));
    }

    @Override
    public boolean isRightClickIgnoreCooldown() {
        return true;
    }

    @Override
    public void onRightClick() {
        if (aimModule.isAiming()) {
            cancel();
            return;
        }

        cancel();
        aimModule.toggleAim();
    }

    @Override
    public boolean isDropIgnoreCooldown() {
        return true;
    }

    @Override
    public void onDrop() {
        reloadModule.reload();
    }

    @Override
    protected void onCancelled() {
        reloadModule.cancel();
        aimModule.cancel();
    }

    /**
     * 사용 후 쿨타임 작업을 수행한다.
     */
    private void action() {
        setCooldown(Timespan.ZERO);
        setCooldown(PalasWeaponInfo.ACTION_COOLDOWN);

        reloadModule.cancel();

        addActionTask(new IntervalTask(i -> {
            PalasWeaponInfo.Effects.ACTION.apply(i).play(combatUser.getLocation());

            switch ((int) i) {
                case 1:
                    combatUser.addYawAndPitch(-0.25, 0.1);
                    break;
                case 2:
                    combatUser.addYawAndPitch(-0.1, 0.2);
                    break;
                case 5:
                    combatUser.addYawAndPitch(0.1, -0.2);
                    break;
                case 6:
                    combatUser.addYawAndPitch(0.25, -0.1);
                    break;
                default:
                    break;
            }
        }, () -> {
            isActionCooldown = true;
            reloadModule.consume(1);
        }, 1, PalasWeaponInfo.ACTION_COOLDOWN.toTicks()));
    }

    @Override
    public int getCapacity() {
        return PalasWeaponInfo.CAPACITY;
    }

    @Override
    @NonNull
    public Timespan getReloadDuration() {
        return PalasWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public void onAmmoEmpty() {
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        PalasWeaponInfo.Effects.RELOAD.apply(i).play(combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    @NonNull
    public ZoomLevel getZoomLevel() {
        return PalasWeaponInfo.ZOOM_LEVEL;
    }

    @Override
    public void onAimEnable() {
        combatUser.setGlobalCooldown(PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().addModifier(MODIFIER);

        PalasWeaponInfo.Effects.AIM_ON.play(combatUser.getLocation());
    }

    @Override
    public void onAimDisable() {
        combatUser.setGlobalCooldown(PalasWeaponInfo.AIM_DURATION);
        combatUser.getMoveModule().removeModifier(MODIFIER);

        PalasWeaponInfo.Effects.AIM_OFF.play(combatUser.getLocation());
    }

    private final class PalasWeaponHitscan extends Hitscan<Damageable> {
        private PalasWeaponHitscan(boolean isAiming) {
            super(combatUser, EntityCondition.enemy(combatUser).or(EntityCondition.team(combatUser).exclude(combatUser)),
                    (isAiming ? Option.builder() : Option.builder().maxDistance(PalasWeaponInfo.DISTANCE)).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(8, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, (aimModule.isAiming() ? 0 : 0.2), -0.2, 0);
                PalasWeaponInfo.Effects.BULLET_TRAIL.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.BULLET_HIT_EFFECT.apply(hitBlock).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.isEnemy(combatUser)) {
                    target.getDamageModule().damage(combatUser, PalasWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, true);

                    PalasWeaponInfo.Effects.HIT_ENTITY.play(location);
                }

                return false;
            };
        }
    }

    private final class PalasWeaponHealHitscan extends Hitscan<Damageable> {
        private PalasWeaponHealHitscan(boolean isAiming) {
            super(combatUser, EntityCondition.enemy(combatUser).or(EntityCondition.team(combatUser).exclude(combatUser)),
                    (isAiming ? Option.builder() : Option.builder().maxDistance(PalasWeaponInfo.DISTANCE)).size(PalasWeaponInfo.HEAL_SIZE).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return (location, i) -> true;
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target instanceof Healable && !target.isEnemy(combatUser)) {
                    combatUser.getAbilityManager().getAbility(PalasP1Info.getInstance()).use((Healable) target, PalasWeaponInfo.HEAL);

                    ((Healable) target).getHealModule().heal(combatUser, PalasWeaponInfo.HEAL, true);

                    PalasWeaponInfo.Effects.HIT_ENTITY.play(location);
                }

                return false;
            };
        }
    }
}
