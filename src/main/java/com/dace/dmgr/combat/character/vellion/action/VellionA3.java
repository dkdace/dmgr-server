package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.HealBlock;
import com.dace.dmgr.combat.entity.module.statuseffect.Silence;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@Getter
public final class VellionA3 extends ActiveSkill implements Confirmable {
    /** 처치 점수 제한시간 쿨타임 ID */
    public static final String KILL_SCORE_COOLDOWN_ID = "VellionA3KillScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionA3";
    /** 위치 확인 모듈 */
    @NonNull
    private final LocationConfirmModule confirmModule;

    VellionA3(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA3Info.getInstance(), 2);
        confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_3, VellionA3Info.MAX_DISTANCE);
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
    public long getDefaultCooldown() {
        return VellionA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
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
                onAccept();

                break;
            }
        }
    }

    @Override
    public boolean isCancellable() {
        return confirmModule.isChecking() || !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        confirmModule.setChecking(false);
        if (!isDurationFinished()) {
            setDuration(0);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
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
    public void onAccept() {
        if (!confirmModule.isValid())
            return;

        setDuration();
        combatUser.setGlobalCooldown((int) VellionA3Info.READY_DURATION);
        confirmModule.toggleCheck();
        combatUser.getWeapon().setCooldown(1);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -VellionA3Info.READY_SLOW);
        Location location = confirmModule.getCurrentLocation();

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A3_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0.2, -0.4, 0);
            for (Location trailLoc : LocationUtil.getLine(loc, location, 0.7))
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 1, 0, 0, 0,
                        156, 60, 130);
            ParticleUtil.play(Particle.SMOKE_LARGE, location, 30, 0.5, 0.3, 0.5, 0.15);
            ParticleUtil.play(Particle.SPELL_WITCH, location, 70, 1, 0.5, 1, 0.2);

            return true;
        }, isCancelled -> {
            onCancelled();
            onReady(location);
        }, 1, VellionA3Info.READY_DURATION));

    }

    /**
     * 시전 완료 시 실행할 작업.
     *
     * @param location 대상 위치
     */
    private void onReady(Location location) {
        Location loc = location.clone().add(0, 0.1, 0);

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A3_USE_READY, loc);

        TaskUtil.addTask(VellionA3.this, new IntervalTask(i -> {
            if (i % 4 == 0) {
                Predicate<CombatEntity> condition = combatEntity -> combatEntity.isEnemy(combatUser);
                CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc, VellionA3Info.RADIUS, condition);
                new VellionA3Area(condition, targets).emit(loc);
            }

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc, 3, 0.4, 0, 0.4,
                    156, 60, 130);
            for (int j = 0; j < 2; j++)
                playTickEffect(i * 2 + j, loc);

            return true;
        }, 1, VellionA3Info.DURATION));
    }

    /**
     * 범위 표시 효과를 재생한다.
     *
     * @param i        인덱스
     * @param location 사용 위치
     */
    private void playTickEffect(long i, Location location) {
        location.setYaw(0);
        location.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(location);
        Vector axis = VectorUtil.getYawAxis(location);

        double distance = i * 0.2 % 5;
        long angle = i * 3;

        for (int j = 0; j < 6; j++) {
            angle += distance > 3 ? 90 : 60;

            Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle);
            Vector vec2 = VectorUtil.getRotatedVector(vector, axis, -angle);

            ParticleUtil.play(Particle.SMOKE_NORMAL, location.clone().add(vec1.clone().multiply(distance)), 0,
                    vec1.getX(), 0.4, vec1.getZ(), 0.1);
            ParticleUtil.play(Particle.SMOKE_NORMAL, location.clone().subtract(vec2.clone().multiply(distance)), 0,
                    vec2.getZ(), 0.4, vec2.getZ(), 0.1);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().add(vec1.clone().multiply(distance)), 1,
                    0, 0, 0, 156, 60, 130);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().subtract(vec2.clone().multiply(distance)), 1,
                    0, 0, 0, 156, 60, 130);
        }

        double distance2 = i * 0.1 % 2.5;
        long angle2 = i * 44;
        for (int j = 0; j < 3; j++) {
            angle2 += 120;

            Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle2);
            Vector vec2 = LocationUtil.getDirection(location.clone().add(vec1), location.clone().add(VectorUtil.getRotatedVector(vector, axis, angle2 + 20)));

            ParticleUtil.play(Particle.SMOKE_LARGE, location.clone().add(vec1.clone().multiply(5)).add(0, distance2 * 0.5, 0),
                    0, vec2.getX(), distance2 * 0.1, vec2.getZ(), 0.3);
        }
    }

    private final class VellionA3Area extends Area {
        private VellionA3Area(Predicate<CombatEntity> condition, CombatEntity[] targets) {
            super(combatUser, VellionA3Info.RADIUS, condition, targets);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, HealBlock.getInstance(), 10);
                target.getStatusEffectModule().applyStatusEffect(combatUser, Silence.getInstance(), 10);
            }

            return true;
        }
    }
}
