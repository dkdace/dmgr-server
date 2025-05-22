package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class No7P2 extends AbstractSkill {
    public No7P2(@NonNull CombatUser combatUser) {
        super(combatUser, No7P2Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_2};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getActionManager().getTrait(No7T1Info.getInstance()).getShield() > 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new No7P2Area().emit(combatUser.getEntity().getLocation().add(0, 1, 0));
    }

    @Override
    public boolean isCancellable() {
        return false;
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
            double power = combatUser.getActionManager().getTrait(No7T1Info.getInstance()).getShield() / No7T1Info.MAX_SHIELD;
            double damage = (No7P2Info.MIN_DAMAGE_PER_SECOND + power * (No7P2Info.MAX_DAMAGE_PER_SECOND - No7P2Info.MIN_DAMAGE_PER_SECOND)) * 5 / 20.0;

            target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, null, false, true);

            No7P2Info.Particles.HIT_ENTITY.play(location, power);
            No7P2Info.Sounds.HIT_ENTITY.play(location, power);
            for (Location loc : LocationUtil.getLine(center, location, 0.4))
                CombatEffectUtil.BULLET_TRAIL_PARTICLE.play(loc);

            return !(target instanceof Barrier);
        }
    }
}
