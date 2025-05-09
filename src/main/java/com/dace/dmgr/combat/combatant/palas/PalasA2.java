package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Targeted;
import com.dace.dmgr.combat.action.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

@Getter
public final class PalasA2 extends ActiveSkill implements Targeted<Healable> {
    /** 상태 효과 저항 수정자 */
    private static final AbilityStatus.Modifier STATUS_EFFECT_RESISTANCE_MODIFIER = new AbilityStatus.Modifier(100);
    /** 넉백 저항 수정자 */
    private static final AbilityStatus.Modifier KNOCKBACK_RESISTANCE_MODIFIER = new AbilityStatus.Modifier(100);

    /** 타겟 모듈 */
    @NonNull
    private final TargetModule<Healable> targetModule;

    public PalasA2(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA2Info.getInstance(), PalasA2Info.COOLDOWN, Timespan.MAX, 1);
        this.targetModule = new TargetModule<>(this, PalasA2Info.MAX_DISTANCE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && targetModule.findTarget();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        combatUser.getActionManager().getWeapon().cancel();

        Healable target = targetModule.getCurrentTarget();
        target.getStatusEffectModule().remove(PalasUlt.PalasUltBuff.instance);
        target.getStatusEffectModule().clear(false);
        target.getStatusEffectModule().apply(PalasA2Immune.instance, PalasA2Info.DURATION);

        if (target instanceof CombatUser) {
            ((CombatUser) target).getUser().sendTitle("§e§l해로운 효과 면역", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
            ((CombatUser) target).addKillHelper(combatUser, PalasA2.this, PalasA2Info.ASSIST_SCORE, PalasA2Info.DURATION);
        }
        if (target.isGoalTarget())
            combatUser.addScore("해로운 효과 면역", PalasA2Info.USE_SCORE);

        PalasA2Info.Sounds.USE.play(combatUser.getLocation());

        Location location = target.getCenterLocation();
        PalasA2Info.Sounds.HIT_ENTITY.play(location);
        PalasA2Info.Particles.HIT_ENTITY_CORE.play(location);

        for (Location loc : LocationUtil.getLine(combatUser.getArmLocation(MainHand.LEFT), location, 0.4))
            PalasA2Info.Particles.HIT_ENTITY_DECO.play(loc);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    @NonNull
    public EntityCondition<Healable> getEntityCondition() {
        return EntityCondition.team(combatUser).exclude(combatUser)
                .and(combatEntity -> !combatEntity.getStatusEffectModule().has(PalasA2Immune.instance));
    }

    /**
     * 해로운 효과 면역 상태 효과 클래스.
     */
    static final class PalasA2Immune extends StatusEffect {
        static final PalasA2Immune instance = new PalasA2Immune();

        private PalasA2Immune() {
            super(true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            combatEntity.getStatusEffectModule().getResistanceStatus().addModifier(STATUS_EFFECT_RESISTANCE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getResistanceStatus().addModifier(KNOCKBACK_RESISTANCE_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            PalasA2Info.Particles.TICK.play(combatEntity.getCenterLocation());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            combatEntity.getStatusEffectModule().getResistanceStatus().removeModifier(STATUS_EFFECT_RESISTANCE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getResistanceStatus().removeModifier(KNOCKBACK_RESISTANCE_MODIFIER);
        }
    }
}
