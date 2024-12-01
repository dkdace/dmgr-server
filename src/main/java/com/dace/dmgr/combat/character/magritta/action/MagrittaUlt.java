package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.GunHitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.function.LongConsumer;

public final class MagrittaUlt extends UltimateSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "MagrittaUlt";
    /** 활성화 완료 여부 */
    @Getter
    private boolean isEnabled = false;
    /** 블록 명중 횟수 */
    private int blockHitCount = 0;

    public MagrittaUlt(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaUltInfo.getInstance());
    }

    @Override
    public int getCost() {
        return MagrittaUltInfo.COST;
    }

    @Override
    public long getDefaultDuration() {
        return MagrittaUltInfo.DURATION;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(MagrittaA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) MagrittaUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -MagrittaUltInfo.USE_SLOW);
        ((MagrittaWeapon) combatUser.getWeapon()).getReloadModule().setRemainingAmmo(MagrittaWeaponInfo.CAPACITY);

        MagrittaUltInfo.SOUND.USE.play(combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(this::onReady, MagrittaUltInfo.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        isEnabled = false;
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        setDuration();
        isEnabled = true;

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getLocation();
            HashMap<Damageable, Integer> targets = new HashMap<>();

            for (int j = 0; j < MagrittaWeaponInfo.PELLET_AMOUNT; j++) {
                Vector dir = VectorUtil.getSpreadedVector(loc.getDirection(), MagrittaWeaponInfo.SPREAD * 1.25);
                new MagrittaUltHitscan(targets).shoot(dir);
            }
            targets.forEach((target, hits) -> {
                if (hits >= MagrittaWeaponInfo.PELLET_AMOUNT / 2)
                    MagrittaT1.addShreddingValue(combatUser, target);
            });
            blockHitCount = 0;

            CombatUtil.setRecoil(combatUser, MagrittaWeaponInfo.RECOIL.UP / 2, MagrittaWeaponInfo.RECOIL.SIDE / 2,
                    MagrittaWeaponInfo.RECOIL.UP_SPREAD / 2, MagrittaWeaponInfo.RECOIL.SIDE_SPREAD / 2, 2, 1);
            MagrittaUltInfo.SOUND.SHOOT.play(loc);
            TaskUtil.addTask(MagrittaUlt.this, new DelayTask(() -> CombatEffectUtil.SHOTGUN_SHELL_DROP_SOUND.play(loc), 8));
        }, () -> {
            onCancelled();
            onEnd();
        }, MagrittaUltInfo.ATTACK_COOLDOWN, MagrittaUltInfo.DURATION / 2));
    }

    /**
     * 사용 종료 시 실행할 작업.
     */
    private void onEnd() {
        combatUser.getWeapon().setCooldown(combatUser.getWeapon().getDefaultCooldown() * 2);
        combatUser.getWeapon().setVisible(false);

        Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 0.5);

        MagrittaUltInfo.SOUND.END.play(loc);
        ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 50, 0, 0, 0, 0.05);
        ParticleUtil.play(Particle.LAVA, loc, 15, 0, 0, 0, 0);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, loc, 50,
                0, 0, 0, 0.1);
        ParticleUtil.play(Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0, 0);

        TaskUtil.addTask(this, new IntervalTask((LongConsumer) i ->
                CombatUtil.addYawAndPitch(combatUser.getEntity(),
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 10,
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 8), 1, 7));

        TaskUtil.addTask(this, new DelayTask(() -> {
            combatUser.getWeapon().setVisible(true);

            MagrittaUltInfo.SOUND.USE.play(combatUser.getEntity().getLocation());
        }, MagrittaWeaponInfo.COOLDOWN * 2));
    }

    private final class MagrittaUltHitscan extends GunHitscan {
        private final HashMap<Damageable, Integer> targets;
        private double distance = 0;

        private MagrittaUltHitscan(HashMap<Damageable, Integer> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(15).maxDistance(MagrittaWeaponInfo.DISTANCE)
                    .condition(combatUser::isEnemy).build());
            this.targets = targets;
        }

        @Override
        protected boolean onInterval() {
            distance += getVelocity().length();
            return super.onInterval();
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, -0.2, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1, 0, 0, 0, 255, 70, 0);
        }

        @Override
        protected void onHit() {
            ParticleUtil.play(Particle.FLAME, getLocation(), 1, 0, 0, 0, 0.15);
            ParticleUtil.play(Particle.LAVA, getLocation(), 1, 0, 0, 0, 0);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), getLocation(),
                    3, 0, 0, 0, 0.1);
            ParticleUtil.play(Particle.DRIP_LAVA, getLocation(), 1, 0, 0, 0, 0);

            if (blockHitCount++ > 0)
                return false;

            return super.onHitBlock(hitBlock);
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            double damage = CombatUtil.getDistantDamage(MagrittaWeaponInfo.DAMAGE, distance, MagrittaWeaponInfo.DISTANCE / 2.0);
            int shredding = target.getPropertyManager().getValue(Property.SHREDDING);
            if (shredding > 0)
                damage = damage * (100 + MagrittaT1Info.DAMAGE_INCREMENT * shredding) / 100.0;
            if (target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, getLocation(), false, false))
                targets.put(target, targets.getOrDefault(target, 0) + 1);

            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.BONE_BLOCK, 0, getLocation(), 4,
                    0, 0, 0, 0.08);

            return false;
        }
    }
}
