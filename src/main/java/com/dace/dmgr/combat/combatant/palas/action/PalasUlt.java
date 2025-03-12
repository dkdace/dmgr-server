package com.dace.dmgr.combat.combatant.palas.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

public final class PalasUlt extends UltimateSkill {
    /** 공격력 수정자 */
    private static final AbilityStatus.Modifier DAMAGE_MODIFIER = new AbilityStatus.Modifier(PalasUltInfo.DAMAGE_INCREMENT);
    /** 이동 속도 수정자 */
    private static final AbilityStatus.Modifier SPEED_MODIFIER = new AbilityStatus.Modifier(PalasUltInfo.SPEED_INCREMENT);

    public PalasUlt(@NonNull CombatUser combatUser) {
        super(combatUser, PalasUltInfo.getInstance(), Timespan.MAX, PalasUltInfo.COST);
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
    static final class PalasUltBuff extends StatusEffect {
        static final PalasUltBuff instance = new PalasUltBuff();

        private PalasUltBuff() {
            super(true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().addModifier(DAMAGE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(SPEED_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            PalasUltInfo.PARTICLE.TICK.play(combatEntity.getCenterLocation());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().removeModifier(DAMAGE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(SPEED_MODIFIER);
        }
    }

    private final class PalasUltTarget extends Target<Healable> {
        private PalasUltTarget() {
            super(combatUser, PalasUltInfo.MAX_DISTANCE, true, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)
                    .and(combatEntity -> !combatEntity.getStatusEffectModule().has(PalasUltBuff.instance)));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            PalasUlt.super.onUse(ActionKey.SLOT_4);

            setCooldown();
            combatUser.getWeapon().onCancelled();

            target.getStatusEffectModule().remove(PalasA2.PalasA2Immune.instance);
            target.getStatusEffectModule().apply(PalasUltBuff.instance, combatUser, PalasUltInfo.DURATION);

            if (target instanceof CombatUser) {
                ((CombatUser) target).getUser().sendTitle("§c§l아드레날린 투여", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

                combatUser.addScore("아군 강화", PalasUltInfo.USE_SCORE);
                ((CombatUser) target).addKillHelper(combatUser, PalasUlt.this, PalasUltInfo.ASSIST_SCORE, PalasUltInfo.DURATION);
            }

            PalasUltInfo.SOUND.USE.play(combatUser.getLocation());
            PalasUltInfo.SOUND.HIT_ENTITY.play(target.getCenterLocation());
            PalasUltInfo.PARTICLE.HIT_ENTITY_CORE_1.play(target.getCenterLocation());
            PalasUltInfo.PARTICLE.HIT_ENTITY_CORE_2.play(target.getCenterLocation());

            Location location = combatUser.getArmLocation(MainHand.LEFT);
            for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4))
                PalasUltInfo.PARTICLE.HIT_ENTITY_DECO.play(loc);
        }
    }
}
