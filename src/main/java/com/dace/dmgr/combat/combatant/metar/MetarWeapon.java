package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.handler.DropHandler;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.ability.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

public final class MetarWeapon extends AbstractWeapon implements Reloadable, FullAuto, DropHandler {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-MetarWeaponInfo.SLOW);

    /** 재장전 모듈 */
    @NonNull
    @Getter
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;
    /** 이동속도 감소 타임스탬프 */
    private Timestamp slowTimestamp = Timestamp.now();
    /** 발사 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public MetarWeapon(@NonNull CombatUser combatUser, @NonNull MetarWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, Timespan.ZERO);

        this.reloadModule = new ReloadModule(this);
        this.fullAutoModule = new FullAutoModule(this);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(10, StringFormUtil.PROGRESS_DEFAULT_SYMBOL).build();
    }

    @Override
    public void onRightClick() {
        if (!reloadModule.consume(1))
            return;

        isOpposite = !isOpposite;

        new MetarWeaponProjectile(isOpposite).shot(VectorUtil.getSpreadedVector(combatUser.getLocation().getDirection(), MetarWeaponInfo.SPREAD));

        combatUser.getMoveModule().addModifier(MODIFIER);

        if (slowTimestamp.isBefore(Timestamp.now())) {
            slowTimestamp = Timestamp.now().plus(MetarWeaponInfo.SLOW_DURATION);
            addTask(new IntervalTask(i -> slowTimestamp.isAfter(Timestamp.now()), () ->
                    combatUser.getMoveModule().removeModifier(MODIFIER), 1));
        } else
            slowTimestamp = Timestamp.now().plus(MetarWeaponInfo.SLOW_DURATION);

        MetarWeaponInfo.Effects.USE.play(combatUser.getLocation());
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
    }

    /**
     * 달리기 가능 여부를 확인한다.
     *
     * @return 달리기 가능 여부
     */
    boolean canSprint() {
        return slowTimestamp.isBefore(Timestamp.now());
    }

    @Override
    public int getCapacity() {
        return MetarWeaponInfo.CAPACITY;
    }

    @Override
    @NonNull
    public Timespan getReloadDuration() {
        return MetarWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public void onAmmoEmpty() {
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        MetarWeaponInfo.Effects.RELOAD.apply(i).play(combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    @NonNull
    public FireRate getFireRate() {
        return MetarWeaponInfo.FIRE_RATE;
    }

    private final class MetarWeaponProjectile extends Projectile<Damageable> {
        private final boolean isOpposite;

        private MetarWeaponProjectile(boolean isOpposite) {
            super(MetarWeapon.this, MetarWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().maxDistance(MetarWeaponInfo.DISTANCE).build());
            this.isOpposite = isOpposite;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            MetarWeaponInfo.Effects.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(13, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, isOpposite ? -0.25 : 0.25, -0.2, 0);
                MetarWeaponInfo.Effects.BULLET_TRAIL.play(loc);
            });
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
                target.getDamageModule().damage(combatUser, MetarWeaponInfo.DISTANT_DAMAGE.getDamage(getTravelDistance()), DamageType.NORMAL, location,
                        false, true);
                return false;
            };
        }
    }
}
