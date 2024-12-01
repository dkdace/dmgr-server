package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.ParticleUtil;
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(NeaceA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.setGlobalCooldown((int) NeaceUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -NeaceUltInfo.READY_SLOW);

        NeaceUltInfo.SOUND.USE.play(combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(this::playUseTickEffect, () -> {
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
        setDuration();
        isEnabled = true;
        combatUser.getDamageModule().heal(combatUser, combatUser.getDamageModule().getMaxHealth(), false);

        NeaceUltInfo.SOUND.USE_READY.play(combatUser.getEntity().getLocation());
        ParticleUtil.playFirework(combatUser.getEntity().getLocation(), 215, 255, 130,
                255, 255, 255, FireworkEffect.Type.STAR, true, false);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (combatUser.isDead())
                return false;

            Location loc = combatUser.getEntity().getEyeLocation();
            new NeaceUltArea().emit(loc);

            playTickEffect(i);
            NeaceWeaponInfo.SOUND.USE_HEAL.play(combatUser.getEntity().getLocation());

            return true;
        }, isCancelled -> isEnabled = false, 1, NeaceUltInfo.DURATION));
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
        Vector vector = VectorUtil.getRollAxis(loc).multiply(1.5);
        Vector axis = VectorUtil.getYawAxis(loc);

        long angle = i * 5;
        for (int j = 0; j < 6; j++) {
            angle += 120;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 3 ? angle : -angle);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc.clone().add(vec), 1,
                    0.1, 0.1, 0.1, 215, 255, 130);
        }
    }

    private final class NeaceUltArea extends Area {
        private NeaceUltArea() {
            super(combatUser, NeaceWeaponInfo.HEAL.MAX_DISTANCE, combatEntity -> Neace.getTargetedActionCondition(NeaceUlt.this.combatUser, combatEntity));
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
