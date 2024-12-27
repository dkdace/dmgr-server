package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
        super(combatUser, NeaceWeaponInfo.getInstance());

        fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceWeaponInfo.COOLDOWN;
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
                combatUser.playMeleeAttackAnimation(-3, 6, true);

                new NeaceWeaponProjectile().shoot();

                NeaceWeaponInfo.SOUND.USE.play(combatUser.getEntity().getLocation());

                break;
            }
            case RIGHT_CLICK: {
                if (target == null) {
                    new NeaceWeaponTarget().shoot();

                    if (target == null)
                        return;
                }

                targetResetTimestamp = Timestamp.now().plus(Timespan.ofTicks(4));
                if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                    blockResetTimestamp = Timestamp.now().plus(Timespan.ofTicks(NeaceWeaponInfo.HEAL.BLOCK_RESET_DELAY));

                NeaceWeaponInfo.SOUND.USE_HEAL.play(combatUser.getEntity().getLocation());
                combatUser.getUser().sendTitle("", MessageFormat.format("{0} : {1}§e{2}",
                                (combatUser.getSkill(NeaceA2Info.getInstance()).isDurationFinished() ?
                                        "§a" + TextIcon.HEAL + " §f치유 중" : "§b" + TextIcon.DAMAGE_INCREASE + " §f강화 중"),
                                (target instanceof CombatUser && ((CombatUser) target).getCharacterType() != null ?
                                        ((CombatUser) target).getCharacterType().getCharacter().getIcon() + " " : ""),
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
        else if (!target.getStatusEffectModule().hasStatusEffect(NeaceA1.NeaceA1Mark.instance))
            target.getDamageModule().heal(combatUser, NeaceWeaponInfo.HEAL.HEAL_PER_SECOND / 20.0, true);

        Location location = combatUser.getArmLocation(true);
        for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.8))
            (isAmplifying ? NeaceWeaponInfo.PARTICLE.HIT_ENTITY_HEAL_AMPLIFY : NeaceWeaponInfo.PARTICLE.HIT_ENTITY_HEAL).play(loc);
    }

    private final class NeaceWeaponTarget extends Target {
        private NeaceWeaponTarget() {
            super(combatUser, NeaceWeaponInfo.HEAL.MAX_DISTANCE, false,
                    combatEntity -> Neace.getTargetedActionCondition(NeaceWeapon.this.combatUser, combatEntity));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            NeaceWeapon.this.target = (Healable) target;
            blockResetTimestamp = Timestamp.now().plus(Timespan.ofTicks(NeaceWeaponInfo.HEAL.BLOCK_RESET_DELAY));

            TaskUtil.addTask(NeaceWeapon.this, new IntervalTask(i -> target.canBeTargeted() && !target.isDisposed()
                    && targetResetTimestamp.isAfter(Timestamp.now()) && blockResetTimestamp.isAfter(Timestamp.now())
                    && combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) <= NeaceWeaponInfo.HEAL.MAX_DISTANCE,
                    () -> NeaceWeapon.this.target = null, 1));
        }
    }

    private final class NeaceWeaponProjectile extends Projectile {
        private NeaceWeaponProjectile() {
            super(combatUser, NeaceWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10).size(NeaceWeaponInfo.SIZE)
                    .maxDistance(NeaceWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, -0.2, 0);
            NeaceWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc);
        }

        @Override
        protected void onHit() {
            NeaceWeaponInfo.PARTICLE.HIT.play(getLocation());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, NeaceWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), isCrit, true);
            return false;
        }
    }
}
