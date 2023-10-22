package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.system.EntityInfoRegistry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * 전투에서 일시적으로 사용하는 엔티티 중 플레이어가 소환할 수 있는 엔티티 클래스.
 */
public abstract class SummonEntity<T extends LivingEntity> extends TemporalEntity<T> {
    /** 엔티티를 소환한 플레이어 */
    @Getter
    protected CombatUser owner;

    /**
     * @param entity    대상 엔티티
     * @param name      이름
     * @param maxHealth 최대 체력
     * @param owner     엔티티를 소환한 플레이어
     * @param hitbox    히트박스
     */
    protected SummonEntity(T entity, String name, int maxHealth, CombatUser owner, Hitbox... hitbox) {
        super(entity, name, maxHealth, hitbox);
        this.owner = owner;
    }

    @Override
    public void onAttack(Damageable victim, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        owner.onAttack(victim, damage, damageType, isCrit, isUlt);
    }

    @Override
    public void onKill(CombatEntity victim) {
        owner.onKill(victim);
    }

    /**
     * 엔티티를 모든 적에게 보이지 않게 한다.
     */
    protected final void hideForEnemies() {
        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{entity.getEntityId()});

        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            CombatUser combatUser2 = EntityInfoRegistry.getCombatUser(player2);
            if (combatUser2 != null && owner.isEnemy(combatUser2))
                packet.sendPacket(player2);
        });
    }
}
