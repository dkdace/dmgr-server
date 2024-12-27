package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * 전투 시스템에 사용되는 각종 효과를 제공하는 클래스.
 */
@UtilityClass
public final class CombatEffectUtil {
    /** 총기 탄피 효과음 */
    public static final SoundEffect SHELL_DROP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_MAGMACUBE_JUMP).volume(0.8).pitch(1).pitchVariance(0.1).build());
    /** 총기 탄피 효과음 (중량) */
    public static final SoundEffect SHELL_DROP_HEAVY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_MAGMACUBE_JUMP).volume(0.8).pitch(0.95).pitchVariance(0.1).build());
    /** 산탄총 탄피 효과음 */
    public static final SoundEffect SHOTGUN_SHELL_DROP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_HORSE_DEATH).volume(1).pitch(1).pitchVariance(0.1).build());
    /** 엔티티 소환 효과음 */
    public static final SoundEffect ENTITY_SUMMON_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.8).pitch(1).build());
    /** 투척 효과음 */
    public static final SoundEffect THROW_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_WITCH_THROW).volume(0.8).pitch(0.8).build());
    /** 투척 효과음 (중량) */
    public static final SoundEffect THROW_HEAVY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_WITCH_THROW).volume(0.8).pitch(0.7).build());
    /** 투척물 튕김 효과음 */
    public static final SoundEffect THROW_BOUNCE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("random.metalhit").volume(0.1).pitch(1.2).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder(Sound.BLOCK_GLASS_BREAK).volume(0.1).pitch(2).build()
    );
    /** 총알 궤적 효과 */
    public static final ParticleEffect BULLET_TRAIL_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).build());

    /** 블록 타격 효과음 (잔디) */
    private static final SoundEffect HIT_BLOCK_GRASS_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.BLOCK_GRASS_BREAK).volume(0.8).pitch(0.7).pitchVariance(0.1).build());
    /** 블록 타격 효과음 (흙) */
    private static final SoundEffect HIT_BLOCK_DIRT_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.BLOCK_GRAVEL_BREAK).volume(0.8).pitch(0.7).pitchVariance(0.1).build());
    /** 블록 타격 효과음 (돌) */
    private static final SoundEffect HIT_BLOCK_STONE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_BREAK).volume(1).pitch(0.9).pitchVariance(0.1).build());
    /** 블록 타격 효과음 (금속) */
    private static final SoundEffect HIT_BLOCK_METAL_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(0.5).pitch(1.95).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder("random.metalhit").volume(0.8).pitch(1.95).pitchVariance(0.1).build()
    );
    /** 블록 타격 효과음 (목재) */
    private static final SoundEffect HIT_BLOCK_WOOD_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.BLOCK_WOOD_BREAK).volume(0.8).pitch(0.8).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder("random.stab").volume(0.8).pitch(1.95).pitchVariance(0.1).build()
    );
    /** 블록 타격 효과음 (유리) */
    private static final SoundEffect HIT_BLOCK_GLASS_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.BLOCK_GLASS_BREAK).volume(0.8).pitch(0.7).pitchVariance(0.1).build());
    /** 블록 타격 효과음 (양털) */
    private static final SoundEffect HIT_BLOCK_WOOL_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.BLOCK_CLOTH_BREAK).volume(1).pitch(0.8).pitchVariance(0.1).build());
    /** 출혈 효과 */
    private static final ParticleEffect BLEEDING_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.REDSTONE_BLOCK, 0)
                    .count(0, 0, 1)
                    .horizontalSpread(1, 0, 0.25)
                    .verticalSpread(2, 0, 0.25)
                    .speed(3, 0.03, 0.1)
                    .build());
    /** 파괴 효과 */
    private static final ParticleEffect BREAK_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.IRON_BLOCK, 0)
                    .count(0, 0, 1)
                    .horizontalSpread(1, 0, 0.25)
                    .verticalSpread(2, 0, 0.25)
                    .speed(3, 0.03, 0.1)
                    .build());
    /** 블록 타격 효과 */
    private static final ParticleEffect HIT_BLOCK_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(0, ParticleEffect.BlockParticleType.BLOCK_DUST)
                    .count(1, 0, 6)
                    .horizontalSpread(1, 0, 0.06)
                    .verticalSpread(1, 0, 0.06)
                    .speed(0.1).build(),
            ParticleEffect.NormalParticleInfo.builder(Particle.TOWN_AURA)
                    .count(1, 0, 25)
                    .horizontalSpread(1, 0, 0.05)
                    .verticalSpread(1, 0, 0.05)
                    .build()
    );
    /** 블록 타격 효과 (소형) */
    private static final ParticleEffect HIT_BLOCK_SMALL_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(0, ParticleEffect.BlockParticleType.BLOCK_DUST)
                    .count(1, 0, 3)
                    .speed(0.1).build(),
            ParticleEffect.NormalParticleInfo.builder(Particle.TOWN_AURA)
                    .count(1, 0, 10)
                    .build()
    );

    /**
     * 지정한 위치 또는 엔티티에 출혈 입자 효과를 재생한다.
     *
     * @param location     대상 위치
     * @param combatEntity 대상 엔티티
     * @param damage       피해량
     */
    public static void playBleedingEffect(@Nullable Location location, @Nullable CombatEntity combatEntity, double damage) {
        if (location == null && combatEntity == null)
            return;

        if (location == null)
            BLEEDING_PARTICLE.play(combatEntity.getCenterLocation(), damage == 0 ? 1 : damage * 0.1, combatEntity.getEntity().getWidth(),
                    combatEntity.getEntity().getHeight(), damage == 0 ? 0 : 1);
        else
            BLEEDING_PARTICLE.play(location, damage * 0.06, 0, 0, 1);
    }

    /**
     * 지정한 위치 또는 엔티티에 파괴 입자 효과를 재생한다.
     *
     * @param location     대상 위치
     * @param combatEntity 대상 엔티티
     * @param damage       피해량
     */
    public static void playBreakEffect(@Nullable Location location, @Nullable CombatEntity combatEntity, double damage) {
        if (location == null && combatEntity == null)
            return;

        if (location == null)
            BREAK_PARTICLE.play(combatEntity.getCenterLocation(), damage == 0 ? 1 : damage * 0.07, combatEntity.getEntity().getWidth(),
                    combatEntity.getEntity().getHeight(), damage == 0 ? 0 : 1);
        else
            BREAK_PARTICLE.play(location, damage * 0.04, 0, 0, 1);
    }

    /**
     * 지정한 위치에 블록 타격 입자 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param block    블록
     * @param scale    입자 규모(입자의 양, 범위). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playHitBlockParticle(@NonNull Location location, @NonNull Block block, double scale) {
        HIT_BLOCK_PARTICLE.play(location, block.getState().getData(), scale);
    }

    /**
     * 지정한 위치에 소형 블록 타격 입자 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param block    블록
     * @param scale    입자 규모(입자의 양). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playSmallHitBlockParticle(@NonNull Location location, @NonNull Block block, double scale) {
        HIT_BLOCK_SMALL_PARTICLE.play(location, block.getState().getData(), scale);
    }

    /**
     * 지정한 위치에 블록 타격 효과음을 재생한다.
     *
     * @param location    대상 위치
     * @param block       블록
     * @param volumeScale 음량 규모. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playHitBlockSound(@NonNull Location location, @NonNull Block block, double volumeScale) {
        switch (block.getType()) {
            case GRASS:
            case LEAVES:
            case LEAVES_2:
            case SPONGE:
            case HAY_BLOCK:
            case GRASS_PATH:
                HIT_BLOCK_GRASS_SOUND.play(location, volumeScale);
                break;
            case DIRT:
            case GRAVEL:
            case SAND:
            case CLAY:
                HIT_BLOCK_DIRT_SOUND.play(location, volumeScale);
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
                HIT_BLOCK_STONE_SOUND.play(location, volumeScale);
                break;
            case IRON_BLOCK:
            case GOLD_BLOCK:
            case IRON_DOOR_BLOCK:
            case ANVIL:
            case IRON_TRAPDOOR:
            case CAULDRON:
            case HOPPER:
                HIT_BLOCK_METAL_SOUND.play(location, volumeScale);
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
                HIT_BLOCK_WOOD_SOUND.play(location, volumeScale);
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
                HIT_BLOCK_GLASS_SOUND.play(location, volumeScale);
                break;
            case WOOL:
            case CARPET:
                HIT_BLOCK_WOOL_SOUND.play(location, volumeScale);
                break;
            default:
                break;
        }
    }
}
