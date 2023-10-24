package com.dace.dmgr.combat.entity.temporal;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 플레이어가 소환할 수 있는 엔티티의 인터페이스.
 */
public interface Summonable extends Temporal {
    /**
     * 엔티티를 소환한 플레이어를 반환한다.
     *
     * @return 엔티티를 소환한 플레이어
     */
    CombatUser getOwner();

    /**
     * 엔티티를 모든 적에게 보이지 않게 한다.
     */
    default void hideForEnemies() {
        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{getEntity().getEntityId()});

        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            CombatUser combatUser2 = EntityInfoRegistry.getCombatUser(player2);
            if (combatUser2 != null && getOwner().isEnemy(combatUser2))
                packet.sendPacket(player2);
        });
    }
}
