package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatEntityRegistry;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

import java.util.function.LongConsumer;

@Getter
public final class ChedA3 extends ActiveSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-ChedA3Info.READY_SLOW);
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public ChedA3(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA3Info.getInstance(), ChedA3Info.COOLDOWN, Timespan.MAX, 2);
        this.bonusScoreModule = new BonusScoreModule(this, "탐지 보너스", ChedA3Info.KILL_SCORE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getActionManager().getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && isDurationFinished() && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.setGlobalCooldown(ChedA3Info.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        ChedWeapon weapon = (ChedWeapon) combatUser.getActionManager().getWeapon();
        weapon.cancel();
        weapon.setCanShoot(false);

        ChedA3Info.Effects.USE.play(combatUser.getLocation());

        long durationTicks = ChedA3Info.READY_DURATION.toTicks();

        addActionTask(new IntervalTask(i -> ChedA3Info.Effects.playUseTick(i, combatUser.getArmLocation(MainHand.RIGHT)), () -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new ChedA3Projectile().shot(loc);

            ChedA3Info.Effects.USE_READY.play(loc);

            addActionTask(new IntervalTask((LongConsumer) i -> ChedA3Info.Effects.playUseTick(i + durationTicks, loc), 1, durationTicks));
        }, 1, durationTicks));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    private final class ChedA3Projectile extends Projectile<Damageable> {
        private ChedA3Projectile() {
            super(ChedA3.this, ChedA3Info.VELOCITY, EntityCondition.enemy(combatUser).and(Damageable::isCreature),
                    Option.builder().size(ChedA3Info.SIZE).build());
        }

        @Override
        protected boolean canBeRemoved() {
            return false;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(18, ChedA3Info.Effects::playBulletTrail);
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> true;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, 0, DamageType.NORMAL, location, false, true)) {
                    CombatEntityRegistry.getCombatEntities(location.getWorld(), EntityCondition.team(combatUser).and(CombatUser.class::isInstance))
                            .forEach(teamTarget -> ((CombatUser) teamTarget).setGlowing(target, ChedA3Info.DETECT_DURATION));

                    if (target.isGoalTarget()) {
                        combatUser.addScore("적 탐지", ChedA3Info.DETECT_SCORE);
                        bonusScoreModule.addTarget(target, ChedA3Info.KILL_SCORE_TIME_LIMIT);
                    }
                }

                return true;
            };
        }
    }
}
