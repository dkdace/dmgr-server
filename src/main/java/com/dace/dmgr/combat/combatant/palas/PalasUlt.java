package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Targeted;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

@Getter
public final class PalasUlt extends UltimateSkill implements Targeted<Healable> {
    /** 공격력 수정자 */
    private static final AbilityStatus.Modifier DAMAGE_MODIFIER = new AbilityStatus.Modifier(PalasUltInfo.DAMAGE_INCREMENT);
    /** 이동 속도 수정자 */
    private static final AbilityStatus.Modifier SPEED_MODIFIER = new AbilityStatus.Modifier(PalasUltInfo.SPEED_INCREMENT);

    /** 타겟 모듈 */
    @NonNull
    private final TargetModule<Healable> targetModule;

    public PalasUlt(@NonNull CombatUser combatUser) {
        super(combatUser, PalasUltInfo.getInstance(), Timespan.MAX, PalasUltInfo.COST);
        this.targetModule = new TargetModule<>(this, PalasUltInfo.MAX_DISTANCE);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && targetModule.findTarget();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setCooldown();
        combatUser.getWeapon().cancel();

        Healable target = targetModule.getCurrentTarget();
        target.getStatusEffectModule().remove(PalasA2.PalasA2Immune.instance);
        target.getStatusEffectModule().apply(PalasUltBuff.instance, PalasUltInfo.DURATION);

        if (target instanceof CombatUser) {
            ((CombatUser) target).getUser().sendTitle("§c§l아드레날린 투여", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

            combatUser.addScore("아군 강화", PalasUltInfo.USE_SCORE);
            ((CombatUser) target).addKillHelper(combatUser, PalasUlt.this, PalasUltInfo.ASSIST_SCORE, PalasUltInfo.DURATION);
        }

        PalasUltInfo.SOUND.USE.play(combatUser.getLocation());

        Location location = target.getCenterLocation();
        PalasUltInfo.SOUND.HIT_ENTITY.play(location);
        PalasUltInfo.PARTICLE.HIT_ENTITY_CORE_1.play(location);
        PalasUltInfo.PARTICLE.HIT_ENTITY_CORE_2.play(location);

        for (Location loc : LocationUtil.getLine(combatUser.getArmLocation(MainHand.LEFT), location, 0.4))
            PalasUltInfo.PARTICLE.HIT_ENTITY_DECO.play(loc);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    @NonNull
    public CombatUtil.EntityCondition<Healable> getEntityCondition() {
        return CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)
                .and(combatEntity -> !combatEntity.getStatusEffectModule().has(PalasUltBuff.instance));
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
        public void onStart(@NonNull Damageable combatEntity) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().addModifier(DAMAGE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(SPEED_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            PalasUltInfo.PARTICLE.TICK.play(combatEntity.getCenterLocation());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().removeModifier(DAMAGE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(SPEED_MODIFIER);
        }
    }
}
