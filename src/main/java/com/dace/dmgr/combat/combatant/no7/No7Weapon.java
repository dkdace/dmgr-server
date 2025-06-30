package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.ability.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

import java.util.EnumSet;
import java.util.Set;

public final class No7Weapon extends AbstractWeapon implements FullAuto {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-No7WeaponInfo.SLOW);

    /** 연사 모듈 */
    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;
    /** 이동속도 감소 타임스탬프 */
    private Timestamp slowTimestamp = Timestamp.now();

    public No7Weapon(@NonNull CombatUser combatUser, @NonNull No7WeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, Timespan.ZERO);
        this.fullAutoModule = new FullAutoModule(this);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.RIGHT_CLICK);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getAbilityManager().getAbility(No7A2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        Bullet.shotgun(i -> new No7WeaponHitscan(i == 0), No7WeaponInfo.PELLET_AMOUNT, No7WeaponInfo.SPREAD);

        combatUser.getMoveModule().addModifier(MODIFIER);

        if (slowTimestamp.isBefore(Timestamp.now())) {
            slowTimestamp = Timestamp.now().plus(No7WeaponInfo.SLOW_DURATION);
            addTask(new IntervalTask(i -> slowTimestamp.isAfter(Timestamp.now()), () -> combatUser.getMoveModule().removeModifier(MODIFIER),
                    1));
        } else
            slowTimestamp = Timestamp.now().plus(No7WeaponInfo.SLOW_DURATION);

        No7WeaponInfo.Effects.USE.play(combatUser.getLocation());
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
    @NonNull
    public ActionKey getFullAutoKey() {
        return ActionKey.RIGHT_CLICK;
    }

    @Override
    @NonNull
    public FireRate getFireRate() {
        return No7WeaponInfo.FIRE_RATE;
    }

    private final class No7WeaponHitscan extends Hitscan<Damageable> {
        private final boolean isFirst;

        private No7WeaponHitscan(boolean isFirst) {
            super(combatUser, EntityCondition.enemy(combatUser), Option.builder().maxDistance(No7WeaponInfo.DISTANCE).build());
            this.isFirst = isFirst;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(14, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                No7WeaponInfo.Effects.BULLET_TRAIL.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                No7WeaponInfo.Effects.HIT_BLOCK_PARTICLE.apply(hitBlock).play(location);
                if (isFirst)
                    No7WeaponInfo.Effects.HIT_BLOCK_SOUND.apply(hitBlock).play(location);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                target.getDamageModule().damage(combatUser, No7WeaponInfo.DISTANT_DAMAGE.getDamage(getTravelDistance()), DamageType.NORMAL, location,
                        false, true);
                return false;
            };
        }
    }
}
