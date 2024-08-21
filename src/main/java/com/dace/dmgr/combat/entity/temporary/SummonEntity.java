package com.dace.dmgr.combat.entity.temporary;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.HologramUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 전투에서 일시적으로 사용하는 엔티티 중 플레이어가 소환할 수 있는 엔티티 클래스.
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 */
@Getter
public abstract class SummonEntity<T extends Entity> extends TemporaryEntity<T> {
    /** 이름표 홀로그램 ID */
    public static final String NAMETAG_HOLOGRAM_ID = "NameTag";
    /** 엔티티를 소환한 플레이어 */
    @NonNull
    protected final CombatUser owner;

    /**
     * 소환 가능한 엔티티 인스턴스를 생성한다.
     *
     * @param entity         대상 엔티티
     * @param name           이름
     * @param owner          엔티티를 소환한 플레이어
     * @param showNameTag    아군에게 이름표 표시 여부
     * @param hideForEnemies 엔티티를 적에게 보이지 할 지 여부
     * @param hitboxes       히트박스 목록
     * @throws IllegalStateException 해당 {@code entity}의 CombatEntity가 이미 존재하면 발생
     */
    protected SummonEntity(@NonNull T entity, @NonNull String name, @NonNull CombatUser owner, boolean showNameTag, boolean hideForEnemies,
                           @NonNull Hitbox @NonNull ... hitboxes) {
        super(entity, name, owner.getGame(), hitboxes);

        this.owner = owner;

        if (showNameTag)
            TaskUtil.addTask(this, new DelayTask(this::showNameTag, 5));
        if (hideForEnemies)
            hideForEnemies();
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();
        HologramUtil.removeHologram(NAMETAG_HOLOGRAM_ID + this);
    }

    @Override
    @NonNull
    public final String getTeamIdentifier() {
        return owner.getTeamIdentifier();
    }

    /**
     * 모든 아군에게 엔티티의 이름표를 표시한다.
     */
    private void showNameTag() {
        HologramUtil.addHologram(NAMETAG_HOLOGRAM_ID + this, entity, 0, entity.getHeight() + 0.7, 0, "§n" + name);

        Bukkit.getOnlinePlayers().forEach((Player target) -> {
            CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(target));
            if (targetCombatUser != null && getOwner().isEnemy(targetCombatUser))
                HologramUtil.setHologramVisibility(NAMETAG_HOLOGRAM_ID + this, false, target);
        });
    }

    /**
     * 엔티티를 모든 적에게 보이지 않게 한다.
     */
    private void hideForEnemies() {
        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{getEntity().getEntityId()});

        Bukkit.getOnlinePlayers().forEach((Player target) -> {
            CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(target));
            if (targetCombatUser != null && getOwner().isEnemy(targetCombatUser))
                packet.sendPacket(target);
        });
    }
}
