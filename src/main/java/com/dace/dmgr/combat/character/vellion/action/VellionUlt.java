package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@Getter
public final class VellionUlt extends UltimateSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    public static final String ASSIST_SCORE_COOLDOWN_ID = "VellionUltAssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionUlt";
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    VellionUlt(@NonNull CombatUser combatUser) {
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
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && !((VellionA3) combatUser.getSkill(VellionA3Info.getInstance())).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(-1);
        if (combatUser.getSkill(VellionP1Info.getInstance()).isCancellable())
            combatUser.getSkill(VellionP1Info.getInstance()).onCancelled();
        combatUser.setGlobalCooldown((int) VellionUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -100);

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_ULT_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            for (int j = 0; j < 2; j++)
                playUseTickEffect(i * 2 + j);

            return true;
        }, isCancelled -> new IntervalTask(j -> !combatUser.getEntity().isOnGround(), isCancelled2 -> {
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
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        long angle = i * 6;
        long angle2 = i * -6;
        double distance = 2.5;
        double up = 0;
        if (i < 16)
            distance = i * 0.15;
        else
            up = (i - 16) * 0.15;

        Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 6; j++) {
            angle += 120;
            angle2 += 120;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 3 ? angle : angle2);

            ParticleUtil.play(Particle.SPELL_WITCH, loc.clone().add(vec.clone().multiply(distance)).add(0, up, 0),
                    3, 0.05, 0.05, 0.05, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc.clone().add(vec.clone().multiply(distance)).add(0, up, 0),
                    1, 0, 0, 0, 70, 0, 45);
        }
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = (i >= 5 ? (int) i - 5 : 0); j < i; j++) {
            double distance = j * 0.16;

            for (int k = 0; k < 6; k++) {
                int angle = j * (j > 30 ? -3 : 5) + k * 60;

                Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle).multiply(distance);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, -angle).multiply(distance);

                if (j > 0 && j % 10 == 0) {
                    ParticleUtil.play(Particle.SPELL_WITCH, loc.clone().add(vec1).add(0, 2.5, 0), 20, 0, 2, 0, 0);
                    ParticleUtil.play(Particle.SPELL_WITCH, loc.clone().add(vec2).add(0, 2.5, 0), 20, 0, 2, 0, 0);
                } else {
                    ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.CONCRETE, 14, loc.clone().add(vec1),
                            1, 0, 0, 0, 0);
                    ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.CONCRETE, 14, loc.clone().add(vec2),
                            1, 0, 0, 0, 0);
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec1), 1,
                            0, 0, 0, (int) (30 + distance * 12), 0, (int) (18 + distance * 10));
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec2), 1,
                            0, 0, 0, (int) (30 + distance * 12), 0, (int) (18 + distance * 10));
                }
            }
        }

        long angle = i * 4;
        long angle2 = i * -4;
        for (int j = 0; j < 8; j++) {
            angle += 90;
            angle2 += 90;

            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 4 ? angle : angle2).multiply(8);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc.clone().add(vec), 3, 0.1, 0.1, 0.1,
                    90, 0, 55);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.MYCEL, 0, loc.clone().add(vec).add(0, 2, 0),
                    4, 0.15, 0.4, 0.15, 0);
        }
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        isEnabled = true;

        setDuration();
        combatUser.getStatusEffectModule().applyStatusEffect(combatUser, Invulnerable.getInstance(), VellionUltInfo.DURATION);

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_ULT_USE_READY, combatUser.getEntity().getLocation());

        Predicate<CombatEntity> condition = combatEntity -> combatEntity instanceof Living && combatEntity.isEnemy(combatUser);
        TaskUtil.addTask(VellionUlt.this, new IntervalTask(i -> {
            if (i % 4 == 0) {
                Location loc = combatUser.getEntity().getEyeLocation();
                CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc, VellionUltInfo.RADIUS, condition);
                new VellionUltArea(condition, targets).emit(loc);
            }

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatUser.getEntity().getEyeLocation().add(0, 1, 0), 4,
                    0.3, 0, 0.3, 90, 0, 55);
            if (i < 8)
                ParticleUtil.play(Particle.PORTAL, combatUser.getEntity().getEyeLocation().add(0, 1, 0), 40,
                        0, 0, 0, 1.5);
            playTickEffect(i);

            return true;
        }, isCancelled -> {
            Location loc = combatUser.getEntity().getEyeLocation().add(0, 1, 0);
            CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc, VellionUltInfo.RADIUS, condition);
            new VellionUltExplodeArea(condition, targets).emit(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_ULT_EXPLODE, loc);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.STAINED_GLASS, 2, loc, 300,
                    0.3, 0.3, 0.3, 0.4);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.STAINED_GLASS, 14, loc, 200,
                    0.3, 0.3, 0.3, 0.4);

            isEnabled = false;
            onCancelled();
        }, 1, VellionUltInfo.DURATION));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class VellionUltSlow extends Slow {
        private static final VellionUltSlow instance = new VellionUltSlow();

        @Override
        public void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -VellionUltInfo.SLOW);
        }

        @Override
        public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }

    private final class VellionUltArea extends Area {
        private VellionUltArea(Predicate<CombatEntity> condition, CombatEntity[] targets) {
            super(combatUser, VellionUltInfo.RADIUS, condition, targets);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, VellionUltSlow.instance, 10);
                target.getStatusEffectModule().applyStatusEffect(combatUser, Grounding.getInstance(), 10);
                if (target instanceof CombatUser)
                    CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, 10);
            }

            return true;
        }
    }

    private final class VellionUltExplodeArea extends Area {
        private VellionUltExplodeArea(Predicate<CombatEntity> condition, CombatEntity[] targets) {
            super(combatUser, VellionUltInfo.RADIUS, condition, targets);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, (int) (target.getDamageModule().getMaxHealth() * VellionUltInfo.DAMAGE_RATIO), DamageType.NORMAL,
                    null, false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, Stun.getInstance(), VellionUltInfo.STUN_DURATION);
                if (target instanceof CombatUser) {
                    combatUser.addScore("결계 발동", VellionUltInfo.DAMAGE_SCORE);
                    CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, VellionUltInfo.STUN_DURATION);
                }
            }

            Location loc = combatUser.getEntity().getEyeLocation().add(0, 1, 0);
            for (Location trailLoc : LocationUtil.getLine(loc, target.getEntity().getLocation().add(0, 1, 0), 0.4))
                ParticleUtil.play(Particle.SMOKE_NORMAL, trailLoc, 3, 0.05, 0.50, 0.05, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, location, 50, 0, 0, 0, 0.4);

            return true;
        }
    }
}
