package com.dace.dmgr.combat.combatant.neace.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

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

    public NeaceWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceWeaponInfo.getInstance(), NeaceWeaponInfo.COOLDOWN);

        fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(NeaceUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                setCooldown();
                combatUser.playMeleeAttackAnimation(-3, Timespan.ofTicks(6), MainHand.RIGHT);

                new NeaceWeaponProjectile().shot();

                NeaceWeaponInfo.SOUND.USE.play(combatUser.getLocation());

                break;
            }
            case RIGHT_CLICK: {
                if (target == null) {
                    new NeaceWeaponTarget().shot();

                    if (target == null)
                        return;
                }

                targetResetTimestamp = Timestamp.now().plus(Timespan.ofTicks(4));
                if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                    blockResetTimestamp = Timestamp.now().plus(NeaceWeaponInfo.HEAL.BLOCK_RESET_DELAY);

                NeaceWeaponInfo.SOUND.USE_HEAL.play(combatUser.getLocation());
                combatUser.getUser().sendTitle("", MessageFormat.format("{0} : {1}§e{2}",
                                (combatUser.getSkill(NeaceA2Info.getInstance()).isDurationFinished() ?
                                        "§a" + TextIcon.HEAL + " §f치유 중" : "§b" + TextIcon.DAMAGE_INCREASE + " §f강화 중"),
                                target instanceof CombatUser ? ((CombatUser) target).getCombatantType().getCombatant().getIcon() + " " : "",
                                target.getName()),
                        Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(5));

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
        NeaceA2 skill2 = combatUser.getSkill(NeaceA2Info.getInstance());
        boolean isAmplifying = !skill2.isDurationFinished();

        if (isAmplifying)
            skill2.amplifyTarget(target);
        else if (!target.getStatusEffectModule().has(ValueStatusEffect.Type.HEALING_MARK))
            target.getDamageModule().heal(combatUser, NeaceWeaponInfo.HEAL.HEAL_PER_SECOND / 20.0, true);

        Location location = combatUser.getArmLocation(MainHand.RIGHT);
        for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.8))
            (isAmplifying ? NeaceWeaponInfo.PARTICLE.HIT_ENTITY_HEAL_AMPLIFY : NeaceWeaponInfo.PARTICLE.HIT_ENTITY_HEAL).play(loc);
    }

    private final class NeaceWeaponTarget extends Target<Healable> {
        private NeaceWeaponTarget() {
            super(combatUser, NeaceWeaponInfo.HEAL.MAX_DISTANCE, false, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            NeaceWeapon.this.target = target;
            blockResetTimestamp = Timestamp.now().plus(NeaceWeaponInfo.HEAL.BLOCK_RESET_DELAY);

            addTask(new IntervalTask(i -> target.canBeTargeted() && !target.isDisposed()
                    && targetResetTimestamp.isAfter(Timestamp.now()) && blockResetTimestamp.isAfter(Timestamp.now())
                    && combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) <= NeaceWeaponInfo.HEAL.MAX_DISTANCE,
                    () -> NeaceWeapon.this.target = null, 1));
        }
    }

    private final class NeaceWeaponProjectile extends Projectile<Damageable> {
        private NeaceWeaponProjectile() {
            super(combatUser, NeaceWeaponInfo.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(NeaceWeaponInfo.SIZE).maxDistance(NeaceWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            NeaceWeaponInfo.PARTICLE.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                NeaceWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc);
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
