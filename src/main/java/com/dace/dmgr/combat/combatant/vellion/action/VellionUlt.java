package com.dace.dmgr.combat.combatant.vellion.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class VellionUlt extends UltimateSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-100);

    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public VellionUlt(@NonNull CombatUser combatUser) {
        super(combatUser, VellionUltInfo.getInstance(), VellionUltInfo.DURATION, VellionUltInfo.COST);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", VellionUltInfo.ASSIST_SCORE);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return (isDurationFinished() || !isEnabled) ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(Timespan.MAX);

        combatUser.setGlobalCooldown(VellionUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        combatUser.getSkill(VellionP1Info.getInstance()).cancel();

        VellionUltInfo.SOUND.USE.play(combatUser.getLocation());

        EffectManager effectManager = new EffectManager();

        addActionTask(new IntervalTask(i -> effectManager.playEffect(), () ->
                addActionTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(), this::onReady, 1)),
                1, VellionUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        isEnabled = false;

        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    @Override
    public boolean isCancellable() {
        return !isEnabled && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        isEnabled = true;

        setDuration();
        combatUser.getStatusEffectModule().apply(Invulnerable.getInstance(), combatUser, VellionUltInfo.DURATION);

        VellionUltInfo.SOUND.USE_READY.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            if (i % 4 == 0)
                new VellionUltArea().emit(combatUser.getEntity().getEyeLocation());

            playTickEffect(i);
        }, () -> {
            forceCancel();

            Location loc = combatUser.getEntity().getEyeLocation();
            new VellionUltExplodeArea().emit(loc);

            Location loc2 = loc.add(0, 1, 0);
            VellionUltInfo.SOUND.EXPLODE.play(loc2);
            VellionUltInfo.PARTICLE.EXPLODE.play(loc2);
        }, 1, VellionUltInfo.DURATION.toTicks()));
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        Location loc = combatUser.getLocation().add(0, 0.1, 0);
        loc.setYaw(0);
        loc.setPitch(0);

        VellionUltInfo.PARTICLE.TICK_CORE_1.play(loc.clone().add(0, 1, 0));
        if (i < 8)
            VellionUltInfo.PARTICLE.TICK_CORE_2.play(loc.clone().add(0, 1, 0));

        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (long j = (i >= 5 ? i - 5 : 0); j < i; j++) {
            long angle = j * (j > 30 ? -3 : 5);
            double distance = j * 0.16;

            for (int k = 0; k < 12; k++) {
                angle += 360 / 6;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle).multiply(distance);
                Location loc2 = loc.clone().add(vec);

                if (j > 0 && j % 10 == 0)
                    VellionUltInfo.PARTICLE.TICK_DECO_1.play(loc2.clone().add(0, 2.5, 0));
                else if (i > 20)
                    VellionUltInfo.PARTICLE.TICK_DECO_3.play(loc2);
                else
                    VellionUltInfo.PARTICLE.TICK_DECO_2.play(loc2, j / 49.0);
            }
        }

        long angle = i * 4;
        for (int j = 0; j < 8; j++) {
            angle += 360 / 4;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 4 ? angle : -angle).multiply(8);
            Location loc2 = loc.clone().add(vec);

            VellionUltInfo.PARTICLE.TICK_DECO_4.play(loc2);
            VellionUltInfo.PARTICLE.TICK_DECO_5.play(loc2.clone().add(0, 2, 0));
        }
    }

    /**
     * 둔화 상태 효과 클래스.
     */
    private static final class VellionUltSlow extends Slow {
        private static final VellionUltSlow instance = new VellionUltSlow();

        private VellionUltSlow() {
            super(VellionUltInfo.SLOW);
        }
    }

    /**
     * 효과를 재생하는 클래스.
     */
    @NoArgsConstructor
    private final class EffectManager {
        private int index = 0;
        private int angle = 0;
        private double distance = 0;
        private double up = 0;

        /**
         * 효과를 재생한다.
         */
        private void playEffect() {
            Location loc = combatUser.getLocation().add(0, 0.1, 0);
            loc.setYaw(0);
            loc.setPitch(0);
            Vector vector = VectorUtil.getRollAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            for (int j = 0; j < 2; j++) {
                angle += 6;

                if (index > 7)
                    up += 0.15;
                else
                    distance += 0.15;

                for (int k = 0; k < 6; k++) {
                    angle += 360 / 3;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 3 ? angle : -angle).multiply(distance);
                    Location loc2 = loc.clone().add(vec).add(0, up, 0);

                    VellionUltInfo.PARTICLE.USE_TICK.play(loc2);
                }
            }

            index++;
        }
    }

    private final class VellionUltArea extends Area<Damageable> {
        private VellionUltArea() {
            super(combatUser, VellionUltInfo.RADIUS, CombatUtil.EntityCondition.enemy(combatUser).and(Damageable::isCreature));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, false)) {
                target.getStatusEffectModule().apply(VellionUltSlow.instance, combatUser, Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(Grounding.getInstance(), combatUser, Timespan.ofTicks(10));

                if (target instanceof CombatUser)
                    bonusScoreModule.addTarget((CombatUser) target, Timespan.ofTicks(10));
            }

            return true;
        }
    }

    private final class VellionUltExplodeArea extends Area<Damageable> {
        private VellionUltExplodeArea() {
            super(combatUser, VellionUltInfo.RADIUS, CombatUtil.EntityCondition.enemy(combatUser).and(Damageable::isCreature));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, target.getDamageModule().getMaxHealth() * VellionUltInfo.DAMAGE_RATIO,
                    DamageType.FIXED, null, false, false)) {
                target.getStatusEffectModule().apply(Stun.getInstance(), combatUser, VellionUltInfo.STUN_DURATION);

                if (target instanceof CombatUser) {
                    combatUser.addScore("결계 발동", VellionUltInfo.DAMAGE_SCORE);
                    bonusScoreModule.addTarget((CombatUser) target, VellionUltInfo.STUN_DURATION);
                }
            }

            VellionUltInfo.PARTICLE.HIT_ENTITY_CORE.play(location);

            Location loc = combatUser.getEntity().getEyeLocation().add(0, 1, 0);
            for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.4))
                VellionUltInfo.PARTICLE.HIT_ENTITY_DECO.play(loc2);

            return true;
        }
    }
}
