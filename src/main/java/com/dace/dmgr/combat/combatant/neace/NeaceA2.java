package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class NeaceA2 extends ChargeableSkill {
    /** 공격력 수정자 */
    private static final AbilityStatus.Modifier DAMAGE_MODIFIER = new AbilityStatus.Modifier(NeaceA2Info.DAMAGE_INCREMENT);
    /** 방어력 수정자 */
    private static final AbilityStatus.Modifier DEFENSE_MODIFIER = new AbilityStatus.Modifier(NeaceA2Info.DEFENSE_INCREMENT);

    public NeaceA2(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA2Info.getInstance(), NeaceA2Info.COOLDOWN, NeaceA2Info.MAX_DURATION.toSeconds(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        String text = ActionBarStringUtil.getDurationBar(this, Timespan.ofSeconds(getStateValue()), Timespan.ofSeconds(maxStateValue));
        if (!isDurationFinished())
            text += ActionBarStringUtil.getKeyInfo(this, "해제");

        return text;
    }

    @Override
    protected double getStateValueDecrement() {
        return 1;
    }

    @Override
    protected double getStateValueIncrement() {
        return getMaxStateValue() / NeaceA2Info.RECOVER_DURATION.toSeconds();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            forceCancel();
            return;
        }

        setDuration();
        combatUser.getActionManager().getWeapon().setGlowing(true);

        NeaceA2Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            if (getStateValue() <= 0)
                return false;

            NeaceA2Info.Particles.TICK.play(combatUser.getCenterLocation());
            if (i < 10)
                playUseTickEffect(i);

            return true;
        }, this::forceCancel, 1));
    }

    @Override
    public boolean isCancellable() {
        return combatUser.isDead() && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        combatUser.getActionManager().getWeapon().setGlowing(false);

        NeaceA2Info.Sounds.DISABLE.play(combatUser.getLocation());
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

            NeaceA2Info.Particles.USE_TICK.play(loc.clone().add(vec).add(0, up, 0), i / 9.0);
        }
    }

    /**
     * 대상에게 축복 효과를 적용한다.
     *
     * @param target 적용 대상
     */
    void amplifyTarget(@NonNull Healable target) {
        target.getStatusEffectModule().apply(NeaceA2Buff.instance, Timespan.ofTicks(4));

        if (target instanceof CombatUser)
            ((CombatUser) target).addKillHelper(combatUser, this, NeaceA2Info.ASSIST_SCORE, Timespan.ofTicks(4));
    }

    /**
     * 축복 상태 효과 클래스.
     */
    private static final class NeaceA2Buff implements StatusEffect {
        private static final NeaceA2Buff instance = new NeaceA2Buff();

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().addModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().addModifier(DAMAGE_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            combatEntity.getDamageModule().getDefenseMultiplierStatus().removeModifier(DEFENSE_MODIFIER);
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().removeModifier(DAMAGE_MODIFIER);
        }
    }
}
