package com.dace.dmgr.combat.combatant.vellion.action;

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

@Getter
public final class VellionA3 extends ActiveSkill implements Confirmable, HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-VellionA3Info.READY_SLOW);

    /** 위치 확인 모듈 */
    @NonNull
    private final LocationConfirmModule confirmModule;
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public VellionA3(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA3Info.getInstance(), VellionA3Info.COOLDOWN, Timespan.MAX, 2);

        this.confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_3, VellionA3Info.MAX_DISTANCE);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", VellionA3Info.ASSIST_SCORE);
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
                combatUser.getWeapon().onCancelled();
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
    public void onCancelled() {
        super.onCancelled();

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

    /**
     * 사용 시 실행할 작업.
     */
    private void onUse() {
        if (!confirmModule.isValid())
            return;

        setDuration();
        combatUser.setGlobalCooldown(VellionA3Info.READY_DURATION);
        confirmModule.toggleCheck();
        combatUser.getWeapon().setCooldown(Timespan.ofTicks(1));
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        Location location = confirmModule.getCurrentLocation();

        VellionA3Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            for (Location loc2 : LocationUtil.getLine(loc, location, 0.7))
                VellionA3Info.PARTICLE.USE_TICK_DECO.play(loc2);
            VellionA3Info.PARTICLE.USE_TICK_CORE.play(location);
        }, () -> {
            onCancelled();
            onReady(location);
        }, 1, VellionA3Info.READY_DURATION.toTicks()));
    }

    /**
     * 시전 완료 시 실행할 작업.
     *
     * @param location 대상 위치
     */
    private void onReady(@NonNull Location location) {
        Location loc = location.clone().add(0, 0.1, 0);

        VellionA3Info.SOUND.USE_READY.play(loc);

        addTask(new IntervalTask(i -> {
            if (i % 4 == 0)
                new VellionA3Area().emit(loc);

            VellionA3Info.PARTICLE.TICK_CORE.play(loc);
            playTickEffect(i, loc);
        }, 1, VellionA3Info.DURATION.toTicks()));
    }

    /**
     * 범위 표시 효과를 재생한다.
     *
     * @param i        인덱스
     * @param location 사용 위치
     */
    private void playTickEffect(long i, @NonNull Location location) {
        location.setYaw(0);
        location.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(location);
        Vector axis = VectorUtil.getYawAxis(location);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * 3;
            double distance = index * 0.2 % 5;

            for (int k = 0; k < 12; k++) {
                angle += distance > 3 ? 90 : 60;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle);
                Location loc = location.clone().add(vec.clone().multiply(distance));

                VellionA3Info.PARTICLE.TICK_DECO_1.play(loc, vec.setY(0.4));
            }
            double distance2 = index * 0.1 % 2.5;
            long angle2 = index * 44;
            for (int k = 0; k < 3; k++) {
                angle2 += 120;

                Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle2);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle2 + 10.0);
                Vector dir = LocationUtil.getDirection(location.clone().add(vec1), location.clone().add(vec2));

                VellionA3Info.PARTICLE.TICK_DECO_2.play(location.clone().add(vec1.clone().multiply(5)).add(0, distance2 * 0.5, 0),
                        dir.setY(distance2 * 0.1));
            }
        }
    }

    @Override
    public boolean isAssistMode() {
        return true;
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
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().apply(HealBlock.getInstance(), combatUser, Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(Silence.getInstance(), combatUser, Timespan.ofTicks(10));

                if (target instanceof CombatUser) {
                    combatUser.addScore("적 침묵", (double) (VellionA3Info.EFFECT_SCORE_PER_SECOND * 4) / 20);
                    bonusScoreModule.addTarget((CombatUser) target, Timespan.ofTicks(10));
                }
            }

            return true;
        }
    }
}
