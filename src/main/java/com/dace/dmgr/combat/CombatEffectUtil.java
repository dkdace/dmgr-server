package com.dace.dmgr.combat;

import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 전투 시스템에 사용되는 효과 재생 기능을 제공하는 클래스.
 */
@UtilityClass
public final class CombatEffectUtil {
    /**
     * 지정한 위치 또는 엔티티에 출혈 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param entity   대상 엔티티
     * @param damage   피해량
     */
    public static void playBleedingEffect(@Nullable Location location, @Nullable LivingEntity entity, int damage) {
        if (location == null && entity == null)
            return;

        if (location == null)
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.REDSTONE_BLOCK, 0,
                    entity.getLocation().add(0, entity.getHeight() / 2, 0), damage == 0 ? 1 : (int) Math.ceil(damage * 0.1),
                    entity.getWidth() / 4, entity.getHeight() / 4, entity.getWidth() / 4, damage == 0 ? 0.03 : 0.1);
        else
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.REDSTONE_BLOCK, 0, location, (int) Math.ceil(damage * 0.06),
                    0, 0, 0, 0.1);
    }

    /**
     * 지정한 위치 또는 엔티티에 파괴 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param entity   대상 엔티티
     * @param damage   피해량
     */
    public static void playBreakEffect(@Nullable Location location, @Nullable LivingEntity entity, int damage) {
        if (location == null && entity == null)
            return;

        if (location == null)
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0,
                    entity.getLocation().add(0, entity.getHeight() / 2, 0), damage == 0 ? 1 : (int) Math.ceil(damage * 0.07),
                    entity.getWidth() / 4, entity.getHeight() / 4, entity.getWidth() / 4, damage == 0 ? 0.03 : 0.1);
        else
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, location, (int) Math.ceil(damage * 0.04),
                    0, 0, 0, 0.1);
    }

    /**
     * 지정한 위치에 블록 타격 효과를 재생한다.
     *
     * @param location        대상 위치
     * @param block           블록
     * @param scaleMultiplier 규모(입자의 양 및 범위) 배수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playBlockHitEffect(@NonNull Location location, @NonNull Block block, double scaleMultiplier) {
        if (scaleMultiplier < 0)
            throw new IllegalArgumentException("'scaleMultiplier'가 0 이상이어야 함");

        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, block.getType(), block.getData(), location,
                (int) (6 * scaleMultiplier), 0.06 * scaleMultiplier, 0.06 * scaleMultiplier, 0.06 * scaleMultiplier, 0.1);
        ParticleUtil.play(Particle.TOWN_AURA, location, (int) (25 * scaleMultiplier),
                0.05 * scaleMultiplier, 0.05 * scaleMultiplier, 0.05 * scaleMultiplier, 0);
    }

    /**
     * 지정한 위치에 블록 타격 효과음을 재생한다.
     *
     * @param location         대상 위치
     * @param block            블록
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playBlockHitSound(@NonNull Location location, @NonNull Block block, double volumeMultiplier) {
        if (volumeMultiplier < 0)
            throw new IllegalArgumentException("'volumeMultiplier'가 0 이상이어야 함");

        switch (block.getType()) {
            case GRASS:
            case LEAVES:
            case LEAVES_2:
            case SPONGE:
            case HAY_BLOCK:
            case GRASS_PATH:
                SoundUtil.play(Sound.BLOCK_GRASS_BREAK, location, 0.8 * volumeMultiplier, 0.7, 0.1);
                break;
            case DIRT:
            case GRAVEL:
            case SAND:
            case CLAY:
                SoundUtil.play(Sound.BLOCK_GRAVEL_BREAK, location, 0.8 * volumeMultiplier, 0.7, 0.1);
                break;
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
                SoundUtil.play(Sound.BLOCK_STONE_BREAK, location, 1 * volumeMultiplier, 0.9, 0.1);
                break;
            case IRON_BLOCK:
            case GOLD_BLOCK:
            case IRON_DOOR_BLOCK:
            case ANVIL:
            case IRON_TRAPDOOR:
            case CAULDRON:
            case HOPPER:
                SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, location, 0.5 * volumeMultiplier, 1.95, 0.1);
                SoundUtil.play("random.metalhit", location, 0.8 * volumeMultiplier, 1.95, 0.1);
                break;
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
                SoundUtil.play(Sound.BLOCK_WOOD_BREAK, location, 0.8 * volumeMultiplier, 0.8, 0.1);
                SoundUtil.play("random.stab", location, 0.8 * volumeMultiplier, 1.95, 0.1);
                break;
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
                SoundUtil.play(Sound.BLOCK_GLASS_BREAK, location, 0.8 * volumeMultiplier, 0.7, 0.1);
                break;
            case WOOL:
            case CARPET:
                SoundUtil.play(Sound.BLOCK_CLOTH_BREAK, location, 1 * volumeMultiplier, 0.8, 0.1);
                break;
            default:
                break;
        }
    }
}
