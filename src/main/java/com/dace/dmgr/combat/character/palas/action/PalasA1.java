package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;

public final class PalasA1 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    private static final String ASSIST_SCORE_COOLDOWN_ID = "PalasA1AssistScoreTimeLimit";

    public PalasA1(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return PalasA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown(PalasA1Info.GLOBAL_COOLDOWN);

        SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A1_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = combatUser.getArmLocation(true);
            new PalasA1Projectile().shoot(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A1_USE_READY, loc);
        }, PalasA1Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     * @param score  점수 (처치 기여도)
     */
    public void applyAssistScore(@NonNull CombatUser victim, int score) {
        if (score < 100 && CooldownUtil.getCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            combatUser.addScore("처치 지원", PalasA1Info.ASSIST_SCORE * score / 100.0);
    }

    /**
     * 기절 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PalasA1Stun extends Stun {
        private static final PalasA1Stun instance = new PalasA1Stun();

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            super.onTick(combatEntity, provider, i);

            if (combatEntity instanceof CombatUser)
                CombatUtil.addYawAndPitch(combatEntity.getEntity(),
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 20,
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 20);

            if (i % 2 == 0) {
                ParticleUtil.play(Particle.CRIT, combatEntity.getCenterLocation(), 20, 0, 0, 0, 0.6);
                SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A1_TICK, combatEntity.getEntity().getLocation());
            }
        }
    }

    private final class PalasA1Projectile extends Projectile {
        private PalasA1Projectile() {
            super(combatUser, PalasA1Info.VELOCITY, ProjectileOption.builder().trailInterval(8).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            ParticleUtil.play(Particle.CRIT, getLocation(), 1, 0, 0, 0, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, getLocation(), 1,
                    0, 0, 0, 240, 230, 50);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, PalasA1Info.DAMAGE, DamageType.NORMAL, getLocation(), false, true)) {
                if (target.getDamageModule().isLiving()) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, PalasA1Stun.instance, PalasA1Info.STUN_DURATION);

                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE,
                            target.getCenterLocation(), 20, target.getEntity().getWidth() / 2,
                            target.getEntity().getHeight() / 2, target.getEntity().getWidth() / 2,
                            240, 230, 50);
                }

                SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A1_HIT_ENTITY, getLocation());

                if (target instanceof CombatUser) {
                    combatUser.addScore("적 기절시킴", PalasA1Info.DAMAGE_SCORE);
                    CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, PalasA1Info.STUN_DURATION);
                }
            }

            return false;
        }
    }
}
