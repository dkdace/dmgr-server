package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.Team;
import com.dace.dmgr.system.EntityInfoRegistry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 전투에서 일시적으로 사용하는 엔티티 중 플레이어가 소환할 수 있는 엔티티 클래스.
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
@Getter
public abstract class SummonEntity<T extends LivingEntity> extends TemporalEntity<T> {
    /** 엔티티를 소환한 플레이어 */
    protected final CombatUser owner;

    /**
     * 소환 가능한 엔티티 인스턴스를 생성한다.
     *
     * @param entity 대상 엔티티
     * @param name   이름
     * @param owner  엔티티를 소환한 플레이어
     * @param hitbox 히트박스 목록
     */
    protected SummonEntity(T entity, String name, CombatUser owner, Hitbox... hitbox) {
        super(entity, name, hitbox);
        this.owner = owner;
    }

    @Override
    public final Game getGame() {
        return owner.getGame();
    }

    @Override
    @MustBeInvokedByOverriders
    public void init() {
        super.init();

        setTeam(owner.getTeam());
    }

    @Override
    public boolean isEnemy(CombatEntity combatEntity) {
        if (combatEntity == this || owner == combatEntity)
            return false;
        if (combatEntity instanceof SummonEntity && ((SummonEntity<?>) combatEntity).owner == owner)
            return false;
        if (getTeam() == Team.NONE || combatEntity.getTeam() == Team.NONE)
            return true;
        return !getTeam().equals(combatEntity.getTeam());
    }

    /**
     * 엔티티를 모든 적에게 보이지 않게 한다.
     */
    protected final void hideForEnemies() {
        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{getEntity().getEntityId()});

        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            CombatUser combatUser2 = EntityInfoRegistry.getCombatUser(player2);
            if (combatUser2 != null && getOwner().isEnemy(combatUser2))
                packet.sendPacket(player2);
        });
    }
}
