package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.util.GlowUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;

public final class DeltaT1 {
    static void setLocked(@NonNull CombatUser attacker, @NonNull Damageable victim, int duration) {
        victim.getStatusEffectModule().applyStatusEffect(attacker, new Locked(), duration);
    }

    /**
     * 잠금 상태 처리 클래스
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Locked implements StatusEffect {
        private Location startLocation;
        private final Collection<Player> glowViewers = new ArrayList<>();

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

            if (provider instanceof CombatUser) {
                CombatUser providerUser = (CombatUser) provider;
                glowViewers.add(providerUser.getEntity());
                if (providerUser.getGameUser() != null && providerUser.getGameUser().getTeam() != null) {
                    for (GameUser providerTeamUser : providerUser.getGameUser().getTeam().getTeamUsers())
                        glowViewers.add(providerTeamUser.getPlayer());
                }
            }

            for (Player glowViewer: glowViewers)
                GlowUtil.setGlowing(combatEntity.getEntity(), ChatColor.DARK_GREEN, glowViewer);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            final Vector ZERO_VECTOR = new Vector();

            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§a§l잠김!", "", 0, 2, 10);

            combatEntity.getEntity().teleport(startLocation);
            combatEntity.getEntity().setVelocity(ZERO_VECTOR);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getEntity().setGravity(true);

            for (Player glowViewer: glowViewers)
                GlowUtil.removeGlowing(combatEntity.getEntity(), glowViewer);
        }

        @Override
        public long getCombatRestrictions(@NonNull Damageable combatEntity) {
            return CombatRestrictions.DEFAULT_MOVE | CombatRestrictions.ACTION_MOVE | CombatRestrictions.USE_ACTION |
                    CombatRestrictions.DAMAGED | CombatRestrictions.HEALED | CombatRestrictions.KNOCKBACKED;
        }
    }
}
