package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.EntitySpawnHandler;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.Team;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
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
    /** 이름표 홀로그램 */
    @Nullable
    private TextHologram nameTagHologram;

    /**
     * 소환 가능한 엔티티 인스턴스를 생성한다.
     *
     * @param entitySpawnHandler 엔티티 생성 처리기
     * @param spawnLocation      생성 위치
     * @param name               이름
     * @param owner              엔티티를 소환한 플레이어
     * @param hasNameTag         아군에게 이름표 표시 여부
     * @param hitboxes           히트박스 목록
     * @throws IllegalStateException {@code spawnLocation}에 엔티티를 소환할 수 없으면 발생
     */
    protected SummonEntity(@NonNull EntitySpawnHandler<T> entitySpawnHandler, @NonNull Location spawnLocation, @NonNull String name,
                           @NonNull CombatUser owner, boolean hasNameTag, @NonNull Hitbox @NonNull ... hitboxes) {
        super(entitySpawnHandler, spawnLocation, name, hitboxes);
        this.owner = owner;

        if (hasNameTag) {
            addTask(new DelayTask(() ->
                    nameTagHologram = new TextHologram(entity, player -> {
                        CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(player));
                        return targetCombatUser == null || !owner.isEnemy(targetCombatUser);
                    }, 1, "§n" + name), 3));
            addOnRemove(() -> {
                if (nameTagHologram != null)
                    nameTagHologram.remove();
            });
        }
    }

    @Override
    @Nullable
    public final Game getGame() {
        return owner.getGame();
    }

    @Override
    @Nullable
    public final Team getTeam() {
        return owner.getTeam();
    }

    @Override
    public final boolean isEnemy(@NonNull CombatEntity target) {
        if (target == this)
            return false;

        return owner.isEnemy(target);
    }
}
