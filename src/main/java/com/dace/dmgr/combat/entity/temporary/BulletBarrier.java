package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.ArmorStandSpawnHandler;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Hitbox;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

/**
 * 피탄되는 총알을 깅제로 제거하는 총알 방벽 클래스.
 */
public abstract class BulletBarrier extends SummonEntity<ArmorStand> {
    /**
     * 총알 방벽 인스턴스를 생성한다.
     *
     * @param spawnLocation 생성 위치
     * @param name          이름
     * @param owner         엔티티를 소환한 플레이어
     * @param hitbox        히트박스
     */
    protected BulletBarrier(@NonNull Location spawnLocation, @NonNull String name, @NonNull CombatUser owner, @NonNull Hitbox hitbox) {
        super(ArmorStandSpawnHandler.getInstance(), spawnLocation, name, owner, false, hitbox);
    }

    /**
     * 총알을 제거했을 때 실행할 작업.
     *
     * @param bullet   제거한 총알
     * @param location 맞은 위치
     */
    public abstract void onRemove(@NonNull Bullet<?> bullet, @NonNull Location location);
}
