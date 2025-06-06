package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class No7Ult extends UltimateSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 기절 상태 효과 */
    private final Stun stun;

    public No7Ult(@NonNull CombatUser combatUser) {
        super(combatUser, No7UltInfo.getInstance(), Timespan.MAX, No7UltInfo.COST);

        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", No7UltInfo.ASSIST_SCORE);
        this.stun = new Stun(combatUser);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ActionManager actionManager = combatUser.getActionManager();
        return super.canUse(actionKey) && isDurationFinished() && actionManager.getSkill(No7A2Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(No7A3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();

        combatUser.getActionManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(No7UltInfo.READY_DURATION);

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation();
            No7UltInfo.Effects.USE_TICK_SOUND.apply(i).play(loc);

            Location loc2 = combatUser.getLocation().add(0, 1, 0);
            new No7UltEffect(false, 1).shot(loc2, VectorUtil.getRandomVector());

            No7UltInfo.Effects.USE_TICK_PARTICLE.play(loc2);
        }, () -> {
            cancel();

            Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
            new No7UltArea().emit(loc);

            Location loc2 = loc.add(0, 1, 0);
            No7UltInfo.Effects.USE_READY.play(loc2);

            addActionTask(new IntervalTask(i -> {
                for (int j = 0; j < 6; j++)
                    new No7UltEffect(true, 2).shot(loc2, VectorUtil.getRandomVector());
            }, 1, 4));
        }, 1, No7UltInfo.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    private final class No7UltEffect extends Hitscan<CombatEntity> {
        private final boolean isReady;
        private final int count;

        private No7UltEffect(boolean isReady, int count) {
            super(combatUser, EntityCondition.all(), Option.builder().maxDistance(isReady ? 3 : 1).build());

            this.isReady = isReady;
            this.count = count;
        }

        @Override
        protected boolean canBeRemoved() {
            return false;
        }

        @Override
        protected void onDestroy(@NonNull Location location, boolean isForce) {
            if (count > 0) {
                for (int i = 0; i < 2; i++)
                    new No7UltEffect(isReady, count - 1).shot(location, VectorUtil.getSpreadedVector(getVelocity(), 100));
            }
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(2, No7UltInfo.Effects.TICK::play);
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<CombatEntity> getHitEntityHandler() {
            return (location, target) -> true;
        }
    }

    private final class No7UltArea extends Area<Damageable> {
        private No7UltArea() {
            super(combatUser, No7UltInfo.RADIUS, EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, No7UltInfo.DAMAGE, DamageType.NORMAL, null, false, false)) {
                target.getStatusEffectModule().apply(stun, No7UltInfo.STUN_DURATION);

                if (target.isGoalTarget()) {
                    combatUser.addScore("적 기절시킴", No7UltInfo.DAMAGE_SCORE);
                    bonusScoreModule.addTarget(target, No7UltInfo.STUN_DURATION);
                }
            }

            return !(target instanceof Barrier);
        }
    }
}
