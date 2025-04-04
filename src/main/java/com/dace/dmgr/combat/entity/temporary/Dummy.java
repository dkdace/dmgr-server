package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.HealModule;
import com.dace.dmgr.combat.entity.module.MoveModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.Game;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 더미(훈련용 봇) 엔티티 클래스.
 */
@Getter
public final class Dummy extends TemporaryPlayerEntity implements Healable, Movable, HasCritHitbox, CombatEntity {
    /** 피해 모듈 */
    @NonNull
    private final HealModule damageModule;
    /** 상태 효과 모듈 */
    @NonNull
    private final StatusEffectModule statusEffectModule;
    /** 이동 모듈 */
    @NonNull
    private final MoveModule moveModule;
    /** 적 여부 */
    private final boolean isEnemy;

    /**
     * 더미 인스턴스를 생성한다.
     *
     * @param spawnLocation 생성 위치
     * @param maxHealth     최대 체력
     * @param isEnemy       적 여부. {@code true}로 지정 시 적 더미, {@code false}로 지정 시 아군 더미
     */
    public Dummy(@NonNull Location spawnLocation, int maxHealth, boolean isEnemy) {
        super(PlayerSkin.fromPlayerName("Clemounours"), spawnLocation, "훈련용 봇", null,
                Hitbox.builder(0.5, 0.7, 0.3).axisOffsetY(0.35).pitchFixed().build(),
                Hitbox.builder(0.8, 0.7, 0.45).axisOffsetY(1.05).pitchFixed().build(),
                Hitbox.builder(0.45, 0.35, 0.45).offsetY(0.225).axisOffsetY(1.4).build(),
                Hitbox.builder(0.45, 0.1, 0.45).offsetY(0.4).axisOffsetY(1.4).build());

        this.damageModule = new HealModule(this, maxHealth, true);
        this.statusEffectModule = new StatusEffectModule(this);
        this.moveModule = new MoveModule(this, 0);
        this.isEnemy = isEnemy;

        onInit();
    }

    /**
     * 적 더미 인스턴스를 생성한다.
     *
     * @param spawnLocation 생성 위치
     * @param maxHealth     최대 체력
     */
    public Dummy(@NonNull Location spawnLocation, int maxHealth) {
        this(spawnLocation, maxHealth, true);
    }

    private void onInit() {
        entity.setSilent(true);
        entity.setAI(true);
    }

    @Override
    @Nullable
    public Game.Team getTeam() {
        return null;
    }

    @Override
    public boolean isCreature() {
        return true;
    }

    @Override
    public boolean isEnemy(@NonNull CombatEntity target) {
        if (target instanceof CombatUser || target instanceof SummonEntity)
            return isEnemy;

        return !(target instanceof Dummy);
    }

    @Override
    @Nullable
    public Hitbox getCritHitbox() {
        return hitboxes[3];
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
        CombatEffectUtil.playBreakParticle(this, location, damage);
    }

    @Override
    public void onTakeHeal(@Nullable Healer provider, double amount) {
        // 미사용
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        remove();
    }
}
