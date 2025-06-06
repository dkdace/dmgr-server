package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;
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
    public static final PlayableEffect.Function<Double> SHELL_DROP_SOUND = pitch ->
            SoundEffect.builder(Sound.ENTITY_MAGMACUBE_JUMP).volume(0.8).pitch(pitch).pitchVariance(0.1).build();
    /** 산탄총 탄피 효과음 */
    public static final PlayableEffect.Function<Double> SHOTGUN_SHELL_DROP_SOUND = pitch ->
            SoundEffect.builder(Sound.ENTITY_ZOMBIE_HORSE_DEATH).volume(1).pitch(pitch).pitchVariance(0.1).build();
    /** 총알 블록 타격 효과음 */
    public static final SoundEffect BULLET_HIT_BLOCK_SOUND =
            SoundEffect.builder("random.gun.ricochet").volume(0.8).pitch(0.975).pitchVariance(0.05).build();
    /** 엔티티 소환 효과음 */
    public static final SoundEffect ENTITY_SUMMON_SOUND =
            SoundEffect.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.8).pitch(1).build();
    /** 투척 효과음 */
    public static final PlayableEffect.Function<Double> THROW_SOUND = pitch ->
            SoundEffect.builder(Sound.ENTITY_WITCH_THROW).volume(0.8).pitch(pitch).build();
    /** 투척물 튕김 효과음 */
    public static final PlayableEffect.Function<Double> THROW_BOUNCE_SOUND = speed -> PlayableEffect.list(
            SoundEffect.builder("random.metalhit").volume(0.1 + speed * 2).pitch(1.2).pitchVariance(0.1).build(),
            SoundEffect.builder(Sound.BLOCK_GLASS_BREAK).volume(0.1 + speed * 2).pitch(2).build());
    /** 총알 궤적 효과 */
    public static final ParticleEffect BULLET_TRAIL_PARTICLE =
            ParticleEffect.Normal.builder(Particle.CRIT).build();
    /** 블록 타격 효과음 */
    public static final PlayableEffect.BiFunction<Block, Double> HIT_BLOCK_SOUND = (block, volumeMultiplier) -> {
        switch (block.getType()) {
            case GRASS:
            case LEAVES:
            case LEAVES_2:
            case SPONGE:
            case HAY_BLOCK:
            case GRASS_PATH:
                return SoundEffect.builder(Sound.BLOCK_GRASS_BREAK).volume(0.8 * volumeMultiplier).pitch(0.7).pitchVariance(0.1).build();
            case DIRT:
            case GRAVEL:
            case SAND:
            case CLAY:
                return SoundEffect.builder(Sound.BLOCK_GRAVEL_BREAK).volume(0.8 * volumeMultiplier).pitch(0.7).pitchVariance(0.1).build();
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
                return SoundEffect.builder(Sound.BLOCK_STONE_BREAK).volume(1 * volumeMultiplier).pitch(0.9).pitchVariance(0.1).build();
            case IRON_BLOCK:
            case GOLD_BLOCK:
            case IRON_DOOR_BLOCK:
            case ANVIL:
            case IRON_TRAPDOOR:
            case CAULDRON:
            case HOPPER:
                return PlayableEffect.list(
                        SoundEffect.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(0.5 * volumeMultiplier).pitch(1.95).pitchVariance(0.1).build(),
                        SoundEffect.builder("random.metalhit").volume(0.8 * volumeMultiplier).pitch(1.95).pitchVariance(0.1).build());
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
                return PlayableEffect.list(
                        SoundEffect.builder(Sound.BLOCK_WOOD_BREAK).volume(0.8 * volumeMultiplier).pitch(0.8).pitchVariance(0.1).build(),
                        SoundEffect.builder("random.stab").volume(0.8 * volumeMultiplier).pitch(1.95).pitchVariance(0.1).build());
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
                return SoundEffect.builder(Sound.BLOCK_GLASS_BREAK).volume(0.8 * volumeMultiplier).pitch(0.7).pitchVariance(0.1).build();
            case WOOL:
            case CARPET:
                return SoundEffect.builder(Sound.BLOCK_CLOTH_BREAK).volume(1 * volumeMultiplier).pitch(0.8).pitchVariance(0.1).build();
            default:
                return SoundEffect.NONE;
        }
    };
    /** 블록 타격 효과 */
    public static final PlayableEffect.BiFunction<Block, Double> HIT_BLOCK_PARTICLE = (block, scale) -> PlayableEffect.list(
            ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, block)
                    .count((int) (scale * 6))
                    .horizontalSpread(scale * 0.06)
                    .verticalSpread(scale * 0.06)
                    .speed(0.1).build(),
            ParticleEffect.Normal.builder(Particle.TOWN_AURA)
                    .count((int) (scale * 25))
                    .horizontalSpread(scale * 0.05)
                    .verticalSpread(scale * 0.05)
                    .build());
    /** 소형 블록 타격 효과 */
    public static final PlayableEffect.BiFunction<Block, Double> HIT_BLOCK_SMALL_PARTICLE = (block, scale) -> PlayableEffect.list(
            ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, block)
                    .count((int) (scale * 3))
                    .speed(0.1).build(),
            ParticleEffect.Normal.builder(Particle.TOWN_AURA)
                    .count((int) (scale * 10))
                    .build());
    /** 총알 타격 효과 */
    public static final PlayableEffect.Function<Block> BULLET_HIT_EFFECT = block -> PlayableEffect.list(
            BULLET_HIT_BLOCK_SOUND,
            HIT_BLOCK_SMALL_PARTICLE.apply(block, 1.0),
            HIT_BLOCK_SOUND.apply(block, 1.0));

    /**
     * 피해 입자 효과의 종류.
     */
    public enum DamageParticle {
        /** 출혈 */
        BLOOD(Material.REDSTONE_BLOCK, 0.06, 0.1),
        /** 금속 파편 */
        METAL(Material.IRON_BLOCK, 0.04, 0.07);

        private final PlayableEffect.BiFunction<@Nullable CombatEntity, Double> effectFunction;

        DamageParticle(Material material, double countHitMultiplier, double countMultiplier) {
            this.effectFunction = (combatEntity, damage) ->
                    ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, material, 0)
                            .count((int) (damage * (combatEntity == null ? countHitMultiplier : countMultiplier)))
                            .horizontalSpread(combatEntity == null ? 0 : combatEntity.getWidth() * 0.25)
                            .verticalSpread(combatEntity == null ? 0 : combatEntity.getHeight() * 0.25)
                            .speed(0.1).build();
        }

        /**
         * 지정한 엔티티에 피해 입자 효과를 재생한다.
         *
         * @param combatEntity 대상 엔티티
         * @param location     대상 위치. {@code null}로 지정 시 {@code combatEntity}의 위치 사용
         * @param damage       피해량. 0 이상의 값
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public void play(@NonNull CombatEntity combatEntity, @Nullable Location location, double damage) {
            Validate.isTrue(damage >= 0, "damage >= 0 (%f)", damage);

            if (location == null)
                effectFunction.apply(combatEntity, damage).play(combatEntity.getCenterLocation());
            else
                effectFunction.apply(null, damage).play(location);
        }
    }
}
