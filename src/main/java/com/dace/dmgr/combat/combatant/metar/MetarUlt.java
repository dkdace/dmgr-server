package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

import java.util.function.Consumer;

@Getter
public final class MetarUlt extends UltimateSkill implements HasBonusScore {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-100);
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public MetarUlt(@NonNull CombatUser combatUser) {
        super(combatUser, MetarUltInfo.getInstance(), Timespan.MAX, MetarUltInfo.COST);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", MetarUltInfo.ASSIST_SCORE);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();

        combatUser.getActionManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(MetarUltInfo.READY_DURATION);
        combatUser.getMoveModule().addModifier(MODIFIER);

        addActionTask(new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    0, 0, 0.3);
            MetarUltInfo.Effects.USE_TICK.apply(i).play(loc);
        }, () -> {
            cancel();

            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0);
            MetarUltInfo.Effects.USE_READY.play(loc);

            addTask(new IntervalTask(i -> {
                if (i % 4 == 0)
                    new MetarUltHitscan().shot(loc, loc.getDirection());

                MetarUltInfo.Effects.playTick(i, loc);
            }, 1, MetarUltInfo.DURATION.toTicks()));
        }, 1, MetarUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().removeModifier(MODIFIER);
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    private final class MetarUltHitscan extends Hitscan<Damageable> {
        private MetarUltHitscan() {
            super(combatUser, EntityCondition.enemy(combatUser), Option.builder().size(MetarUltInfo.SIZE).build());
        }

        @Override
        protected boolean canBeRemoved() {
            return false;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createPeriodIntervalHandler(15, new Consumer<Location>() {
                        private long i = 0;

                        @Override
                        public void accept(Location location) {
                            MetarUltInfo.Effects.playBulletTrail(i++, location);
                        }
                    }));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(combatUser, MetarUltInfo.DAMAGE_PER_SECOND * 4 / 20.0, DamageType.NORMAL, null,
                        false, false) && target.isGoalTarget())
                    bonusScoreModule.addTarget(target, Timespan.ofTicks(10));

                return true;
            };
        }
    }
}
