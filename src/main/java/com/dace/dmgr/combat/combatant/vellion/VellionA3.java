package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.HealBlock;
import com.dace.dmgr.combat.entity.module.statuseffect.Silence;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

public final class VellionA3 extends ActiveSkill implements Confirmable, HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-VellionA3Info.READY_SLOW);

    /** 위치 확인 모듈 */
    @NonNull
    @Getter
    private final LocationConfirmModule confirmModule;
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 침묵 상태 효과 */
    private final Silence silence;

    public VellionA3(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA3Info.getInstance(), VellionA3Info.COOLDOWN, Timespan.MAX, 2);

        this.confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_3, VellionA3Info.MAX_DISTANCE);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", VellionA3Info.ASSIST_SCORE);
        this.silence = new Silence(combatUser);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3, ActionKey.LEFT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case SLOT_3: {
                confirmModule.toggleCheck();

                break;
            }
            case LEFT_CLICK: {
                onUse();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean isCancellable() {
        return confirmModule.isChecking() || !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        confirmModule.cancel();

        if (!isDurationFinished()) {
            setDuration(Timespan.ZERO);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        }
    }

    @Override
    public void onCheckEnable() {
        // 미사용
    }

    @Override
    public void onCheckTick(long i) {
        // 미사용
    }

    @Override
    public void onCheckDisable() {
        // 미사용
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 사용 시 실행할 작업.
     */
    private void onUse() {
        if (!confirmModule.isValid())
            return;

        setDuration();

        confirmModule.toggleCheck();
        combatUser.setGlobalCooldown(VellionA3Info.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        Location location = confirmModule.getCurrentLocation();

        VellionA3Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            VellionA3Info.PARTICLE.USE_TICK_CORE.play(location);
            for (Location loc2 : LocationUtil.getLine(combatUser.getArmLocation(MainHand.RIGHT), location, 0.7))
                VellionA3Info.PARTICLE.USE_TICK_DECO.play(loc2);
        }, () -> {
            cancel();

            Location loc = location.clone().add(0, 0.1, 0);

            VellionA3Info.SOUND.USE_READY.play(loc);

            addTask(new IntervalTask(i -> {
                if (i % 4 == 0)
                    new VellionA3Area().emit(loc);

                playTickEffect(loc, i);
            }, 1, VellionA3Info.DURATION.toTicks()));
        }, 1, VellionA3Info.READY_DURATION.toTicks()));
    }

    /**
     * 범위 표시 효과를 재생한다.
     *
     * @param location 사용 위치
     * @param i        인덱스
     */
    private void playTickEffect(@NonNull Location location, long i) {
        Location loc = location.clone();
        loc.setYaw(0);
        loc.setPitch(0);

        VellionA3Info.PARTICLE.TICK_CORE.play(loc);

        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * 3;
            double distance = index * 0.2 % 5;

            for (int k = 0; k < 12; k++) {
                angle += distance > 3 ? 360 / 4 : 360 / 6;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle);
                Location loc2 = loc.clone().add(vec.clone().multiply(distance));

                VellionA3Info.PARTICLE.TICK_DECO_1.play(loc2, vec.setY(0.4));
            }

            long angle2 = index * 44;
            double distance2 = index * 0.1 % 2.5;

            for (int k = 0; k < 3; k++) {
                angle2 += 360 / 3;
                Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle2);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle2 + 10.0);
                Vector dir = LocationUtil.getDirection(loc.clone().add(vec1), loc.clone().add(vec2));

                VellionA3Info.PARTICLE.TICK_DECO_2.play(loc.clone().add(vec1.clone().multiply(5)).add(0, distance2 * 0.5, 0),
                        dir.setY(distance2 * 0.1));
            }
        }
    }

    private final class VellionA3Area extends Area<Damageable> {
        private VellionA3Area() {
            super(combatUser, VellionA3Info.RADIUS, CombatUtil.EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, true)) {
                target.getStatusEffectModule().apply(HealBlock.getInstance(), Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(silence, Timespan.ofTicks(10));

                if (target.isGoalTarget()) {
                    combatUser.addScore("적 침묵", VellionA3Info.EFFECT_SCORE_PER_SECOND * 4 / 20.0);
                    bonusScoreModule.addTarget(target, Timespan.ofTicks(10));
                }
            }

            return true;
        }
    }
}
