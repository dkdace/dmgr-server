package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.ArmorStandSpawnHandler;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.GameUser;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 플레이어가 소환할 수 있는 방벽 클래스.
 *
 * <p>방벽으로 받은 피해는 게임 진행 시 막은 피해({@link GameUser#getDefend()})로 취급한다.</p>
 */
@Getter
public abstract class Barrier extends SummonEntity<ArmorStand> implements Damageable {
    /** 피해 모듈 */
    @NonNull
    protected final DamageModule damageModule;
    /** 상태 효과 모듈 */
    @NonNull
    protected final StatusEffectModule statusEffectModule;
    /** 죽었을 때 공격자에게 주는 점수 */
    private final int score;

    /**
     * 방벽 인스턴스를 생성한다.
     *
     * @param spawnLocation 생성 위치
     * @param name          이름
     * @param owner         엔티티를 소환한 플레이어
     * @param maxHealth     최대 체력
     * @param score         죽었을 때 공격자에게 주는 점수
     * @param hitbox        히트박스
     */
    protected Barrier(@NonNull Location spawnLocation, @NonNull String name, @NonNull CombatUser owner, int maxHealth, int score, @NonNull Hitbox hitbox) {
        super(ArmorStandSpawnHandler.getInstance(), spawnLocation, name, owner, false, hitbox);

        this.damageModule = new DamageModule(this, maxHealth, false);
        this.statusEffectModule = new StatusEffectModule(this);
        this.score = score;
    }

    @Override
    public final boolean isCreature() {
        return false;
    }

    @Override
    @MustBeInvokedByOverriders
    public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
        if (owner.getGameUser() != null)
            owner.getGameUser().addDefend(damage);
    }
}
