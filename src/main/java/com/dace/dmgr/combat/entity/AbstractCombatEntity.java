package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.text.MessageFormat;

/**
 * {@link CombatEntity}의 기본 구현체, 모든 전투 시스템 엔티티의 기반 클래스.
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 */
@Getter
abstract class AbstractCombatEntity<T extends Entity> implements CombatEntity {
    /** 엔티티 객체 */
    @NonNull
    protected final T entity;
    /** 속성 목록 관리 객체 */
    @NonNull
    protected final PropertyManager propertyManager = new PropertyManager();
    /** 이름 */
    @NonNull
    protected final String name;
    /** 소속된 게임. {@code null}이면 게임에 참여중이지 않음을 나타냄 */
    protected final Game game;
    /** 히트박스 객체 목록 */
    @NonNull
    protected final Hitbox[] hitboxes;
    /** 활성화 여부 */
    protected boolean isActivated = false;
    /** 히트박스의 가능한 최대 크기. (단위: 블록) */
    private double maxHitboxSize = 0;

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성한다.
     *
     * @param entity   대상 엔티티
     * @param name     이름
     * @param game     소속된 게임. {@code null}이면 게임에 참여중이지 않음을 나타냄
     * @param hitboxes 히트박스 목록
     * @throws IllegalStateException 해당 {@code entity}의 CombatEntity가 이미 존재하면 발생
     */
    protected AbstractCombatEntity(@NonNull T entity, @NonNull String name, Game game, @NonNull Hitbox... hitboxes) {
        CombatEntity combatEntity = CombatEntityRegistry.getInstance().get(entity);
        if (combatEntity != null)
            throw new IllegalStateException(MessageFormat.format("엔티티 {0}의 CombatEntity가 이미 생성됨", name));

        this.entity = entity;
        this.name = name;
        this.game = game;
        this.hitboxes = hitboxes;

        for (Hitbox hitbox : hitboxes) {
            double hitboxMaxSize = Math.max(hitbox.getSizeX(), Math.max(hitbox.getSizeY(), hitbox.getSizeZ()));
            maxHitboxSize = Math.max(maxHitboxSize, hitboxMaxSize + Math.max(hitbox.getOffsetX() + hitbox.getAxisOffsetX(),
                    Math.max(hitbox.getOffsetY() + hitbox.getAxisOffsetY(), hitbox.getOffsetZ() + hitbox.getAxisOffsetZ())));
        }
        entity.setCustomName(ChatColor.WHITE + name);
        if (game != null)
            game.addCombatEntity(this);

        CombatEntityRegistry.getInstance().add(entity, this);
    }

    @Override
    @MustBeInvokedByOverriders
    public void activate() {
        isActivated = true;

        TaskUtil.addTask(this, new IntervalTask(i -> {
            onTick(i);
            updateHitboxTick();

            return true;
        }, 1));
    }

    /**
     * 엔티티가 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    protected abstract void onTick(long i);

    /**
     * 엔티티의 히트박스를 업데이트한다.
     */
    private void updateHitboxTick() {
        Location oldLoc = entity.getLocation();

        TaskUtil.addTask(this, new DelayTask(() -> {
            for (Hitbox hitbox : hitboxes) {
                hitbox.setCenter(oldLoc);
            }
        }, 3));
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        checkAccess();

        CombatEntityRegistry.getInstance().remove(entity);
        if (game != null)
            game.removeCombatEntity(this);

        TaskUtil.clearTask(this);
    }

    @Override
    public boolean isDisposed() {
        return CombatEntityRegistry.getInstance().get(entity) == null;
    }

    @Override
    public boolean isEnemy(@NonNull CombatEntity combatEntity) {
        return !getTeamIdentifier().equals(combatEntity.getTeamIdentifier());
    }

    @Override
    public boolean canPass(@NonNull Location location) {
        if (entity.getWorld() != location.getWorld())
            return false;
        return LocationUtil.canPass(location, getEntity().getLocation().add(0, 0.1, 0)) ||
                LocationUtil.canPass(location, getEntity().getLocation().add(0, 1, 0));
    }

    @Override
    public boolean canPass(@NonNull CombatEntity combatEntity) {
        if (entity.getWorld() != combatEntity.getEntity().getWorld())
            return false;
        return LocationUtil.canPass(combatEntity.getEntity().getLocation().add(0, 0.1, 0),
                getEntity().getLocation().add(0, 0.1, 0));
    }

    @Override
    public final void applyStatusEffect(@NonNull StatusEffectType statusEffectType, long duration) {
        StatusEffect statusEffect = statusEffectType.getStatusEffect();

        if (!hasStatusEffect(statusEffectType)) {
            CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffect.getName(), duration);

            statusEffect.onStart(this);

            TaskUtil.addTask(this, new IntervalTask(i -> {
                if (!hasStatusEffect(statusEffectType))
                    return false;

                statusEffect.onTick(AbstractCombatEntity.this, i);

                return true;
            }, isCancelled -> statusEffect.onEnd(AbstractCombatEntity.this), 1));
        } else if (getStatusEffectDuration(statusEffectType) < duration)
            CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffect.getName(), duration);
    }

    @Override
    public final long getStatusEffectDuration(@NonNull StatusEffectType statusEffectType) {
        return CooldownUtil.getCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType.getStatusEffect().getName());
    }

    @Override
    public final boolean hasStatusEffect(@NonNull StatusEffectType statusEffectType) {
        return getStatusEffectDuration(statusEffectType) > 0;
    }

    @Override
    public final void removeStatusEffect(@NonNull StatusEffectType statusEffectType) {
        CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType.getStatusEffect().getName());
    }
}
