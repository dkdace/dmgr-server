package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.function.LongConsumer;

public final class No7Ult extends UltimateSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 기절 상태 효과 */
    private final Stun stun;

    public No7Ult(@NonNull CombatUser combatUser, @NonNull No7UltInfo skillInfo) {
        super(combatUser, skillInfo, Timespan.MAX, No7UltInfo.COST);

        this.bonusScoreModule = new BonusScoreModule(this);
        this.stun = new Stun(combatUser);
    }

    @Override
    protected boolean canUse() {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse() && isDurationFinished() && abilityManager.getAbility(No7A2Info.getInstance()).isDurationFinished()
                && abilityManager.getAbility(No7A3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onSlot() {
        setDuration();

        combatUser.getAbilityManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(No7UltInfo.READY_DURATION);

        addActionTask(new IntervalTask(i -> No7UltInfo.Effects.playUseTick(i, combatUser.getLocation()), () -> {
            cancel();

            Location loc = combatUser.getLocation().add(0, 0.1, 0);
            new No7UltArea().emit(loc);

            No7UltInfo.Effects.playUseReady(combatUser.getLocation());

            addActionTask(new IntervalTask((LongConsumer) i -> No7UltInfo.Effects.playTick(combatUser.getLocation()), 1, 4));
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
    @NonNull
    public CombatScore getCombatScore() {
        return No7UltInfo.ASSIST_SCORE;
    }

    @Override
    public boolean isAssistMode() {
        return true;
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
                    combatUser.addScore(No7UltInfo.DAMAGE_SCORE);
                    bonusScoreModule.addTarget(target, No7UltInfo.STUN_DURATION);
                }
            }

            return !(target instanceof Barrier);
        }
    }
}
