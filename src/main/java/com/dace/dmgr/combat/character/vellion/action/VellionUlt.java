package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Grounding;
import com.dace.dmgr.combat.entity.module.statuseffect.Invulnerable;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

@Getter
public final class VellionUlt extends UltimateSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    private static final String ASSIST_SCORE_COOLDOWN_ID = "VellionUltAssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionUlt";

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
        combatUser.setGlobalCooldown((int) VellionUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -100);

        VellionP1 skillp1 = combatUser.getSkill(VellionP1Info.getInstance());
        if (skillp1.isCancellable())
            skillp1.onCancelled();

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_ULT_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            playUseTickEffect(i);

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
        isEnabled = false;
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
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

                ParticleUtil.play(Particle.SPELL_WITCH, loc2, 3, 0.05, 0.05, 0.05, 0);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc2, 1,
                        0, 0, 0, 70, 0, 45);
            }
        }
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        setDuration();
        isEnabled = true;
        combatUser.getStatusEffectModule().applyStatusEffect(combatUser, Invulnerable.getInstance(), VellionUltInfo.DURATION);

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_ULT_USE_READY, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (combatUser.isDead())
                return false;

            if (i % 4 == 0)
                new VellionUltArea().emit(combatUser.getEntity().getEyeLocation());

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatUser.getEntity().getEyeLocation().add(0, 1, 0), 4,
                    0.3, 0, 0.3, 90, 0, 55);
            if (i < 8)
                ParticleUtil.play(Particle.PORTAL, combatUser.getEntity().getEyeLocation().add(0, 1, 0), 40,
                        0, 0, 0, 1.5);
            playTickEffect(i);

            return true;
        }, isCancelled -> {
            onCancelled();

            Location loc = combatUser.getEntity().getEyeLocation();
            new VellionUltExplodeArea().emit(loc);

            Location loc2 = loc.add(0, 1, 0);
            SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_ULT_EXPLODE, loc2);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.STAINED_GLASS, 2, loc2, 300,
                    0.3, 0.3, 0.3, 0.4);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.STAINED_GLASS, 14, loc2, 200,
                    0.3, 0.3, 0.3, 0.4);
        }, 1, VellionUltInfo.DURATION));
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
            int angle = j * (j > 30 ? -3 : 5);
            double distance = j * 0.16;

            for (int k = 0; k < 12; k++) {
                angle += 60;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle).multiply(distance);
                Location loc2 = loc.clone().add(vec);

                if (j > 0 && j % 10 == 0)
                    ParticleUtil.play(Particle.SPELL_WITCH, loc2.clone().add(0, 2.5, 0), 20, 0, 2, 0, 0);
                else {
                    if (i > 20)
                        ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.CONCRETE, 14, loc2,
                                1, 0, 0, 0, 0);
                    else
                        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc2, 1, 0, 0, 0,
                                (int) (30 + distance * 12), 0, (int) (18 + distance * 10));
                }
            }
        }

        long angle = i * 4;
        for (int j = 0; j < 8; j++) {
            angle += 90;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 4 ? angle : -angle).multiply(8);
            Location loc2 = loc.clone().add(vec);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc2, 3, 0.1, 0.1, 0.1,
                    90, 0, 55);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.MYCEL, 0, loc2.clone().add(0, 2, 0),
                    4, 0.15, 0.4, 0.15, 0);
        }
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        if (CooldownUtil.getCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            combatUser.addScore("처치 지원", VellionUltInfo.ASSIST_SCORE);
    }

    /**
     * 둔화 상태 효과 클래스.
     */
    private static final class VellionUltSlow extends Slow {
        private static final VellionUltSlow instance = new VellionUltSlow();

        private VellionUltSlow() {
            super(MODIFIER_ID, VellionUltInfo.SLOW);
        }
    }

    private final class VellionUltArea extends Area {
        private VellionUltArea() {
            super(combatUser, VellionUltInfo.RADIUS, combatEntity -> combatEntity instanceof Damageable
                    && ((Damageable) combatEntity).getDamageModule().isLiving() && combatEntity.isEnemy(VellionUlt.this.combatUser));
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
        private VellionUltExplodeArea() {
            super(combatUser, VellionUltInfo.RADIUS, combatEntity -> combatEntity instanceof Damageable
                    && ((Damageable) combatEntity).getDamageModule().isLiving() && combatEntity.isEnemy(VellionUlt.this.combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, (int) (target.getDamageModule().getMaxHealth() * VellionUltInfo.DAMAGE_RATIO),
                    DamageType.FIXED, null, false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, Stun.getInstance(), VellionUltInfo.STUN_DURATION);

                if (target instanceof CombatUser) {
                    combatUser.addScore("결계 발동", VellionUltInfo.DAMAGE_SCORE);
                    CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, VellionUltInfo.STUN_DURATION);
                }
            }

            Location loc = combatUser.getEntity().getEyeLocation().add(0, 1, 0);
            for (Location loc2 : LocationUtil.getLine(loc, target.getCenterLocation(), 0.4))
                ParticleUtil.play(Particle.SMOKE_NORMAL, loc2, 3, 0.05, 0.05, 0.05, 0);
            ParticleUtil.play(Particle.CRIT_MAGIC, location, 50, 0, 0, 0, 0.4);

            return true;
        }
    }
}
