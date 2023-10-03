package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * 입자 생성 기능을 제공하는 클래스.
 */
public final class ParticleUtil {
    /**
     * 지정한 위치에 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param speed    입자의 속도
     */
    public static void play(Particle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = getParticlePacket(particle, location, count, offsetX, offsetY, offsetZ, speed);

        packet.broadcastPacket();
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param speed    입자의 속도
     * @param player   대상 플레이어
     */
    public static void play(Particle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed, Player player) {
        WrapperPlayServerWorldParticles packet = getParticlePacket(particle, location, count, offsetX, offsetY, offsetZ, speed);

        packet.sendPacket(player);
    }

    /**
     * 지정한 위치에 색이 있는 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param red      빨강. {@code 0 ~ 255} 사이의 값
     * @param green    초록. {@code 0 ~ 255} 사이의 값
     * @param blue     파랑. {@code 0 ~ 255} 사이의 값
     */
    public static void playRGB(ColoredParticle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, int red, int green, int blue) {
        for (int i = 0; i < count; i++) {
            WrapperPlayServerWorldParticles packet = getRGBParticlePacket(particle, location, offsetX, offsetY, offsetZ,
                    red, green, blue);

            packet.broadcastPacket();
        }
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 색이 있는 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param red      빨강. {@code 0 ~ 255} 사이의 값
     * @param green    초록. {@code 0 ~ 255} 사이의 값
     * @param blue     파랑. {@code 0 ~ 255} 사이의 값
     * @param player   대상 플레이어
     */
    public static void playRGB(ColoredParticle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, int red, int green, int blue, Player player) {
        for (int i = 0; i < count; i++) {
            WrapperPlayServerWorldParticles packet = getRGBParticlePacket(particle, location, offsetX, offsetY, offsetZ,
                    red, green, blue);

            packet.sendPacket(player);
        }
    }

    /**
     * 지정한 위치에 블록 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param material 블록 종류
     * @param data     블록의 데이터. 나무의 종류나 양털의 색 등
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param speed    입자의 속도
     */
    public static void playBlock(BlockParticle particle, Material material, int data, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = getBlockParticlePacket(particle, material, data, location, count, offsetX,
                offsetY, offsetZ, speed);

        packet.broadcastPacket();
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 블록 입자를 생성한다.
     *
     * @param particle 입자 종류
     * @param material 블록 종류
     * @param data     블록의 데이터. 나무의 종류나 양털의 색 등
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param speed    입자의 속도
     * @param player   대상 플레이어
     */
    public static void playBlock(BlockParticle particle, Material material, int data, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed, Player player) {
        WrapperPlayServerWorldParticles packet = getBlockParticlePacket(particle, material, data, location, count, offsetX,
                offsetY, offsetZ, speed);

        packet.sendPacket(player);
    }

    /**
     * 일반 입자 패킷을 반환한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param speed    입자의 속도
     * @return 입자 패킷
     */
    private static WrapperPlayServerWorldParticles getParticlePacket(Particle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        packet.setParticleType(EnumWrappers.Particle.valueOf(particle.toString()));
        packet.setX((float) location.getX());
        packet.setY((float) location.getY());
        packet.setZ((float) location.getZ());
        packet.setNumberOfParticles(count);
        packet.setOffsetX(offsetX);
        packet.setOffsetY(offsetY);
        packet.setOffsetZ(offsetZ);
        packet.setParticleData(speed);
        packet.setLongDistance(true);

        return packet;
    }

    /**
     * 색이 있는 입자 패킷을 반환한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param red      빨강. {@code 0 ~ 255} 사이의 값
     * @param green    초록. {@code 0 ~ 255} 사이의 값
     * @param blue     파랑. {@code 0 ~ 255} 사이의 값
     * @return 입자 패킷
     */
    private static WrapperPlayServerWorldParticles getRGBParticlePacket(ColoredParticle particle, Location location, float offsetX, float offsetY, float offsetZ, int red, int green, int blue) {
        if (red == 0) red = 1;
        if (green == 0) green = 1;
        if (blue == 0) blue = 1;

        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        float finalOffsetX = (float) ((Math.random() - Math.random()) * offsetX);
        float finalOffsetY = (float) ((Math.random() - Math.random()) * offsetY);
        float finalOffsetZ = (float) ((Math.random() - Math.random()) * offsetZ);
        packet.setParticleType(EnumWrappers.Particle.valueOf(particle.toString()));
        packet.setX((float) location.getX() + finalOffsetX);
        packet.setY((float) location.getY() + finalOffsetY);
        packet.setZ((float) location.getZ() + finalOffsetZ);
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
     * @param particle 입자 종류
     * @param material 블록 종류
     * @param data     블록의 데이터. 나무의 종류나 양털의 색 등
     * @param location 대상 위치
     * @param count    입자의 양
     * @param offsetX  입자가 X축으로 퍼지는 범위
     * @param offsetY  입자가 Y축으로 퍼지는 범위
     * @param offsetZ  입자가 Z축으로 퍼지는 범위
     * @param speed    입자의 속도
     * @return 입자 패킷
     */
    private static WrapperPlayServerWorldParticles getBlockParticlePacket(BlockParticle particle, Material material, int data, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        packet.setParticleType(EnumWrappers.Particle.valueOf(particle.toString()));
        packet.setX((float) location.getX());
        packet.setY((float) location.getY());
        packet.setZ((float) location.getZ());
        packet.setNumberOfParticles(count);
        packet.setOffsetX(offsetX);
        packet.setOffsetY(offsetY);
        packet.setOffsetZ(offsetZ);
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
