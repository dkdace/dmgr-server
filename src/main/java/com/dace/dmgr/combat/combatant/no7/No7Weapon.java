package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.Shotgun;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

public final class No7Weapon extends AbstractWeapon implements FullAuto {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-No7WeaponInfo.SLOW);

    /** 연사 모듈 */
    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;
    /** 이동속도 감소 타임스탬프 */
    private Timestamp slowTimestamp = Timestamp.now();

    public No7Weapon(@NonNull CombatUser combatUser) {
        super(combatUser, No7WeaponInfo.getInstance(), Timespan.ZERO);

        this.fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, No7WeaponInfo.FIRE_RATE);

        addOnReset(() -> combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER));
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK};
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new No7WeaponShotgun().shot();

        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        if (slowTimestamp.isBefore(Timestamp.now())) {
            slowTimestamp = Timestamp.now().plus(No7WeaponInfo.SLOW_DURATION);
            addTask(new IntervalTask(i -> slowTimestamp.isAfter(Timestamp.now()), () ->
                    combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER), 1));
        } else
            slowTimestamp = Timestamp.now().plus(No7WeaponInfo.SLOW_DURATION);

        No7WeaponInfo.Sounds.USE.play(combatUser.getLocation());
    }

    /**
     * 달리기 가능 여부를 확인한다.
     *
     * @return 달리기 가능 여부
     */
    boolean canSprint() {
        return slowTimestamp.isBefore(Timestamp.now());
    }

    private final class No7WeaponShotgun extends Shotgun<Damageable> {
        private No7WeaponShotgun() {
            super(No7WeaponInfo.PELLET_AMOUNT, No7WeaponInfo.SPREAD);
        }

        @Override
        @NonNull
        protected Bullet<Damageable> getBullet(int i) {
            return new No7WeaponHitscan(i == 0);
        }
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
                No7WeaponInfo.Particles.BULLET_TRAIL.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playSmallHitBlockParticle(location, hitBlock, 1);

                if (isFirst) {
                    CombatEffectUtil.BULLET_HIT_BLOCK_SOUND.play(location);
                    CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                }

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                double damage = CombatUtil.getDistantDamage(No7WeaponInfo.DAMAGE, getTravelDistance(), No7WeaponInfo.DISTANCE / 2.0);

                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, false, true);
                return false;
            };
        }
    }
}
