package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.Task;
import com.dace.dmgr.util.task.TaskManager;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.LongConsumer;

/**
 * {@link CombatEntity}의 기본 구현체, 모든 전투 시스템 엔티티의 기반 클래스.
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 */
public abstract class AbstractCombatEntity<T extends Entity> implements CombatEntity {
    /** 전투 시스템 엔티티 목록 (엔티티 : 전투 시스템 엔티티) */
    private static final HashMap<Entity, CombatEntity> COMBAT_ENTITY_MAP = new HashMap<>();
    /** 게임에 소속되지 않은 전투 시스템 엔티티 목록 (엔티티 : 전투 시스템 엔티티) */
    private static final HashMap<Entity, CombatEntity> COMBAT_ENTITY_EXCLUDED_MAP = new HashMap<>();

    /** 엔티티 인스턴스 */
    @NonNull
    @Getter
    protected final T entity;
    /** 이름 */
    @NonNull
    @Getter
    protected final String name;
    /** 소속된 게임 */
    @Nullable
    @Getter
    protected final Game game;
    /** 태스크 관리 인스턴스 */
    private final TaskManager taskManager = new TaskManager();
    /** 매 틱마다 실행할 작업 목록 */
    private final ArrayList<LongConsumer> onTicks = new ArrayList<>();
    /** 제거 시 실행할 작업 목록 */
    private final ArrayList<Runnable> onDisposes = new ArrayList<>();

    /** 히트박스 목록 */
    protected Hitbox[] hitboxes;
    /** 히트박스 기준 위치 */
    private Location hitboxBaseLocation;

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성한다.
     *
     * @param entity   대상 엔티티
     * @param name     이름
     * @param game     소속된 게임. {@code null}이면 게임에 참여중이지 않음을 나타냄
     * @param hitboxes 히트박스 목록
     * @throws IllegalStateException 해당 {@code entity}의 CombatEntity가 이미 존재하면 발생
     */
    protected AbstractCombatEntity(@NonNull T entity, @NonNull String name, @Nullable Game game, @NonNull Hitbox @NonNull ... hitboxes) {
        Validate.validState(COMBAT_ENTITY_MAP.get(entity) == null, "CombatEntity가 이미 존재함");

        this.entity = entity;
        this.name = name;
        this.game = game;
        this.hitboxes = hitboxes;
        this.hitboxBaseLocation = entity.getLocation();

        entity.setCustomName(ChatColor.WHITE + name);
        if (game == null)
            COMBAT_ENTITY_EXCLUDED_MAP.put(entity, this);
        else
            game.addCombatEntity(this);

        COMBAT_ENTITY_MAP.put(entity, this);

        addTask(new IntervalTask(i -> {
            onTicks.forEach(onTick -> {
                if (!isDisposed())
                    onTick.accept(i);
            });

            updateHitboxTick();
        }, 1));
    }

    /**
     * 지정한 엔티티의 전투 시스템 엔티티 인스턴스를 반환한다.
     *
     * @param entity 대상 엔티티
     * @return 전투 시스템의 엔티티 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    static CombatEntity fromEntity(@NonNull Entity entity) {
        return COMBAT_ENTITY_MAP.get(entity);
    }

    /**
     * 게임에 소속되지 않은 모든 엔티티를 반환한다.
     *
     * @return 게임에 소속되지 않은 모든 엔티티
     */
    @NonNull
    @UnmodifiableView
    static Collection<@NonNull CombatEntity> getAllExcluded() {
        return Collections.unmodifiableCollection(COMBAT_ENTITY_EXCLUDED_MAP.values());
    }

    @Override
    public final void addTask(@NonNull Task task) {
        taskManager.add(task);
    }

    @Override
    public final void addOnTick(@NonNull LongConsumer onTick) {
        onTicks.add(onTick);
    }

    @Override
    public final void addOnDispose(@NonNull Runnable onDispose) {
        onDisposes.add(onDispose);
    }

    /**
     * 엔티티의 히트박스를 업데이트한다.
     */
    private void updateHitboxTick() {
        Location oldLoc = entity.getLocation();

        new DelayTask(() -> {
            hitboxBaseLocation = oldLoc;
            for (Hitbox hitbox : hitboxes)
                hitbox.setBaseLocation(hitboxBaseLocation);
        }, 3);
    }

    @Override
    public double getWidth() {
        return entity.getWidth();
    }

    @Override
    public double getHeight() {
        return entity.getHeight();
    }

    @Override
    @NonNull
    public final Location getHitboxCenter() {
        return hitboxBaseLocation.clone().add(0, getHeight() / 2, 0);
    }

    @Override
    public final void setHitboxes(@NonNull Hitbox @NonNull ... hitboxes) {
        this.hitboxes = hitboxes;
    }

    @Override
    public final boolean isInHitbox(@NonNull Location location, double radius) {
        for (Hitbox hitbox : hitboxes)
            if (hitbox.isInHitbox(location, radius))
                return true;

        return false;
    }

    @Override
    @NonNull
    public final Location getLocation() {
        return entity.getLocation();
    }

    @Override
    @NonNull
    public final Location getCenterLocation() {
        return getLocation().add(0, getHeight() / 2, 0);
    }

    @Override
    public final void dispose() {
        if (isDisposed())
            throw new IllegalStateException("인스턴스가 이미 폐기됨");

        onDisposes.forEach(Runnable::run);
        taskManager.dispose();

        if (game == null)
            COMBAT_ENTITY_EXCLUDED_MAP.remove(entity);
        else
            game.removeCombatEntity(this);

        COMBAT_ENTITY_MAP.remove(entity);
    }

    @Override
    public final boolean isDisposed() {
        return COMBAT_ENTITY_MAP.get(entity) == null;
    }
}
