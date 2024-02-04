package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
    public boolean onHitBlock(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
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

        switch (getBlockTexture(hitBlock.getType())) {
            case GRASS:
                SoundUtil.play(Sound.BLOCK_GRASS_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case DIRT:
                SoundUtil.play(Sound.BLOCK_GRAVEL_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case STONE:
                SoundUtil.play(Sound.BLOCK_STONE_BREAK, location, 1, 0.9, 0.1);
                break;
            case METAL:
                SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, location, 0.5, 1.95, 0.1);
                SoundUtil.play("random.metalhit", location, 0.8, 1.95, 0.1);
                break;
            case WOOD:
                SoundUtil.play(Sound.BLOCK_WOOD_BREAK, location, 0.8, 0.8, 0.1);
                SoundUtil.play("random.stab", location, 0.8, 1.95, 0.1);
                break;
            case GLASS:
                SoundUtil.play(Sound.BLOCK_GLASS_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case WOOL:
                SoundUtil.play(Sound.BLOCK_CLOTH_BREAK, location, 1, 0.8, 0.1);
                break;
        }
    }

    /**
     * 블록의 재질을 반환한다.
     *
     * @param material 아이템 타입
     */
    private Texture getBlockTexture(@NonNull Material material) {
        switch (material) {
            case GRASS:
            case LEAVES:
            case LEAVES_2:
            case SPONGE:
            case HAY_BLOCK:
                return Texture.GRASS;
            case DIRT:
            case GRAVEL:
            case SAND:
            case CLAY:
                return Texture.DIRT;
            case STONE:
            case COBBLESTONE:
            case COBBLESTONE_STAIRS:
            case BRICK:
            case BRICK_STAIRS:
            case SANDSTONE:
            case SANDSTONE_STAIRS:
            case RED_SANDSTONE:
            case RED_SANDSTONE_STAIRS:
            case HARD_CLAY:
            case STAINED_CLAY:
            case OBSIDIAN:
            case COAL_BLOCK:
            case QUARTZ_BLOCK:
            case QUARTZ_STAIRS:
            case COBBLE_WALL:
            case SMOOTH_BRICK:
            case SMOOTH_STAIRS:
            case NETHER_BRICK:
            case NETHER_FENCE:
            case NETHER_BRICK_STAIRS:
            case STEP:
            case DOUBLE_STEP:
            case STONE_SLAB2:
            case DOUBLE_STONE_SLAB2:
            case CONCRETE:
                return Texture.STONE;
            case IRON_BLOCK:
            case GOLD_BLOCK:
            case IRON_DOOR_BLOCK:
            case ANVIL:
            case IRON_TRAPDOOR:
            case CAULDRON:
            case HOPPER:
                return Texture.METAL;
            case WOOD:
            case LOG:
            case LOG_2:
            case WOOD_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case DARK_OAK_STAIRS:
            case ACACIA_STAIRS:
            case WOOD_STEP:
            case WOOD_DOUBLE_STEP:
            case NOTE_BLOCK:
            case JUKEBOX:
            case FENCE:
            case SPRUCE_FENCE:
            case BIRCH_FENCE:
            case JUNGLE_FENCE:
            case DARK_OAK_FENCE:
            case ACACIA_FENCE:
            case FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case WOODEN_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case CHEST:
            case BOOKSHELF:
                return Texture.WOOD;
            case GLASS:
            case THIN_GLASS:
            case STAINED_GLASS:
            case STAINED_GLASS_PANE:
            case ICE:
            case PACKED_ICE:
            case FROSTED_ICE:
            case REDSTONE_LAMP_OFF:
            case REDSTONE_LAMP_ON:
            case SEA_LANTERN:
                return Texture.GLASS;
            case WOOL:
            case CARPET:
                return Texture.WOOL;
            default:
                return Texture.NONE;
        }
    }

    /**
     * 블록 재질의 종류.
     */
    private enum Texture {
        /** 미지정 */
        NONE,
        /** 풀 */
        GRASS,
        /** 흙 */
        DIRT,
        /** 석재 */
        STONE,
        /** 금속 */
        METAL,
        /** 목재 */
        WOOD,
        /** 유리 */
        GLASS,
        /** 양털 */
        WOOL
    }
}
