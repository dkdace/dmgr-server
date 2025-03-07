package com.dace.dmgr.combat.combatant.ched.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.skill.StackableSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;

@Getter
public final class ChedA1 extends StackableSkill {
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public ChedA1(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return ChedA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public long getDefaultStackCooldown() {
        return ChedA1Info.STACK_COOLDOWN;
    }

    @Override
    public int getMaxStack() {
        return ChedA1Info.MAX_STACK;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();
            combatUser.getWeapon().onCancelled();
            combatUser.setGlobalCooldown(Timespan.ofTicks(ChedA1Info.READY_DURATION));

            ChedA1Info.SOUND.USE.play(combatUser.getLocation());

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                isEnabled = true;
                combatUser.getWeapon().setGlowing(true);
                combatUser.getWeapon().setMaterial(WeaponInfo.MATERIAL);
                combatUser.getWeapon().setDurability(ChedWeaponInfo.RESOURCE.FIRE);

                TaskUtil.addTask(taskRunner, new IntervalTask(i -> !isDurationFinished(), this::onCancelled, 1));
            }, ChedA1Info.READY_DURATION));
        } else
            onCancelled();
    }

    @Override
    public boolean isCancellable() {
        return !isEnabled && !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        isEnabled = false;
        combatUser.getWeapon().setGlowing(false);
        combatUser.getWeapon().setMaterial(Material.BOW);
        combatUser.getWeapon().setDurability(ChedWeaponInfo.RESOURCE.DEFAULT);
    }

    /**
     * 불화살 투사체를 발사한다.
     */
    void shoot() {
        addStack(-1);
        if (getStack() <= 0)
            setDuration(0);

        new ChedA1Projectile().shot();

        ChedA1Info.SOUND.SHOOT.play(combatUser.getLocation());
    }

    /**
     * 화염 상태 효과 클래스.
     */
    private static final class ChedA1Burning extends Burning {
        private static final ChedA1Burning instance = new ChedA1Burning();

        private ChedA1Burning() {
            super(ChedA1Info.FIRE_DAMAGE_PER_SECOND, true);
        }
    }

    private final class ChedA1Projectile extends Projectile<Damageable> {
        private ChedA1Projectile() {
            super(combatUser, ChedA1Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser));
        }

        @Override
        protected void onHit(@NonNull Location location) {
            ChedWeaponInfo.SOUND.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(9, location -> {
                        Location loc = LocationUtil.getLocationFromOffset(location, 0.2, 0, 0);
                        ChedA1Info.PARTICLE.BULLET_TRAIL.play(loc);
                    }));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                CombatEffectUtil.playSmallHitBlockParticle(location, hitBlock, 1.5);
                ChedA1Info.PARTICLE.HIT_BLOCK.play(location);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                if (target.getDamageModule().damage(this, ChedA1Info.DAMAGE, DamageType.NORMAL, location, isCrit, true)) {
                    target.getStatusEffectModule().apply(ChedA1Burning.instance, shooter, Timespan.ofTicks(ChedA1Info.FIRE_DURATION));

                    if (target instanceof CombatUser)
                        ((CombatUser) shooter).addScore("불화살", ChedA1Info.DAMAGE_SCORE);
                }

                return false;
            });
        }
    }
}
