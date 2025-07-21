package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.Targeted;
import com.dace.dmgr.combat.ability.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.inventory.MainHand;

public final class PalasA2 extends ActiveSkill implements Targeted<Healable> {
    /** 상태 효과 저항 수정자 */
    private static final Modifier STATUS_EFFECT_RESISTANCE_MODIFIER = new Modifier(100);
    /** 넉백 저항 수정자 */
    private static final Modifier KNOCKBACK_RESISTANCE_MODIFIER = new Modifier(100);

    /** 타겟 모듈 */
    private final TargetModule<Healable> targetModule;

    public PalasA2(@NonNull CombatUser combatUser, @NonNull PalasA2Info skillInfo) {
        super(combatUser, skillInfo, PalasA2Info.COOLDOWN, Timespan.MAX);
        this.targetModule = new TargetModule<>(this);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && targetModule.findTarget();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_2;
    }

    @Override
    public void onSlot() {
        setCooldown();
        combatUser.getAbilityManager().getWeapon().cancel();

        Healable target = targetModule.getCurrentTarget();
        target.getStatusEffectModule().remove(PalasUlt.PalasUltBuff.instance);
        target.getStatusEffectModule().clear(false);
        target.getStatusEffectModule().apply(PalasA2Immune.instance, PalasA2Info.DURATION);

        if (target instanceof CombatUser) {
            ((CombatUser) target).getUser().sendTitle("§e§l해로운 효과 면역", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
            ((CombatUser) target).addKillHelper(combatUser, PalasA2.this, PalasA2Info.ASSIST_SCORE, PalasA2Info.DURATION);
        }
        if (target.isGoalTarget())
            combatUser.addScore(PalasA2Info.USE_SCORE);

        PalasA2Info.Effects.playUse(combatUser.getLocation(), combatUser.getArmLocation(MainHand.LEFT), target.getCenterLocation());
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public int getTargetMaxDistance() {
        return PalasA2Info.MAX_DISTANCE;
    }

    @Override
    @NonNull
    public EntityCondition<Healable> getTargetEntityCondition() {
        return EntityCondition.team(combatUser).exclude(combatUser)
                .and(combatEntity -> !combatEntity.getStatusEffectModule().has(PalasA2Immune.instance));
    }

    /**
     * 해로운 효과 면역 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PalasA2Immune implements StatusEffect {
        static final PalasA2Immune instance = new PalasA2Immune();

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            combatEntity.getStatusEffectModule().addModifier(STATUS_EFFECT_RESISTANCE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getKnockbackModule().addModifier(KNOCKBACK_RESISTANCE_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            PalasA2Info.Effects.IMMUNE_TICK.play(combatEntity.getCenterLocation());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            combatEntity.getStatusEffectModule().removeModifier(STATUS_EFFECT_RESISTANCE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getKnockbackModule().removeModifier(KNOCKBACK_RESISTANCE_MODIFIER);
        }
    }
}
