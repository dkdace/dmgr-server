package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@Getter
public final class NeaceUlt extends UltimateSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "NeaceUlt";
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public NeaceUlt(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceUltInfo.getInstance());
    }

    @Override
    public int getCost() {
        return NeaceUltInfo.COST;
    }

    @Override
    public long getDefaultDuration() {
        return NeaceUltInfo.DURATION;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getSkill(NeaceA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(-1);
        combatUser.setGlobalCooldown((int) NeaceUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -NeaceUltInfo.READY_SLOW);

        SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_ULT_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            playUseTickEffect(i);

            return true;
        }, isCancelled -> {
            onCancelled();
            onReady();
        }, 1, NeaceUltInfo.READY_DURATION));
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
        Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 3; j++) {
            long index = i * 3 + j;
            long angle = index * (i < 10 ? 7 : -31);
            double distance = 10;
            double up = 0;
            if (i < 10)
                distance = index * 0.35;
            else
                up = (index - 30) * 0.2;

            for (int k = 0; k < 8; k++) {
                angle += 90;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 4 ? angle : -angle).multiply(distance);
                Location loc2 = loc.clone().add(vec).add(0, up, 0);

                ParticleUtil.play(Particle.VILLAGER_HAPPY, loc2, 3, 0.05, 0.05, 0.05, 0);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc2, 1, 0, 0, 0,
                        215, 255, 130);
                ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.GRASS, 0, loc2, 1,
                        0, 0, 0, 0);
            }
        }
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        isEnabled = true;

        setDuration();
        combatUser.getDamageModule().heal(combatUser, combatUser.getDamageModule().getMaxHealth(), false);

        SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_ULT_USE_READY, combatUser.getEntity().getLocation());
        ParticleUtil.playFirework(combatUser.getEntity().getLocation(), 215, 255, 130,
                255, 255, 255, FireworkEffect.Type.STAR, true, false);

        TaskUtil.addTask(this, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getEyeLocation();
            Predicate<CombatEntity> condition = combatEntity -> combatEntity instanceof Healable && !combatEntity.isEnemy(combatUser) &&
                    combatEntity != combatUser;
            new NeaceUltArea(condition).emit(loc);

            playTickEffect(i);
            SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_WEAPON_USE_HEAL, combatUser.getEntity().getLocation());

            return true;
        }, isCancelled2 -> isEnabled = false, 1, NeaceUltInfo.DURATION));
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        Location loc = combatUser.getEntity().getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(15);
        Vector axis = VectorUtil.getYawAxis(loc);

        long angle = i * 6;
        for (int j = 0; j < 8; j++) {
            angle += 90;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 4 ? angle : -angle);
            Location loc2 = loc.clone().add(vec);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc2, 3,
                    0.1, 0.1, 0.1, 215, 255, 130);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.GRASS, 0, loc2.clone().add(0, 2, 0),
                    4, 0.15, 0.4, 0.15, 0);
        }
    }

    private final class NeaceUltArea extends Area {
        private NeaceUltArea(Predicate<CombatEntity> condition) {
            super(combatUser, NeaceWeaponInfo.HEAL.MAX_DISTANCE, condition);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            ((NeaceWeapon) combatUser.getWeapon()).healTarget((Healable) target);

            return true;
        }
    }
}
