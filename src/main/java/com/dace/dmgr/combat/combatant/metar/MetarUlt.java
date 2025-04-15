package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
public final class MetarUlt extends UltimateSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-100);
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

        combatUser.getWeapon().cancel();
        combatUser.setGlobalCooldown(MetarUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        addActionTask(new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    0, 0, 0.3);

            MetarUltInfo.SOUND.USE_TICK.play(loc, 1, i / 39.0);
            MetarUltInfo.PARTICLE.USE_TICK.play(loc);
        }, () -> {
            cancel();

            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0);

            MetarUltInfo.SOUND.USE_READY.play(loc);
            MetarUltInfo.PARTICLE.USE_READY.play(loc);

            addTask(new IntervalTask(i -> {
                if (i % 4 == 0) {
                    new MetarUltHitscan().shot(loc, loc.getDirection());

                    MetarUltInfo.SOUND.TICK.play(loc);
                }

                MetarUltInfo.PARTICLE.TICK.play(loc);
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
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    private final class MetarUltHitscan extends Hitscan<Damageable> {
        private MetarUltHitscan() {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser), Option.builder().size(MetarUltInfo.SIZE).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createPeriodIntervalHandler(16, MetarUltInfo.PARTICLE.BULLET_TRAIL_CORE::play))
                    .next(createPeriodIntervalHandler(42, location -> {
                        Vector vector = VectorUtil.getYawAxis(location).multiply(2);
                        Vector axis = VectorUtil.getRollAxis(location);

                        for (int i = 0; i < 16; i++) {
                            int angle = 360 / 16 * i;
                            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

                            MetarUltInfo.PARTICLE.BULLET_TRAIL_DECO.play(location.clone().add(vec));
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
                        false, false) && target instanceof CombatUser)
                    bonusScoreModule.addTarget((CombatUser) target, Timespan.ofTicks(10));

                return true;
            };
        }
    }
}
