package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Getter
public final class MagrittaA1 extends ActiveSkill {
    public MagrittaA1(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return MagrittaA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(MagrittaA2Info.getInstance()).isDurationFinished()
                && combatUser.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) MagrittaA1Info.READY_DURATION);

        SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_A1_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = combatUser.getArmLocation(true);
            new MagrittaA1Projectile().shoot(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_THROW, loc, 1, -0.1);
        }, MagrittaA1Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    /**
     * 화염 상태 효과 클래스.
     */
    private static final class MagrittaA1Burning extends Burning {
        private static final MagrittaA1Burning instance = new MagrittaA1Burning();

        private MagrittaA1Burning() {
            super(MagrittaA1Info.FIRE_DAMAGE_PER_SECOND, true);
        }
    }

    private final class MagrittaA1Projectile extends Projectile {
        private MagrittaA1Projectile() {
            super(combatUser, MagrittaA1Info.VELOCITY, ProjectileOption.builder().trailInterval(7).hasGravity(true)
                    .condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, getLocation(), 3,
                    0.1, 0.1, 0.1, 200, 95, 35);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            onStuck(getLocation(), null);
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, MagrittaA1Info.DAMAGE_DIRECT, DamageType.NORMAL, getLocation(), false, true);
            onStuck(getLocation(), target);

            return false;
        }

        /**
         * 폭탄 부착 시 실행할 작업.
         *
         * @param location 부착 위치
         * @param target   부착 대상
         */
        private void onStuck(@NonNull Location location, @Nullable Damageable target) {
            SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_A1_STUCK, location);

            if (target != null) {
                combatUser.getUser().sendTitle("§b§l부착", "", 0, 5, 10);

                if (target instanceof CombatUser)
                    combatUser.addScore("부착", MagrittaA1Info.STUCK_SCORE);
            }

            TaskUtil.addTask(MagrittaA1.this, new IntervalTask(i -> {
                Location loc = location.clone();

                if (target == null) {
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 6, 0.15, 0.15, 0.15,
                            200, 30, 15);
                    ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 4, 0.12, 0.12, 0.12, 0);
                } else {
                    if ((target instanceof CombatUser && ((CombatUser) target).isDead()) || target.isDisposed())
                        return false;

                    loc = target.getCenterLocation();
                }

                if (i % 2 == 0)
                    SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_A1_TICK, loc);

                return true;
            }, isCancelled -> {
                Location loc = (target == null ? location.clone() : target.getHitboxLocation().add(0, target.getEntity().getHeight() / 2, 0))
                        .add(0, 0.1, 0);
                new MagrittaA1Area(this).emit(loc);

                SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_A1_EXPLODE, loc);
                ParticleUtil.play(Particle.SMOKE_LARGE, loc, 80, 0.3, 0.3, 0.3, 0.1);
                ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 150, 0.2, 0.2, 0.2, 0.3);
                ParticleUtil.play(Particle.LAVA, loc, 100, 0.8, 0.8, 0.8, 0);
                ParticleUtil.play(Particle.FLAME, loc, 250, 0.2, 0.2, 0.2, 0.15);
                ParticleUtil.play(Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0, 0);
            }, 1, MagrittaA1Info.EXPLODE_DURATION));
        }
    }

    private final class MagrittaA1Area extends Area {
        private final MagrittaA1Projectile projectile;

        private MagrittaA1Area(MagrittaA1Projectile projectile) {
            super(combatUser, MagrittaA1Info.RADIUS, combatEntity -> combatEntity.isEnemy(MagrittaA1.this.combatUser)
                    || combatEntity == MagrittaA1.this.combatUser);
            this.projectile = projectile;
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            double distance = center.distance(location);
            int damage = CombatUtil.getDistantDamage(MagrittaA1Info.DAMAGE_EXPLODE, distance,
                    MagrittaA1Info.RADIUS / 2.0);
            int burning = CombatUtil.getDistantDamage((int) MagrittaA1Info.FIRE_DURATION, distance,
                    MagrittaA1Info.RADIUS / 2.0);
            if (target.getDamageModule().damage(projectile, damage, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, MagrittaA1Burning.instance, burning);
                target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0))
                        .multiply(MagrittaA1Info.KNOCKBACK));
                MagrittaT1.addShreddingValue(combatUser, target);
            }

            return !(target instanceof Barrier);
        }
    }
}
