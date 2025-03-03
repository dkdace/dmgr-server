package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class NeaceA2 extends ActiveSkill {
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

            NeaceA2Info.SOUND.USE.play(combatUser.getLocation());

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (isDurationFinished() || combatUser.isDead())
                    return false;

                NeaceA2Info.PARTICLE.TICK.play(combatUser.getCenterLocation());
                if (i < 12)
                    playUseTickEffect(i);

                return true;
            }, () -> combatUser.getWeapon().setGlowing(false), 1));
        } else
            setDuration(0);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        Location loc = combatUser.getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(1.3);
        Vector axis = VectorUtil.getYawAxis(loc);

        long angle = i * 14;
        for (int j = 0; j < 4; j++) {
            angle += 90;
            double up = (i * 4 + j) * 0.05;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            NeaceA2Info.PARTICLE.USE_TICK.play(loc.clone().add(vec).add(0, up, 0), i / 11.0);
        }
    }

    /**
     * 대상에게 축복 효과를 적용한다.
     *
     * @param target 적용 대상
     */
    void amplifyTarget(@NonNull Healable target) {
        target.getStatusEffectModule().applyStatusEffect(combatUser, NeaceA2Buff.instance, 4);
        if (target instanceof CombatUser)
            ((CombatUser) target).addKillHelper(combatUser, this, NeaceA2Info.ASSIST_SCORE, Timespan.ofTicks(4));
    }

    /**
     * 축복 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NeaceA2Buff implements StatusEffect {
        private static final NeaceA2Buff instance = new NeaceA2Buff();

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
        }
    }
}
