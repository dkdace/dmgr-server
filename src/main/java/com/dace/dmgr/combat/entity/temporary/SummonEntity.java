package com.dace.dmgr.combat.entity.temporary;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.user.User;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 전투에서 일시적으로 사용하는 엔티티 중 플레이어가 소환할 수 있는 엔티티 클래스.
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 */
public abstract class SummonEntity<T extends Entity> extends TemporaryEntity<T> {
    /** 엔티티를 소환한 플레이어 */
    @NonNull
    @Getter
    protected final CombatUser owner;
    /** 아군에게 이름표 표시 여부 */
    private final boolean showNameTag;
    /** 이름표 홀로그램 */
    @Nullable
    private TextHologram nameTagHologram;

    /**
     * 소환 가능한 엔티티 인스턴스를 생성한다.
     *
     * @param entity      대상 엔티티
     * @param name        이름
     * @param owner       엔티티를 소환한 플레이어
     * @param showNameTag 아군에게 이름표 표시 여부
     * @param isHidden    엔티티를 보이지 할지 여부
     * @param hitboxes    히트박스 목록
     * @throws IllegalStateException 해당 {@code entity}의 CombatEntity가 이미 존재하면 발생
     */
    protected SummonEntity(@NonNull T entity, @NonNull String name, @NonNull CombatUser owner, boolean showNameTag, boolean isHidden,
                           @NonNull Hitbox @NonNull ... hitboxes) {
        super(entity, name, owner.getGame(), hitboxes);

        this.owner = owner;
        this.showNameTag = showNameTag;

        if (showNameTag)
            TaskUtil.addTask(this, new DelayTask(() ->
                    nameTagHologram = new TextHologram(entity, player -> {
                        CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(player));
                        return targetCombatUser == null || !owner.isEnemy(targetCombatUser);
                    }, 1, "§n" + name), 3));
        if (isHidden)
            hide();
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();

        if (showNameTag && nameTagHologram != null)
            nameTagHologram.dispose();
    }

    @Override
    @NonNull
    public final String getTeamIdentifier() {
        return owner.getTeamIdentifier();
    }

    /**
     * 엔티티를 보이지 않게 한다.
     */
    private void hide() {
        WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
        packet.setEntityIds(new int[]{getEntity().getEntityId()});

        entity.getWorld().getPlayers().forEach(target -> {
            CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(target));
            if (targetCombatUser != null)
                packet.sendPacket(target);
        });
    }
}
