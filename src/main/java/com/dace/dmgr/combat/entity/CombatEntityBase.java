package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;

/**
 * 모든 전투 시스템 엔티티의 기반 클래스.
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
@Getter
public abstract class CombatEntityBase<T extends LivingEntity> implements CombatEntity {
    /** 엔티티 객체 */
    protected final T entity;
    /** 능력치 목록 관리 객체 */
    protected final AbilityStatusManager abilityStatusManager = new AbilityStatusManager();
    /** 속성 목록 관리 객체 */
    protected final PropertyManager propertyManager = new PropertyManager();
    /** 히트박스 객체 목록 */
    protected final Hitbox[] hitboxes;
    /** 이름 */
    protected final String name;
    @Setter
    protected String team = "";
    /** 히트박스의 가능한 최대 크기 */
    private double maxHitboxSize = 0;

    /**
     * 전투 시스템의 엔티티 인스턴스를 생성한다.
     *
     * <p>{@link CombatEntityBase#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param entity 대상 엔티티
     * @param name   이름
     * @param hitbox 히트박스 목록
     */
    protected CombatEntityBase(T entity, String name, Hitbox... hitbox) {
        this.entity = entity;
        this.name = name;
        this.hitboxes = hitbox;
    }

    /**
     * 엔티티를 초기화하고 틱 스케쥴러를 실행한다.
     */
    public final void init() {
        abilityStatusManager.getAbilityStatus(Ability.DAMAGE).setBaseValue(1);
        abilityStatusManager.getAbilityStatus(Ability.DEFENSE).setBaseValue(1);
        entity.setCustomName(name);

        maxHitboxSize = 0;
        Arrays.stream(hitboxes).forEach(hitbox -> {
            double hitboxMaxSize = Math.max(hitbox.getSizeX(), Math.max(hitbox.getSizeY(), hitbox.getSizeZ()));
            maxHitboxSize = Math.max(maxHitboxSize, hitboxMaxSize + Math.max(hitbox.getOffsetX() + hitbox.getAxisOffsetX(),
                    Math.max(hitbox.getOffsetY() + hitbox.getAxisOffsetY(), hitbox.getOffsetZ() + hitbox.getAxisOffsetZ())));
        });
        onInit();

        TaskManager.addTask(this, new TaskTimer(1) {
            @Override
            public boolean onTimerTick(int i) {
                tick(i);
                onTick(i);
                updateHitboxTick();

                return true;
            }
        });
    }

    /**
     * 엔티티의 히트박스를 업데이트한다.
     */
    private void updateHitboxTick() {
        Location oldLoc = entity.getLocation();

        TaskManager.addTask(this, new TaskWait(3) {
            @Override
            public void onEnd() {
                for (Hitbox hitbox : hitboxes) {
                    hitbox.setCenter(oldLoc);
                }
            }
        });
    }

    @Override
    public final void remove() {
    public void remove() {
        TaskManager.clearTask(this);
        onRemove();
    }

    @Override
    public final boolean isEnemy(CombatEntity combatEntity) {
        return !getTeam().equals(combatEntity.getTeam());
    }

    @Override
    public boolean canPass(Location location) {
        return LocationUtil.canPass(location, getEntity().getLocation().add(0, 0.1, 0)) ||
                LocationUtil.canPass(location, getEntity().getEyeLocation());
    }

    @Override
    public boolean canPass(CombatEntity combatEntity) {
        return LocationUtil.canPass(combatEntity.getEntity().getLocation().add(0, 0.1, 0),
                getEntity().getLocation().add(0, 0.1, 0));
    }

    @Override
    public final void applyStatusEffect(StatusEffect statusEffect, long duration) {
        if (!hasStatusEffect(statusEffect.getStatusEffectType())) {
            CooldownManager.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffect.getStatusEffectType(), duration);

            statusEffect.onStart(this);

            TaskManager.addTask(this, new TaskTimer(1) {
                @Override
                public boolean onTimerTick(int i) {
                    if (!hasStatusEffect(statusEffect.getStatusEffectType()))
                        return false;

                    statusEffect.onTick(CombatEntityBase.this, i);

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    statusEffect.onEnd(CombatEntityBase.this);
                }
            });
        } else if (getStatusEffectDuration(statusEffect.getStatusEffectType()) < duration)
            CooldownManager.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffect.getStatusEffectType(), duration);
    }

    @Override
    public final long getStatusEffectDuration(StatusEffectType statusEffectType) {
        return CooldownManager.getCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType);
    }

    @Override
    public final boolean hasStatusEffect(StatusEffectType statusEffectType) {
        return getStatusEffectDuration(statusEffectType) > 0;
    }

    @Override
    public final void removeStatusEffect(StatusEffectType statusEffectType) {
        CooldownManager.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType);
    }
}
