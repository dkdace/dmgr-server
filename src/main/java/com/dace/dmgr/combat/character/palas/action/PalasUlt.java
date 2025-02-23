package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;

public final class PalasUlt extends UltimateSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "PalasUlt";

    public PalasUlt(@NonNull CombatUser combatUser) {
        super(combatUser, PalasUltInfo.getInstance());
    }

    @Override
    public int getCost() {
        return PalasUltInfo.COST;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new PalasUltTarget().shot();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 아드레날린 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PalasUltBuff implements StatusEffect {
        static final PalasUltBuff instance = new PalasUltBuff();

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
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().addModifier(MODIFIER_ID, PalasUltInfo.DAMAGE_INCREMENT);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, PalasUltInfo.SPEED_INCREMENT);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            PalasUltInfo.PARTICLE.TICK.play(combatEntity.getCenterLocation());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().removeModifier(MODIFIER_ID);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }

    private final class PalasUltTarget extends Target<Healable> {
        private PalasUltTarget() {
            super(combatUser, PalasUltInfo.MAX_DISTANCE, true, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)
                    .and(combatEntity -> !combatEntity.getStatusEffectModule().hasStatusEffect(PalasUltBuff.instance)));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            PalasUlt.super.onUse(ActionKey.SLOT_4);

            setCooldown();
            combatUser.getWeapon().onCancelled();

            target.getStatusEffectModule().removeStatusEffect(PalasA2.PalasA2Immune.instance);
            target.getStatusEffectModule().applyStatusEffect(combatUser, PalasUltBuff.instance, PalasUltInfo.DURATION);

            if (target instanceof CombatUser) {
                ((CombatUser) target).getUser().sendTitle("§c§l아드레날린 투여", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

                combatUser.addScore("아군 강화", PalasUltInfo.USE_SCORE);
                ((CombatUser) target).addKillAssist(combatUser, PalasUlt.this, PalasUltInfo.ASSIST_SCORE, PalasUltInfo.DURATION);
            }

            PalasUltInfo.SOUND.USE.play(combatUser.getEntity().getLocation());
            PalasUltInfo.SOUND.HIT_ENTITY.play(target.getCenterLocation());
            PalasUltInfo.PARTICLE.HIT_ENTITY_CORE_1.play(target.getCenterLocation());
            PalasUltInfo.PARTICLE.HIT_ENTITY_CORE_2.play(target.getCenterLocation());

            Location location = combatUser.getArmLocation(false);
            for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4))
                PalasUltInfo.PARTICLE.HIT_ENTITY_DECO.play(loc);
        }
    }
}
