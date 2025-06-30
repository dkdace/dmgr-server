package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.ability.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;

public final class NeaceWeapon extends AbstractWeapon implements FullAuto {
    /** 연사 모듈 */
    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;

    /** 대상 초기화 타임스탬프 */
    private Timestamp targetResetTimestamp = Timestamp.now();
    /** 대상 위치 통과 불가 시 초기화 타임스탬프 */
    private Timestamp blockResetTimestamp = Timestamp.now();
    /** 현재 사용 대상 */
    @Nullable
    private Healable target = null;

    public NeaceWeapon(@NonNull CombatUser combatUser, @NonNull NeaceWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, NeaceWeaponInfo.COOLDOWN);
        this.fullAutoModule = new FullAutoModule(this);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getAbilityManager().getAbility(NeaceUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                setCooldown();
                combatUser.playMeleeAttackAnimation(-3, Timespan.ofTicks(6), MainHand.RIGHT);

                new NeaceWeaponLProjectile().shot();

                NeaceWeaponInfo.Effects.USE.play(combatUser.getLocation());

                break;
            }
            case RIGHT_CLICK: {
                if (target != null && (!target.canBeTargeted() || target.isRemoved() || targetResetTimestamp.isBefore(Timestamp.now())
                        || blockResetTimestamp.isBefore(Timestamp.now())
                        || combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) > NeaceWeaponInfo.Heal.MAX_DISTANCE))
                    target = null;

                if (target == null)
                    new NeaceWeaponRTarget().shot();
                if (target == null)
                    return;

                targetResetTimestamp = Timestamp.now().plus(Timespan.ofTicks(4));
                if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                    blockResetTimestamp = Timestamp.now().plus(NeaceWeaponInfo.Heal.BLOCK_RESET_DELAY);

                String title = MessageFormat.format("{0} : {1}§e{2}",
                        (combatUser.getAbilityManager().getAbility(NeaceA2Info.getInstance()).isDurationFinished()
                                ? MessageFormat.format("§a{0} §f치유 중", TextIcon.HEAL)
                                : MessageFormat.format("§b{0} §f강화 중", TextIcon.DAMAGE_INCREASE)),
                        (target instanceof CombatUser ? ((CombatUser) target).getCombatantType().getCombatant().getIcon() + " " : ""),
                        target.getName());
                combatUser.getUser().sendTitle("", title, Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(5));

                NeaceWeaponInfo.Effects.HEAL_USE_SOUND.play(combatUser.getLocation());

                healTarget(target);
                break;
            }
            default:
                break;
        }
    }

    /**
     * 대상에게 치유 광선을 사용한다.
     *
     * @param target 치유 대상
     */
    void healTarget(@NonNull Healable target) {
        NeaceA2 skill2 = combatUser.getAbilityManager().getAbility(NeaceA2Info.getInstance());
        boolean isAmplifying = !skill2.isDurationFinished();

        if (isAmplifying)
            skill2.amplifyTarget(target);
        else
            target.getHealModule().heal(combatUser, NeaceWeaponInfo.Heal.HEAL_PER_SECOND / 20.0, true);

        NeaceWeaponInfo.Effects.playHealUse(combatUser.getArmLocation(MainHand.RIGHT), target.getCenterLocation(), isAmplifying);
    }

    /**
     * 대상이 치유를 받고 있는지 확인한다.
     *
     * @param target 확인할 대상
     * @return 치유를 받고 있으면 {@code true} 반환
     */
    boolean isHealing(@NonNull Healable target) {
        return combatUser.getAbilityManager().getAbility(NeaceA2Info.getInstance()).isDurationFinished() && this.target == target
                && targetResetTimestamp.isAfter(Timestamp.now());
    }

    @Override
    @NonNull
    public ActionKey getFullAutoKey() {
        return ActionKey.RIGHT_CLICK;
    }

    @Override
    @NonNull
    public FireRate getFireRate() {
        return FireRate.RPM_1200;
    }

    private final class NeaceWeaponRTarget extends Target<Healable> {
        private NeaceWeaponRTarget() {
            super(combatUser, NeaceWeaponInfo.Heal.MAX_DISTANCE, EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            NeaceWeapon.this.target = target;
        }
    }

    private final class NeaceWeaponLProjectile extends Projectile<Damageable> {
        private NeaceWeaponLProjectile() {
            super(NeaceWeapon.this, NeaceWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(NeaceWeaponInfo.SIZE).maxDistance(NeaceWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            NeaceWeaponInfo.Effects.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                NeaceWeaponInfo.Effects.BULLET_TRAIL.play(loc);
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
            return createCritHitEntityHandler((location, hitTarget, isCrit) -> {
                hitTarget.getDamageModule().damage(this, NeaceWeaponInfo.DAMAGE, DamageType.NORMAL, location, isCrit, true);
                return false;
            });
        }
    }
}
