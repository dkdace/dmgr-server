package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
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
     * 지정한 위치 또는 엔티티에 출혈 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param entity   대상 엔티티
     * @param damage   피해량
     */
    public static void playBleeding(Location location, Entity entity, int damage) {
        if (location == null)
            playBleeding(entity, damage);
        else
            playBleeding(location, damage);
    }


    /**
     * 지정한 위치에 출혈 효과를 재생한다.
     *
     * @param location 대상 위치
     * @param damage   피해량
     */
    public static void playBleeding(@NonNull Location location, int damage) {
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.REDSTONE_BLOCK, 0, location, (int) Math.ceil(damage * 0.06),
                0, 0, 0, 0.1);
    }

    /**
     * 지정한 엔티티에 출혈 효과를 재생한다.
     *
     * @param entity 대상 엔티티
     * @param damage 피해량
     */
    public static void playBleeding(@NonNull Entity entity, int damage) {
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.REDSTONE_BLOCK, 0,
                entity.getLocation().add(0, entity.getHeight() / 2, 0), (int) Math.ceil(damage * 0.1),
                entity.getWidth() / 4, entity.getHeight() / 4, entity.getWidth() / 4, damage == 0 ? 0.03 : 0.1);
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
