package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * 입자 생성 기능을 제공하는 클래스.
 */
@UtilityClass
public final class ParticleUtil {
    /**
     * 지정한 위치에 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY  입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ  입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed    입자의 속도
     */
    public static void play(@NonNull Particle particle, @NonNull Location location, int count, double offsetX,
                            double offsetY, double offsetZ, double speed) {
        WrapperPlayServerWorldParticles packet = getParticlePacket(particle, location, count, offsetX, offsetY, offsetZ, (float) speed);

        packet.broadcastPacket();
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY  입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ  입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed    입자의 속도
     * @param player   대상 플레이어
     */
    public static void play(@NonNull Particle particle, @NonNull Location location, int count, double offsetX,
                            double offsetY, double offsetZ, double speed, @NonNull Player player) {
        WrapperPlayServerWorldParticles packet = getParticlePacket(particle, location, count, offsetX, offsetY, offsetZ, (float) speed);

        packet.sendPacket(player);
    }

    /**
     * 지정한 위치에 색이 있는 입자를 생성한다.
     *
     * @param coloredParticle 색 입자 종류
     * @param location        대상 위치
     * @param count           입자의 양
     * @param offsetX         입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY         입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ         입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param red             빨강. 0~255 사이의 값
     * @param green           초록. 0~255 사이의 값
     * @param blue            파랑. 0~255 사이의 값
     * @throws IllegalArgumentException {@code red}, {@code green} 또는 {@code blue}가 0~255 사이가 아니면 발생
     */
    public static void playRGB(@NonNull ColoredParticle coloredParticle, @NonNull Location location, int count,
                               double offsetX, double offsetY, double offsetZ, int red, int green, int blue) {
        validateRGB(red, green, blue);

        for (int i = 0; i < count; i++) {
            WrapperPlayServerWorldParticles packet = getRGBParticlePacket(coloredParticle, location, offsetX, offsetY, offsetZ,
                    Math.max(1, red), Math.max(1, green), Math.max(1, blue));

            packet.broadcastPacket();
        }
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 색이 있는 입자를 생성한다.
     *
     * @param coloredParticle 색 입자 종류
     * @param location        대상 위치
     * @param count           입자의 양
     * @param offsetX         입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY         입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ         입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param red             빨강. 0~255 사이의 값
     * @param green           초록. 0~255 사이의 값
     * @param blue            파랑. 0~255 사이의 값
     * @param player          대상 플레이어
     * @throws IllegalArgumentException {@code red}, {@code green} 또는 {@code blue}가 0~255 사이가 아니면 발생
     */
    public static void playRGB(@NonNull ColoredParticle coloredParticle, @NonNull Location location, int count,
                               double offsetX, double offsetY, double offsetZ, int red, int green, int blue, @NonNull Player player) {
        validateRGB(red, green, blue);

        for (int i = 0; i < count; i++) {
            WrapperPlayServerWorldParticles packet = getRGBParticlePacket(coloredParticle, location, offsetX, offsetY, offsetZ,
                    Math.max(1, red), Math.max(1, green), Math.max(1, blue));

            packet.sendPacket(player);
        }
    }

    /**
     * RGB 색상 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param red   빨강
     * @param green 초록
     * @param blue  파랑
     * @throws IllegalArgumentException {@code red}, {@code green} 또는 {@code blue}가 0~255 사이가 아니면 발생
     */
    private static void validateRGB(int red, int green, int blue) {
        if (red < 0 || red > 255)
            throw new IllegalArgumentException("'red'가 0에서 255 사이여야 함");
        else if (blue < 0 || blue > 255)
            throw new IllegalArgumentException("'green'이 0에서 255 사이여야 함");
        else if (green < 0 || green > 255)
            throw new IllegalArgumentException("'blue'가 0에서 255 사이여야 함");
    }

    /**
     * 지정한 위치에 블록 입자를 생성한다.
     *
     * @param blockParticle 블록 입자 종류
     * @param material      블록 종류
     * @param data          블록의 데이터. 나무의 종류나 양털의 색 등
     * @param location      대상 위치
     * @param count         입자의 양
     * @param offsetX       입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY       입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ       입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed         입자의 속도
     */
    public static void playBlock(@NonNull BlockParticle blockParticle, @NonNull Material material, int data,
                                 @NonNull Location location, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        WrapperPlayServerWorldParticles packet = getBlockParticlePacket(blockParticle, material, data, location, count, offsetX,
                offsetY, offsetZ, (float) speed);

        packet.broadcastPacket();
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 블록 입자를 생성한다.
     *
     * @param blockParticle 블록 입자 종류
     * @param material      블록 종류
     * @param data          블록의 데이터. 나무의 종류나 양털의 색 등
     * @param location      대상 위치
     * @param count         입자의 양
     * @param offsetX       입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY       입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ       입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed         입자의 속도
     * @param player        대상 플레이어
     */
    public static void playBlock(@NonNull BlockParticle blockParticle, @NonNull Material material, int data,
                                 @NonNull Location location, int count, double offsetX, double offsetY, double offsetZ, double speed, @NonNull Player player) {
        WrapperPlayServerWorldParticles packet = getBlockParticlePacket(blockParticle, material, data, location, count, offsetX,
                offsetY, offsetZ, (float) speed);

        packet.sendPacket(player);
    }

    /**
     * 지정한 위치에 블록 타격 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param block    블록
     */
    public static void playBlockHitEffect(@NonNull Location location, @NonNull Block block) {
        switch (ParticleUtil.getBlockTexture(block.getType())) {
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
     * 일반 입자 패킷을 반환한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY  입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ  입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed    입자의 속도
     * @return 입자 패킷
     */
    private static WrapperPlayServerWorldParticles getParticlePacket(@NonNull Particle particle, @NonNull Location location,
                                                                     int count, double offsetX, double offsetY, double offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        packet.setParticleType(EnumWrappers.Particle.valueOf(particle.toString()));
        packet.setX((float) location.getX());
        packet.setY((float) location.getY());
        packet.setZ((float) location.getZ());
        packet.setNumberOfParticles(count);
        packet.setOffsetX((float) offsetX);
        packet.setOffsetY((float) offsetY);
        packet.setOffsetZ((float) offsetZ);
        packet.setParticleData(speed);
        packet.setLongDistance(true);

        return packet;
    }

    /**
     * 색이 있는 입자 패킷을 반환한다.
     *
     * @param coloredParticle 색 입자 종류
     * @param location        대상 위치
     * @param offsetX         입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY         입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ         입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param red             빨강. 0~255 사이의 값
     * @param green           초록. 0~255 사이의 값
     * @param blue            파랑. 0~255 사이의 값
     * @return 입자 패킷
     */
    private static WrapperPlayServerWorldParticles getRGBParticlePacket(@NonNull ColoredParticle coloredParticle, @NonNull Location location,
                                                                        double offsetX, double offsetY, double offsetZ, int red, int green, int blue) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        double finalOffsetX = (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * offsetX;
        double finalOffsetY = (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * offsetY;
        double finalOffsetZ = (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * offsetZ;
        packet.setParticleType(EnumWrappers.Particle.valueOf(coloredParticle.toString()));
        packet.setX((float) ((float) location.getX() + finalOffsetX));
        packet.setY((float) ((float) location.getY() + finalOffsetY));
        packet.setZ((float) ((float) location.getZ() + finalOffsetZ));
        packet.setNumberOfParticles(0);
        packet.setOffsetX(red / 255F);
        packet.setOffsetY(green / 255F);
        packet.setOffsetZ(blue / 255F);
        packet.setParticleData(1);
        packet.setLongDistance(true);

        return packet;
    }

    /**
     * 블록 입자 패킷을 반환한다.
     *
     * @param blockParticle 블록 입자 종류
     * @param material      블록 종류
     * @param data          블록의 데이터. 나무의 종류나 양털의 색 등
     * @param location      대상 위치
     * @param count         입자의 양
     * @param offsetX       입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY       입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ       입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed         입자의 속도
     * @return 입자 패킷
     */
    private static WrapperPlayServerWorldParticles getBlockParticlePacket(@NonNull BlockParticle blockParticle, @NonNull Material material,
                                                                          int data, Location location, int count, double offsetX, double offsetY, double offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        packet.setParticleType(EnumWrappers.Particle.valueOf(blockParticle.toString()));
        packet.setX((float) location.getX());
        packet.setY((float) location.getY());
        packet.setZ((float) location.getZ());
        packet.setNumberOfParticles(count);
        packet.setOffsetX((float) offsetX);
        packet.setOffsetY((float) offsetY);
        packet.setOffsetZ((float) offsetZ);
        packet.setParticleData(speed);
        packet.setData(new int[]{material.getId() + 4096 * data});
        packet.setLongDistance(true);

        return packet;
    }

    /**
     * 블록의 재질을 반환한다.
     *
     * @param material 아이템 타입
     */
    @NonNull
    public static Texture getBlockTexture(@NonNull Material material) {
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
    public enum Texture {
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

    /**
     * 색이 있는 입자 종류.
     */
    public enum ColoredParticle {
        /** 레드스톤 입자 */
        REDSTONE,
        /** 포션 이펙트 입자 */
        SPELL_MOB,
        /** 포션 이펙트 입자(반투명) */
        SPELL_MOB_AMBIENT
    }

    /**
     * 블록 입자 종류.
     */
    public enum BlockParticle {
        /** 블록 파편 입자 */
        BLOCK_DUST,
        /** 블록 먼지 입자 */
        FALLING_DUST
    }
}
