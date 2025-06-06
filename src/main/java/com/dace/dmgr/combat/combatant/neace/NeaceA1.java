package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Targeted;
import com.dace.dmgr.combat.action.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

@Getter
public final class NeaceA1 extends ActiveSkill implements Targeted<Healable> {
    /** 타겟 모듈 */
    @NonNull
    private final TargetModule<Healable> targetModule;

    public NeaceA1(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA1Info.getInstance(), NeaceA1Info.COOLDOWN, Timespan.MAX, 0);
        this.targetModule = new TargetModule<>(this, NeaceA1Info.MAX_DISTANCE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && targetModule.findTarget();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();

        Healable target = targetModule.getCurrentTarget();

        ValueEffect valueEffect = target.getStatusEffectModule().get(ValueEffect.class);
        if (valueEffect == null)
            valueEffect = new ValueEffect();

        target.getStatusEffectModule().apply(valueEffect, NeaceA1Info.DURATION);

        NeaceA1Info.Effects.USE_SOUND.play(combatUser.getLocation());
        playUseEffect(target);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    @NonNull
    public EntityCondition<Healable> getEntityCondition() {
        return EntityCondition.team(combatUser).exclude(combatUser)
                .and(combatEntity -> !combatEntity.getStatusEffectModule().has(ValueEffect.class));
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param target 사용 대상
     */
    private void playUseEffect(@NonNull Healable target) {
        Location location = combatUser.getArmLocation(MainHand.RIGHT);
        for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4))
            NeaceA1Info.Effects.USE_PARTICLE_1.play(loc);

        Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);
        Vector vector = VectorUtil.getYawAxis(loc).multiply(0.8);
        Vector axis = VectorUtil.getRollAxis(loc);

        for (int i = 0; i < 8; i++) {
            int angle = i * 10;

            for (int j = 0; j < 10; j++) {
                angle += 360 / 5;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 5 ? angle : -angle).multiply(1 + i * 0.2);

                NeaceA1Info.Effects.USE_PARTICLE_2.play(loc.clone().add(vec));
            }
        }
        for (int i = 0; i < 7; i++) {
            Location loc1 = LocationUtil.getLocationFromOffset(loc, -0.525 + i * 0.15, 0, 0);
            Location loc2 = LocationUtil.getLocationFromOffset(loc, 0, -0.525 + i * 0.15, 0);
            NeaceA1Info.Effects.USE_PARTICLE_2.play(loc1);
            NeaceA1Info.Effects.USE_PARTICLE_2.play(loc2);
        }
    }

    /**
     * 구원의 표식 상태 효과 클래스.
     */
    private final class ValueEffect implements StatusEffect {
        private double heal = 0;

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            NeaceA1Info.Effects.MARK.play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.5, 0));

            if (combatUser.isRemoved()) {
                combatEntity.getStatusEffectModule().remove(this);
                return;
            }

            if (!(combatEntity instanceof Healable) || ((Healable) combatEntity).getDamageModule().isFullHealth()
                    || ((NeaceWeapon) combatUser.getActionManager().getWeapon()).isHealing((Healable) combatEntity))
                return;

            double amount = NeaceA1Info.HEAL_PER_SECOND / 20.0;
            if (((Healable) combatEntity).getDamageModule().heal(combatUser, amount, true))
                heal += amount;

            if (heal >= NeaceA1Info.MAX_HEAL)
                combatEntity.getStatusEffectModule().remove(this);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            // 미사용
        }
    }
}
