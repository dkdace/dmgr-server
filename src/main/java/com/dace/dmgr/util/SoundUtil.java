package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * 소리 재생 기능을 제공하는 클래스.
 */
@UtilityClass
public final class SoundUtil {
    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound    소리 종류
     * @param location 위치
     * @param volume   음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch    음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Location location, double volume, double pitch) {
        validatePitch(pitch);
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound            소리 종류
     * @param location         위치
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Location location, double volume, double pitch, double pitchSpreadRange) {
        play(sound, location, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound    소리 이름
     * @param location 위치
     * @param volume   음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch    음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Location location, double volume, double pitch) {
        validatePitch(pitch);
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound            소리 이름
     * @param location         위치
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Location location, double volume, double pitch, double pitchSpreadRange) {
        play(sound, location, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리 종류
     * @param player 대상 플레이어
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Player player, double volume, double pitch) {
        validatePitch(pitch);
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound            소리 종류
     * @param player           대상 플레이어
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Player player, double volume, double pitch, double pitchSpreadRange) {
        play(sound, player, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리 이름
     * @param player 대상 플레이어
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Player player, double volume, double pitch) {
        validatePitch(pitch);
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound            소리 이름
     * @param player           대상 플레이어
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Player player, double volume, double pitch, double pitchSpreadRange) {
        play(sound, player, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 모든 플레이어에게 소리를 재생한다.
     *
     * @param sound  소리 종류
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void broadcast(@NonNull Sound sound, double volume, double pitch) {
        validatePitch(pitch);
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch));
    }

    /**
     * 모든 플레이어에게 소리를 재생한다.
     *
     * @param sound  소리 이름
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void broadcast(@NonNull String sound, double volume, double pitch) {
        validatePitch(pitch);
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch));
    }

    /**
     * 음정 분산도를 적용한 최종 음정 값을 반환한다.
     *
     * @param pitch            음정
     * @param pitchSpreadRange 음정의 분산도
     * @return 최종 음정 값
     */
    private static float getFinalPitch(double pitch, double pitchSpreadRange) {
        double spread = pitchSpreadRange * (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 0.5;
        return (float) Math.max(0.5, Math.min(pitch + spread, 2));
    }

    /**
     * 음정 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param pitch 음정
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    private static void validatePitch(double pitch) {
        if (pitch < 0.5 || pitch > 2)
            throw new IllegalArgumentException("'pitch'가 0.5에서 2 사이여야 함");
    }

    /**
     * 지정한 위치에 블록 타격 효과음을 재생한다.
     *
     * @param location 대상 위치
     * @param block    블록
     */
    public static void playBlockHitSound(@NonNull Location location, @NonNull Block block) {
        switch (getBlockTexture(block.getType())) {
            case GRASS:
                play(Sound.BLOCK_GRASS_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case DIRT:
                play(Sound.BLOCK_GRAVEL_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case STONE:
                play(Sound.BLOCK_STONE_BREAK, location, 1, 0.9, 0.1);
                break;
            case METAL:
                play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, location, 0.5, 1.95, 0.1);
                play("random.metalhit", location, 0.8, 1.95, 0.1);
                break;
            case WOOD:
                play(Sound.BLOCK_WOOD_BREAK, location, 0.8, 0.8, 0.1);
                play("random.stab", location, 0.8, 1.95, 0.1);
                break;
            case GLASS:
                play(Sound.BLOCK_GLASS_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case WOOL:
                play(Sound.BLOCK_CLOTH_BREAK, location, 1, 0.8, 0.1);
                break;
        }
    }

    /**
     * 블록의 재질을 반환한다.
     *
     * @param material 아이템 타입
     */
    @NonNull
    private static Texture getBlockTexture(@NonNull Material material) {
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
