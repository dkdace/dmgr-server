package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * 실탄을 발사하는 화기의 총알을 관리하는 클래스.
 */
public abstract class GunHitscan extends Hitscan {
    protected GunHitscan(@NonNull CombatEntity shooter, @NonNull HitscanOption option) {
        super(shooter, option);
    }

    protected GunHitscan(@NonNull CombatEntity shooter) {
        super(shooter);
    }

    @Override
    protected boolean onHitBlock(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
        playHitBlockEffect(location, hitBlock);
        return false;
    }

    /**
     * 총알이 맞았을 때 효과를 재생한다.
     *
     * @param location 맞은 위치
     * @param hitBlock 맞은 블록
     */
    private void playHitBlockEffect(@NonNull Location location, @NonNull Block hitBlock) {
        SoundUtil.play("random.gun.ricochet", location, 0.8, 0.975, 0.05);

        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                3, 0, 0, 0, 0.1);
        ParticleUtil.play(Particle.TOWN_AURA, location, 10, 0, 0, 0, 0);
        ParticleUtil.playBlockHitEffect(location, hitBlock);
    }
}
