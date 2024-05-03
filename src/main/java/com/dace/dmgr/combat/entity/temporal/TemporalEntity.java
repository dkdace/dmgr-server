package com.dace.dmgr.combat.entity.temporal;

import com.dace.dmgr.combat.entity.AbstractCombatEntity;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.Game;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 전투 시스템의 일시적인 엔티티 클래스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 * @see SummonEntity
 */
public abstract class TemporalEntity<T extends Entity> extends AbstractCombatEntity<T> {
    /**
     * 일시적 엔티티 인스턴스를 생성한다.
     *
     * @param entity   대상 엔티티
     * @param name     이름
     * @param game     소속된 게임. {@code null}이면 게임에 참여중이지 않음을 나타냄
     * @param hitboxes 히트박스 목록
     * @throws IllegalStateException 해당 {@code entity}의 CombatEntity가 이미 존재하면 발생
     */
    protected TemporalEntity(@NonNull T entity, @NonNull String name, @Nullable Game game, @NonNull Hitbox @NonNull ... hitboxes) {
        super(entity, name, game, hitboxes);
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();

        getEntity().remove();
    }

    @Override
    public boolean canBeTargeted() {
        return isActivated;
    }

    @Override
    @NonNull
    public String getTeamIdentifier() {
        return toString();
    }
}
