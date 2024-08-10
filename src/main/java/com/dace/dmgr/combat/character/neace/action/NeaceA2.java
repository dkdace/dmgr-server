package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class NeaceA2 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    public static final String ASSIST_SCORE_COOLDOWN_ID = "NeaceA2AssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "NeaceA2";

    public NeaceA2(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return NeaceA2Info.DURATION;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();
            combatUser.getWeapon().setGlowing(true);

            SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_A2_USE, combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (isDurationFinished())
                    return false;

                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, combatUser.getCenterLocation(), 3,
                        1, 1.5, 1, 140, 255, 245);
                if (i < 12)
                    playTickEffect(i);

                return true;
            }, isCancelled -> combatUser.getWeapon().setGlowing(false), 1));
        } else
            setDuration(0);
    }

    @Override
    public boolean isCancellable() {
        return false;
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
        Vector vector = VectorUtil.getRollAxis(loc).multiply(1.3);
        Vector axis = VectorUtil.getYawAxis(loc);

        long angle = i * 14;
        for (int j = 0; j < 4; j++) {
            angle += 90;
            double up = (i * 4 + j) * 0.05;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec).add(0, up, 0), 6,
                    0.2, 0.2, 0.2, (int) (200 - i * 5), 255, (int) (i * 8 + 160));
        }
    }

    /**
     * 축복 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class NeaceA2Buff implements StatusEffect {
        static final NeaceA2Buff instance = new NeaceA2Buff();

        @Override
        @NonNull
        public StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().addModifier(MODIFIER_ID, NeaceA2Info.DEFENSE_INCREMENT);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().addModifier(MODIFIER_ID, NeaceA2Info.DAMAGE_INCREMENT);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, NeaceA2Info.SPEED);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().removeModifier(MODIFIER_ID);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().removeModifier(MODIFIER_ID);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }
}
