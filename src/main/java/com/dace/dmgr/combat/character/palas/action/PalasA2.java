package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

public final class PalasA2 extends ActiveSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(100);

    public PalasA2(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return PalasA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new PalasA2Target().shot();
    }

    @Override
    public boolean isCancellable() {
        return false;
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
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getStatusEffectModule().getResistanceStatus().addModifier(MODIFIER);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            PalasA2Info.PARTICLE.TICK.play(combatEntity.getCenterLocation());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getStatusEffectModule().getResistanceStatus().removeModifier(MODIFIER);
        }
    }

    private final class PalasA2Target extends Target<Healable> {
        private PalasA2Target() {
            super(combatUser, PalasA2Info.MAX_DISTANCE, true, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)
                    .and(combatEntity -> !combatEntity.getStatusEffectModule().has(PalasA2Immune.instance)));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            setCooldown();
            combatUser.getWeapon().onCancelled();

            target.getStatusEffectModule().remove(PalasUlt.PalasUltBuff.instance);
            target.getStatusEffectModule().clear(false);
            target.getStatusEffectModule().apply(PalasA2Immune.instance, combatUser, Timespan.ofTicks(PalasA2Info.DURATION));

            if (target instanceof CombatUser) {
                ((CombatUser) target).getUser().sendTitle("§e§l해로운 효과 면역", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

                combatUser.addScore("해로운 효과 면역", PalasA2Info.USE_SCORE);
                ((CombatUser) target).addKillHelper(combatUser, PalasA2.this, PalasA2Info.ASSIST_SCORE, Timespan.ofTicks(PalasA2Info.DURATION));
            }

            PalasA2Info.SOUND.USE.play(combatUser.getLocation());
            PalasA2Info.SOUND.HIT_ENTITY.play(target.getCenterLocation());
            PalasA2Info.PARTICLE.HIT_ENTITY_CORE.play(target.getCenterLocation());

            Location location = combatUser.getArmLocation(MainHand.LEFT);
            for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4))
                PalasA2Info.PARTICLE.HIT_ENTITY_DECO.play(loc);
        }
    }
}
