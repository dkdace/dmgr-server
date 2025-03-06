package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

public final class NeaceA1 extends ActiveSkill {
    public NeaceA1(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new NeaceA1Target().shot();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 치유 표식 상태 효과 클래스.
     */
    public static final class NeaceA1Mark extends ValueStatusEffect {
        public NeaceA1Mark() {
            super(StatusEffectType.NONE, true, NeaceA1Info.MAX_HEAL);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            NeaceA1Info.PARTICLE.MARK.play(combatEntity.getLocation().add(0, combatEntity.getHeight() + 0.5, 0));

            if (!(combatEntity instanceof Healable) || !(provider instanceof Healer))
                return;
            if (((Healable) combatEntity).getDamageModule().isFullHealth())
                return;

            if (getValue() >= NeaceA1Info.MAX_HEAL) {
                combatEntity.getStatusEffectModule().remove(this);
                return;
            }

            if (((Healable) combatEntity).getDamageModule().heal((Healer) provider, NeaceA1Info.HEAL_PER_SECOND / 20.0, true))
                setValue(getValue() + NeaceA1Info.HEAL_PER_SECOND / 20);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            setValue(0);
        }
    }

    private final class NeaceA1Target extends Target<Healable> {
        private NeaceA1Target() {
            super(combatUser, NeaceA1Info.MAX_DISTANCE, true, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)
                    .and(combatEntity -> !combatEntity.getStatusEffectModule().has(ValueStatusEffect.Type.HEALING_MARK)));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            setCooldown();

            target.getStatusEffectModule().apply(ValueStatusEffect.Type.HEALING_MARK, combatUser, Timespan.ofTicks(NeaceA1Info.DURATION));

            NeaceA1Info.SOUND.USE.play(combatUser.getLocation());
            playUseEffect(target);
        }

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
                    angle += 72;
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
    }
}
