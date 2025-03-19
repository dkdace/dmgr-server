package com.dace.dmgr.combat.combatant.neace.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class NeaceA2 extends ActiveSkill {
    /** 공격력 수정자 */
    private static final AbilityStatus.Modifier DAMAGE_MODIFIER = new AbilityStatus.Modifier(NeaceA2Info.DAMAGE_INCREMENT);
    /** 방어력 수정자 */
    private static final AbilityStatus.Modifier DEFENSE_MODIFIER = new AbilityStatus.Modifier(NeaceA2Info.DEFENSE_INCREMENT);

    public NeaceA2(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA2Info.getInstance(), NeaceA2Info.COOLDOWN, NeaceA2Info.DURATION, 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished())
            return null;

        return ActionBarStringUtil.getDurationBar(this) + ActionBarStringUtil.getKeyInfo(this, "해제");
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            forceCancel();
            return;
        }

        setDuration();
        combatUser.getWeapon().setGlowing(true);

        NeaceA2Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            NeaceA2Info.PARTICLE.TICK.play(combatUser.getCenterLocation());
            if (i < 12)
                playUseTickEffect(i);
        }, 1, NeaceA2Info.DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();
        combatUser.getWeapon().setGlowing(false);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
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
            angle += 360 / 4;
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
        target.getStatusEffectModule().apply(NeaceA2Buff.instance, combatUser, Timespan.ofTicks(4));

        if (target instanceof CombatUser)
            ((CombatUser) target).addKillHelper(combatUser, this, NeaceA2Info.ASSIST_SCORE, Timespan.ofTicks(4));
    }

    /**
     * 축복 상태 효과 클래스.
     */
    private static final class NeaceA2Buff extends StatusEffect {
        private static final NeaceA2Buff instance = new NeaceA2Buff();

        private NeaceA2Buff() {
            super(true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().addModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().addModifier(DAMAGE_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().removeModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().removeModifier(DAMAGE_MODIFIER);
        }
    }
}
