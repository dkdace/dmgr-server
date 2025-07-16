package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.Task;
import com.dace.dmgr.util.task.TaskManager;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.function.LongConsumer;

/**
 * {@link CombatEntity}의 기본 구현체, 모든 전투 시스템 엔티티의 기반 클래스.
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 */
public abstract class AbstractCombatEntity<T extends Entity> implements CombatEntity {
    /** 엔티티 인스턴스 */
    @NonNull
    @Getter
    protected final T entity;
    /** 이름 */
    @NonNull
    @Getter
    protected final String name;
    /** 월드 */
    @Getter
    private final World world;
    /** 태스크 관리 인스턴스 */
    private final TaskManager taskManager = new TaskManager();
    /** 매 틱마다 실행할 작업 목록 */
    private final ArrayList<LongConsumer> onTicks = new ArrayList<>();
    /** 제거 시 실행할 작업 목록 */
    private final ArrayList<Runnable> onRemoves = new ArrayList<>();
    /** 히트박스 기준 위치 */
    private Location hitboxBaseLocation;

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성한다.
     *
     * @param entity 대상 엔티티
     * @param name   이름
     * @throws IllegalStateException 해당 {@code entity}의 CombatEntity가 이미 존재하면 발생
     */
    protected AbstractCombatEntity(@NonNull T entity, @NonNull String name) {
        Validate.validState(CombatEntityRegistry.get(entity) == null, "CombatEntity가 이미 존재함");

        this.entity = entity;
        this.name = name;
        this.world = entity.getWorld();
        this.hitboxBaseLocation = entity.getLocation();

        entity.setCustomName(ChatColor.WHITE + name);

        CombatEntityRegistry.onAdd(this);

        addTask(new IntervalTask(i -> {
            for (LongConsumer onTick : onTicks) {
                onTick.accept(i);

                if (isRemoved()) {
                    onTicks.clear();
                    break;
                }
            }

            updateHitboxTick();
        }, 1));
    }

    @Override
    public final void remove() {
        Validate.validState(!isRemoved(), "CombatEntity가 이미 제거됨");

        onRemoves.forEach(Runnable::run);
        onRemoves.clear();
        taskManager.stop();

        CombatEntityRegistry.onRemove(this);
    }

    @Override
    public final boolean isRemoved() {
        return CombatEntityRegistry.get(entity) == null;
    }

    @Override
    public final void addTask(@NonNull Task task) {
        if (!isRemoved())
            taskManager.add(task);
    }

    @Override
    public final void addOnTick(@NonNull LongConsumer onTick) {
        if (!isRemoved())
            onTicks.add(onTick);
    }

    @Override
    public final void addOnRemove(@NonNull Runnable onRemove) {
        if (!isRemoved())
            onRemoves.add(onRemove);
    }

    /**
     * 엔티티의 히트박스 목록을 반환한다.
     *
     * @return 히트박스 목록
     */
    @NonNull
    protected abstract Hitbox @NonNull [] getHitboxes();

    /**
     * 엔티티의 히트박스를 업데이트한다.
     */
    private void updateHitboxTick() {
        Location oldLoc = entity.getLocation();

        new DelayTask(() -> {
            hitboxBaseLocation = oldLoc;
            for (Hitbox hitbox : getHitboxes())
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
    public final boolean isInHitbox(@NonNull Location location, double radius) {
        for (Hitbox hitbox : getHitboxes())
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
}
