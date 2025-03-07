package com.dace.dmgr.combat.combatant.vellion.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
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
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.WeakHashMap;

@Getter
public final class VellionUlt extends UltimateSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-100);
    /** 처치 지원 점수 제한시간 타임스탬프 목록 (피격자 : 종료 시점) */
    private final WeakHashMap<CombatUser, Timestamp> assistScoreTimeLimitTimestampMap = new WeakHashMap<>();
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public VellionUlt(@NonNull CombatUser combatUser) {
        super(combatUser, VellionUltInfo.getInstance());
    }

    @Override
    public int getCost() {
        return VellionUltInfo.COST;
    }

    @Override
    public long getDefaultDuration() {
        return VellionUltInfo.DURATION;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(-1);
        combatUser.setGlobalCooldown(Timespan.ofTicks(VellionUltInfo.READY_DURATION));
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        VellionP1 skillp1 = combatUser.getSkill(VellionP1Info.getInstance());
        if (skillp1.isCancellable())
            skillp1.onCancelled();

        VellionUltInfo.SOUND.USE.play(combatUser.getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(this::playUseTickEffect,
                () -> new IntervalTask(i -> !combatUser.getEntity().isOnGround(), () -> {
                    setDuration(0);
                    onReady();
                }, 1), 1, VellionUltInfo.READY_DURATION));
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
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        Location loc = combatUser.getLocation().add(0, 0.1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * 6;
            double distance = 2.5;
            double up = 0;
            if (i < 8)
                distance = index * 0.15;
            else
                up = (index - 16) * 0.15;

            for (int k = 0; k < 6; k++) {
                angle += 120;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 3 ? angle : -angle).multiply(distance);
                Location loc2 = loc.clone().add(vec).add(0, up, 0);

                VellionUltInfo.PARTICLE.USE_TICK.play(loc2);
            }
        }
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        setDuration();
        isEnabled = true;
        combatUser.getStatusEffectModule().apply(Invulnerable.getInstance(), combatUser, Timespan.ofTicks(VellionUltInfo.DURATION));

        VellionUltInfo.SOUND.USE_READY.play(combatUser.getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (combatUser.isDead())
                return false;

            if (i % 4 == 0)
                new VellionUltArea().emit(combatUser.getEntity().getEyeLocation());

            VellionUltInfo.PARTICLE.TICK_CORE_1.play(combatUser.getEntity().getEyeLocation().add(0, 1, 0));
            if (i < 8)
                VellionUltInfo.PARTICLE.TICK_CORE_2.play(combatUser.getEntity().getEyeLocation().add(0, 1, 0));
            playTickEffect(i);

            return true;
        }, isCancelled -> {
            onCancelled();

            Location loc = combatUser.getEntity().getEyeLocation();
            new VellionUltExplodeArea().emit(loc);

            Location loc2 = loc.add(0, 1, 0);
            VellionUltInfo.SOUND.EXPLODE.play(loc2);
            VellionUltInfo.PARTICLE.EXPLODE.play(loc2);
        }, 1, VellionUltInfo.DURATION));
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
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = (i >= 5 ? (int) i - 5 : 0); j < i; j++) {
            int angle = j * (j > 30 ? -3 : 5);
            double distance = j * 0.16;

            for (int k = 0; k < 12; k++) {
                angle += 60;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle).multiply(distance);
                Location loc2 = loc.clone().add(vec);

                if (j > 0 && j % 10 == 0)
                    VellionUltInfo.PARTICLE.TICK_DECO_1.play(loc2.clone().add(0, 2.5, 0));
                else {
                    if (i <= 20)
                        VellionUltInfo.PARTICLE.TICK_DECO_2.play(loc2, j / 49.0);
                    else
                        VellionUltInfo.PARTICLE.TICK_DECO_3.play(loc2);
                }
            }
        }

        long angle = i * 4;
        for (int j = 0; j < 8; j++) {
            angle += 90;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 4 ? angle : -angle).multiply(8);
            Location loc2 = loc.clone().add(vec);

            VellionUltInfo.PARTICLE.TICK_DECO_4.play(loc2);
            VellionUltInfo.PARTICLE.TICK_DECO_5.play(loc2.clone().add(0, 2, 0));
        }
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        Timestamp expiration = assistScoreTimeLimitTimestampMap.get(victim);
        if (expiration != null && expiration.isAfter(Timestamp.now()))
            combatUser.addScore("처치 지원", VellionUltInfo.ASSIST_SCORE);
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
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().apply(VellionUltSlow.instance, combatUser, Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(Grounding.getInstance(), combatUser, Timespan.ofTicks(10));

                if (target instanceof CombatUser)
                    assistScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(10)));
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
                    DamageType.FIXED, null, false, true)) {
                target.getStatusEffectModule().apply(Stun.getInstance(), combatUser, Timespan.ofTicks(VellionUltInfo.STUN_DURATION));

                if (target instanceof CombatUser) {
                    combatUser.addScore("결계 발동", VellionUltInfo.DAMAGE_SCORE);
                    assistScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(VellionUltInfo.STUN_DURATION)));
                }
            }

            Location loc = combatUser.getEntity().getEyeLocation().add(0, 1, 0);
            for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.4))
                VellionUltInfo.PARTICLE.HIT_ENTITY_DECO.play(loc2);
            VellionUltInfo.PARTICLE.HIT_ENTITY_CORE.play(location);

            return true;
        }
    }
}
