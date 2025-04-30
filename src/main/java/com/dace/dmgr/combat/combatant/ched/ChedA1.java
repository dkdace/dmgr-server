package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.skill.StackableSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public final class ChedA1 extends StackableSkill {
    /** 화염 상태 효과 */
    private final Burning burning;
    /** 활성화 완료 여부 */
    @Getter(AccessLevel.PACKAGE)
    private boolean isEnabled = false;

    public ChedA1(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA1Info.getInstance(), ChedA1Info.COOLDOWN, ChedA1Info.STACK_COOLDOWN, Timespan.MAX, ChedA1Info.MAX_STACK, 0);
        this.burning = new Burning(combatUser, ChedA1Info.FIRE_DAMAGE_PER_SECOND, true);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished() || !isEnabled())
            return null;

        return ChedA1Info.getInstance() + ActionBarStringUtil.getKeyInfo(this, "해제");
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getActionManager().getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            forceCancel();
            return;
        }

        setDuration();
        combatUser.setGlobalCooldown(ChedA1Info.READY_DURATION);

        Weapon weapon = combatUser.getActionManager().getWeapon();
        weapon.cancel();

        ChedA1Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            isEnabled = true;

            weapon.setGlowing(true);
            weapon.setMaterial(WeaponInfo.MATERIAL);
            weapon.setDurability(ChedWeaponInfo.Resource.FIRE);
        }, ChedA1Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        isEnabled = false;

        setDuration(Timespan.ZERO);

        Weapon weapon = combatUser.getActionManager().getWeapon();
        weapon.setGlowing(false);
        weapon.setMaterial(Material.BOW);
        weapon.setDurability(ChedWeaponInfo.Resource.DEFAULT);
    }

    /**
     * 불화살 투사체를 발사한다.
     */
    void shot() {
        addStack(-1);
        if (getStack() <= 0)
            forceCancel();

        new ChedA1Projectile().shot();

        ChedA1Info.Sounds.SHOOT.play(combatUser.getLocation());
    }

    private final class ChedA1Projectile extends Projectile<Damageable> {
        private ChedA1Projectile() {
            super(ChedA1.this, ChedA1Info.VELOCITY, EntityCondition.enemy(combatUser));
        }

        @Override
        protected void onHit(@NonNull Location location) {
            ChedWeaponInfo.Sounds.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(9, location -> {
                        Location loc = LocationUtil.getLocationFromOffset(location, 0.2, 0, 0);
                        ChedA1Info.Particles.BULLET_TRAIL.play(loc);
                    }));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                CombatEffectUtil.playSmallHitBlockParticle(location, hitBlock, 1.5);
                ChedA1Info.Particles.HIT_BLOCK.play(location);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                if (target.getDamageModule().damage(this, ChedA1Info.DAMAGE, DamageType.NORMAL, location, isCrit, true)) {
                    target.getStatusEffectModule().apply(burning, ChedA1Info.FIRE_DURATION);

                    if (target.isGoalTarget())
                        combatUser.addScore("불화살", ChedA1Info.DAMAGE_SCORE);
                }

                return false;
            });
        }
    }
}
