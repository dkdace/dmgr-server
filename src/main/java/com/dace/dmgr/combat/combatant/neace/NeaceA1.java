package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Targeted;
import com.dace.dmgr.combat.action.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

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

        NeaceA1Mark neaceA1Mark = target.getStatusEffectModule().apply(ValueStatusEffect.Type.HEALING_MARK, NeaceA1Info.DURATION);
        neaceA1Mark.provider = combatUser;

        NeaceA1Info.SOUND.USE.play(combatUser.getLocation());
        playUseEffect(target);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    @NonNull
    public CombatUtil.EntityCondition<Healable> getEntityCondition() {
        return CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)
                .and(combatEntity -> !combatEntity.getStatusEffectModule().has(ValueStatusEffect.Type.HEALING_MARK));
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param target 사용 대상
     */
    private void playUseEffect(@NonNull Healable target) {
        Location location = combatUser.getArmLocation(MainHand.RIGHT);
        for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4))
            NeaceA1Info.PARTICLE.HIT_ENTITY.play(loc);

        Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);
        Vector vector = VectorUtil.getYawAxis(loc).multiply(0.8);
        Vector axis = VectorUtil.getRollAxis(loc);

        for (int i = 0; i < 8; i++) {
            int angle = i * 10;

            for (int j = 0; j < 10; j++) {
                angle += 360 / 5;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 5 ? angle : -angle).multiply(1 + i * 0.2);

                NeaceA1Info.PARTICLE.USE.play(loc.clone().add(vec));
            }
        }
        for (int i = 0; i < 7; i++) {
            Location loc1 = LocationUtil.getLocationFromOffset(loc, -0.525 + i * 0.15, 0, 0);
            Location loc2 = LocationUtil.getLocationFromOffset(loc, 0, -0.525 + i * 0.15, 0);
            NeaceA1Info.PARTICLE.USE.play(loc1);
            NeaceA1Info.PARTICLE.USE.play(loc2);
        }
    }

    /**
     * 치유 표식 상태 효과 클래스.
     */
    public static final class NeaceA1Mark extends ValueStatusEffect {
        /** 제공자 */
        @Nullable
        private CombatUser provider;

        public NeaceA1Mark() {
            super(StatusEffectType.NONE, true, NeaceA1Info.MAX_HEAL);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            NeaceA1Info.PARTICLE.MARK.play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.5, 0));

            if (!(combatEntity instanceof Healable) || ((Healable) combatEntity).getDamageModule().isFullHealth())
                return;

            if (provider == null || provider.isRemoved()) {
                combatEntity.getStatusEffectModule().remove(this);
                return;
            }

            double amount = NeaceA1Info.HEAL_PER_SECOND / 20.0;
            if (((Healable) combatEntity).getDamageModule().heal(provider, amount, true))
                setValue(getValue() + amount);

            if (getValue() >= NeaceA1Info.MAX_HEAL)
                combatEntity.getStatusEffectModule().remove(this);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            setValue(0);
            provider = null;
        }
    }
}
