package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.skill.Targeted;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.skill.module.TargetModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.inventory.MainHand;

@Getter
public final class PalasUlt extends UltimateSkill implements Targeted<Healable> {
    /** 공격력 수정자 */
    private static final Modifier DAMAGE_MODIFIER = new Modifier(PalasUltInfo.DAMAGE_INCREMENT);
    /** 이동 속도 수정자 */
    private static final Modifier SPEED_MODIFIER = new Modifier(PalasUltInfo.SPEED_INCREMENT);

    /** 타겟 모듈 */
    @NonNull
    private final TargetModule<Healable> targetModule;

    public PalasUlt(@NonNull CombatUser combatUser, @NonNull PalasUltInfo skillInfo) {
        super(combatUser, skillInfo, Timespan.MAX, PalasUltInfo.COST);
        this.targetModule = new TargetModule<>(this);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && targetModule.findTarget();
    }

    @Override
    public void onSlot() {
        setCooldown();
        combatUser.getAbilityManager().getWeapon().cancel();

        Healable target = targetModule.getCurrentTarget();
        target.getStatusEffectModule().remove(PalasA2.PalasA2Immune.instance);
        target.getStatusEffectModule().apply(PalasUltBuff.instance, PalasUltInfo.DURATION);

        if (target instanceof CombatUser) {
            ((CombatUser) target).getUser().sendTitle("§c§l아드레날린 투여", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
            ((CombatUser) target).addKillHelper(combatUser, PalasUlt.this, PalasUltInfo.ASSIST_SCORE, PalasUltInfo.DURATION);
        }
        if (target.isGoalTarget())
            combatUser.addScore(PalasUltInfo.USE_SCORE);

        PalasUltInfo.Effects.playUse(combatUser.getLocation(), combatUser.getArmLocation(MainHand.LEFT), target.getCenterLocation());
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public int getMaxDistance() {
        return PalasUltInfo.MAX_DISTANCE;
    }

    @Override
    @NonNull
    public EntityCondition<Healable> getEntityCondition() {
        return EntityCondition.team(combatUser).exclude(combatUser)
                .and(combatEntity -> !combatEntity.getStatusEffectModule().has(PalasUltBuff.instance));
    }

    /**
     * 아드레날린 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PalasUltBuff implements StatusEffect {
        static final PalasUltBuff instance = new PalasUltBuff();

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackerModule().addModifier(DAMAGE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().addModifier(SPEED_MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            PalasUltInfo.Effects.BUFF_TICK.play(combatEntity.getCenterLocation());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackerModule().removeModifier(DAMAGE_MODIFIER);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().removeModifier(SPEED_MODIFIER);
        }
    }
}
