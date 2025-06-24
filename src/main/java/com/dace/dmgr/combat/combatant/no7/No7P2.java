package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Set;

public final class No7P2 extends PassiveSkill {
    public No7P2(@NonNull CombatUser combatUser, @NonNull No7P2Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.PERIODIC_2);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getAbilityManager().getTrait(No7T1Info.getInstance()).getShield() > 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new No7P2Area().emit(combatUser.getEntity().getLocation().add(0, 1, 0));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    void onTick(long i) {
        if (i % 5 == 0)
            combatUser.getAbilityManager().useAction(ActionKey.PERIODIC_2);
    }

    private final class No7P2Area extends Area<Damageable> {
        private No7P2Area() {
            super(combatUser, No7P2Info.RADIUS, EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            double power = combatUser.getAbilityManager().getTrait(No7T1Info.getInstance()).getShield() / No7T1Info.MAX_SHIELD;
            double damage = (No7P2Info.MIN_DAMAGE_PER_SECOND + power * (No7P2Info.MAX_DAMAGE_PER_SECOND - No7P2Info.MIN_DAMAGE_PER_SECOND)) * 5 / 20.0;

            target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, null, false, true);

            No7P2Info.Effects.playHitEntity(center, location, power);

            return !(target instanceof Barrier);
        }
    }
}
