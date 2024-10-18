package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class DeltaT1 {
    static void setLocked(@NonNull CombatUser attacker, @NonNull Damageable victim) {

    }

    /**
     * 잠금 상태 처리 클래스
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Locked implements StatusEffect {
        private Location startLocation;

        @Override
        public @NonNull StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return false;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getEntity().setGravity(false);

            startLocation = combatEntity.getEntity().getLocation();
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§a§l잠김!", "", 0, 2, 10);

            combatEntity.getEntity().setVelocity(new Vector(0, 0, 0));
            combatEntity.getEntity().teleport(startLocation);
            CombatUtil.setYawAndPitch(combatEntity.getEntity(), combatEntity.getEntity().getLocation().getYaw(), combatEntity.getEntity().getLocation().getPitch());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getEntity().setGravity(true);
        }

        @Override
        public long getCombatRestrictions(@NonNull Damageable combatEntity) {
            return CombatRestrictions.DEFAULT_MOVE | CombatRestrictions.ACTION_MOVE | CombatRestrictions.USE_ACTION |
                    CombatRestrictions.DAMAGED | CombatRestrictions.HEALED;
        }
    }
}
