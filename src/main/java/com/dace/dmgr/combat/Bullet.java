package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;

/**
 * 총알. 원거리 공격(투사체, 히트스캔) 등을 관리하기 위한 클래스.
 *
 * @see Hitscan
 * @see Projectile
 */
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class Bullet {
    /** 총알의 최소 사거리 */
    protected static final float MIN_DISTANCE = 0.5F;
    /** 궤적 상 히트박스 판정점 간 거리 기본값. 단위: 블록 */
    protected static final float HITBOX_INTERVAL = 0.25F;
    /** 총알을 발사하는 엔티티 */
    protected final CombatEntity<?> shooter;
    /** 트레일 파티클을 남기는 주기. 단위: 판정점 개수 */
    protected int trailInterval;
    /** 총알의 최대 사거리 */
    protected int maxDistance;
    /** 관통 여부 */
    protected boolean penetrating;
    /** 판정 반경의 배수 (판정의 엄격함에 영향을 미침) */
    protected float hitboxMultiplier;

    /**
     * 총알이 맞았을 때의 파티클, 소리 효과를 재생한다.
     *
     * @param location 총알이 피격된 위치
     * @param hitBlock 총알이 피격된 블록
     * @param sound    피격음 재생 여부
     */
    public static void bulletHitEffect(Location location, Block hitBlock, boolean sound) {
        if (sound)
            SoundUtil.play("random.gun.ricochet", location, 0.8F, (float) (0.975 + Math.random() * 0.05));

        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                3, 0, 0, 0, 0.1F);
        ParticleUtil.play(Particle.TOWN_AURA, location, 10, 0, 0, 0, 0);
    }

    /**
     * 총알을 발사한다.
     *
     * @param origin    발화점
     * @param direction 발사 방향
     * @param spread    탄퍼짐 정도
     */
    public abstract void shoot(Location origin, Vector direction, float spread);

    /**
     * 엔티티가 보는 방향으로 총알을 발사한다.
     *
     * @param origin 발화점
     * @param spread 탄퍼짐 정도. 단위: ×0.02블록/블록
     */
    public final void shoot(Location origin, float spread) {
        shoot(origin, shooter.getEntity().getLocation().getDirection(), spread);
    }

    /**
     * 엔티티가 보는 방향으로 탄퍼짐 없이 총알을 발사한다.
     *
     * @param origin 발화점
     */
    public final void shoot(Location origin) {
        shoot(origin, 0);
    }

    /**
     * 엔티티가 보는 방향으로, 엔티티의 눈 위치에서 총알을 발사한다.
     *
     * @param spread 탄퍼짐 정도. 단위: ×0.02블록/블록
     */
    public final void shoot(float spread) {
        if (shooter instanceof CombatUser)
            shoot(((CombatUser) shooter).getEntity().getEyeLocation(), spread);
        else
            shoot(shooter.getEntity().getLocation(), spread);
    }

    /**
     * 엔티티가 보는 방향으로, 엔티티의 눈 위치에서 탄퍼짐 없이 총알을 발사한다.
     */
    public final void shoot() {
        if (shooter instanceof CombatUser)
            shoot(((CombatUser) shooter).getEntity().getEyeLocation());
        else
            shoot(shooter.getEntity().getLocation());
    }

    /**
     * 총알의 블록 충돌 로직을 처리한다.
     *
     * @param location  위치
     * @param direction 발사 방향
     */
    protected final void handleBlockCollision(Location location, Vector direction) {
        Location loc = location.clone();
        Vector subDir = direction.clone().multiply(0.25);
        Block hitBlock = location.getBlock();

        while (!LocationUtil.isNonSolid(loc))
            loc.subtract(subDir);

        loc.subtract(subDir);
        onHit(loc);
        onHitBlock(loc, hitBlock);
    }

    /**
     * 총알 주변의 적을 찾고 피격 로직을 처리한다.
     *
     * @param location 위치
     * @param targets  피격자 목록
     * @param size     기본 판정 범위
     * @return 적 피격 시 {@code penetrating}이 {@code false}이면 {@code true} 반환
     */
    protected final boolean findEnemyAndHandleCollision(Location location, Set<CombatEntity<?>> targets, float size) {
        Map.Entry<CombatEntity<?>, Boolean> targetEntry
                = CombatUtil.getNearEnemy(shooter, location, size * hitboxMultiplier);
        CombatEntity<?> target = targetEntry.getKey();
        boolean isCrit = targetEntry.getValue();

        if (target != null && targets.add(target)) {
            onHit(location);
            onHitEntity(location, target, isCrit);

            return !penetrating;
        }

        return false;
    }

    /**
     * {@link Bullet#trailInterval} 주기마다 실행될 작업
     *
     * <p>주로 파티클을 남길 때 사용한다.</p>
     *
     * @param location 위치
     */
    public abstract void trail(Location location);

    /**
     * 총알이 블록에 맞았을 때 실행될 작업
     *
     * @param location 맞은 위치
     * @param hitBlock 맞은 블록
     * @see Bullet#onHit
     * @see Bullet#onHitEntity
     */
    public void onHitBlock(Location location, Block hitBlock) {
        Bullet.bulletHitEffect(location, hitBlock, true);
    }

    /**
     * 총알이 엔티티에 맞았을 때 실행될 작업
     *
     * @param location 맞은 위치
     * @param target   맞은 엔티티
     * @param isCrit   치명타 여부
     * @see Bullet#onHit
     * @see Bullet#onHitBlock
     */
    public abstract void onHitEntity(Location location, CombatEntity<?> target, boolean isCrit);

    /**
     * 총알이 어느 곳이든(블록, 엔티티) 맞았을 때 실행될 작업
     *
     * @param location 맞은 위치
     * @see Bullet#onHitBlock
     * @see Bullet#onHitEntity
     */
    public void onHit(Location location) {
    }
}
