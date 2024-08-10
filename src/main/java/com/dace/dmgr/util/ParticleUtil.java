package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.util.task.DelayTask;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

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
     * @param count    입자의 양. 0 이상의 값
     * @param offsetX  입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY  입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ  입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed    입자의 속도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
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
     * @param count    입자의 양. 0 이상의 값
     * @param offsetX  입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY  입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ  입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed    입자의 속도
     * @param player   대상 플레이어
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
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
     * @param count           입자의 양. 0 이상의 값
     * @param offsetX         입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY         입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ         입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param red             빨강. 0~255 사이의 값
     * @param green           초록. 0~255 사이의 값
     * @param blue            파랑. 0~255 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playRGB(@NonNull ColoredParticle coloredParticle, @NonNull Location location, int count,
                               double offsetX, double offsetY, double offsetZ, int red, int green, int blue) {
        validateArgs(count);
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
     * @param count           입자의 양. 0 이상의 값
     * @param offsetX         입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY         입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ         입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param red             빨강. 0~255 사이의 값
     * @param green           초록. 0~255 사이의 값
     * @param blue            파랑. 0~255 사이의 값
     * @param player          대상 플레이어
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playRGB(@NonNull ColoredParticle coloredParticle, @NonNull Location location, int count,
                               double offsetX, double offsetY, double offsetZ, int red, int green, int blue, @NonNull Player player) {
        validateArgs(count);
        validateRGB(red, green, blue);

        for (int i = 0; i < count; i++) {
            WrapperPlayServerWorldParticles packet = getRGBParticlePacket(coloredParticle, location, offsetX, offsetY, offsetZ,
                    Math.max(1, red), Math.max(1, green), Math.max(1, blue));

            packet.sendPacket(player);
        }
    }

    /**
     * 지정한 위치에 블록 입자를 생성한다.
     *
     * @param blockParticle 블록 입자 종류
     * @param material      블록 종류
     * @param data          블록의 데이터. 나무의 종류나 양털의 색 등
     * @param location      대상 위치
     * @param count         입자의 양. 0 이상의 값
     * @param offsetX       입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY       입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ       입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed         입자의 속도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
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
     * @param count         입자의 양. 0 이상의 값
     * @param offsetX       입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY       입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ       입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed         입자의 속도
     * @param player        대상 플레이어
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playBlock(@NonNull BlockParticle blockParticle, @NonNull Material material, int data,
                                 @NonNull Location location, int count, double offsetX, double offsetY, double offsetZ, double speed, @NonNull Player player) {
        WrapperPlayServerWorldParticles packet = getBlockParticlePacket(blockParticle, material, data, location, count, offsetX,
                offsetY, offsetZ, (float) speed);

        packet.sendPacket(player);
    }

    /**
     * 지정한 위치에 폭죽 효과를 생성한다.
     *
     * @param location   대상 위치
     * @param red        빨강. 0~255 사이의 값
     * @param green      초록. 0~255 사이의 값
     * @param blue       파랑. 0~255 사이의 값
     * @param fadeRed    빨강 (사라지는 색). 0~255 사이의 값
     * @param fadeGreen  초록 (사라지는 색). 0~255 사이의 값
     * @param fadeBlue   파랑 (사라지는 색). 0~255 사이의 값
     * @param type       폭죽 효과 타입
     * @param hasTrail   잔상 여부
     * @param hasFlicker 반짝임 여부
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void playFirework(@NonNull Location location, int red, int green, int blue, int fadeRed, int fadeGreen, int fadeBlue,
                                    @NonNull FireworkEffect.Type type, boolean hasTrail, boolean hasFlicker) {
        validateRGB(red, green, blue);
        validateRGB(fadeRed, fadeGreen, fadeBlue);

        Firework firework = location.getWorld().spawn(location, Firework.class);

        firework.setSilent(true);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder().with(type)
                .withColor(Color.fromRGB(red, green, blue))
                .withFade(Color.fromRGB(fadeRed, fadeGreen, fadeBlue))
                .trail(hasTrail)
                .flicker(hasFlicker)
                .build());
        firework.setFireworkMeta(fireworkMeta);

        new DelayTask(firework::detonate, 1);
    }

    /**
     * 일반 입자 패킷을 반환한다.
     *
     * @param particle 입자 종류
     * @param location 대상 위치
     * @param count    입자의 양. 0 이상의 값
     * @param offsetX  입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY  입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ  입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed    입자의 속도
     * @return 입자 패킷
     */
    @NonNull
    private static WrapperPlayServerWorldParticles getParticlePacket(@NonNull Particle particle, @NonNull Location location,
                                                                     int count, double offsetX, double offsetY, double offsetZ, float speed) {
        validateArgs(count);

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
    @NonNull
    private static WrapperPlayServerWorldParticles getRGBParticlePacket(@NonNull ColoredParticle coloredParticle, @NonNull Location location,
                                                                        double offsetX, double offsetY, double offsetZ, int red, int green, int blue) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        double finalOffsetX = (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * offsetX;
        double finalOffsetY = (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * offsetY;
        double finalOffsetZ = (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * offsetZ;
        packet.setParticleType(EnumWrappers.Particle.valueOf(coloredParticle.toString()));
        packet.setX((float) (location.getX() + finalOffsetX));
        packet.setY((float) (location.getY() + finalOffsetY));
        packet.setZ((float) (location.getZ() + finalOffsetZ));
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
     * @param count         입자의 양. 0 이상의 값
     * @param offsetX       입자가 X축으로 퍼지는 범위. (단위: 블록)
     * @param offsetY       입자가 Y축으로 퍼지는 범위. (단위: 블록)
     * @param offsetZ       입자가 Z축으로 퍼지는 범위. (단위: 블록)
     * @param speed         입자의 속도
     * @return 입자 패킷
     */
    @NonNull
    private static WrapperPlayServerWorldParticles getBlockParticlePacket(@NonNull BlockParticle blockParticle, @NonNull Material material, int data,
                                                                          @NonNull Location location, int count, double offsetX, double offsetY, double offsetZ, float speed) {
        validateArgs(count);

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
     * 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param count 입자의 양
     */
    private static void validateArgs(int count) {
        if (count < 0)
            throw new IllegalArgumentException("'count'가 0 이상이어야 함");
    }

    /**
     * RGB 색상 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param red   빨강
     * @param green 초록
     * @param blue  파랑
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
