package com.dace.dmgr.util;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleUtil {
    public static void play(Particle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = getNormalPacket(particle, location, count, offsetX, offsetY, offsetZ, speed);

        Bukkit.getOnlinePlayers().forEach(packet::sendPacket);
    }

    public static void play(Particle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed, Player player) {
        WrapperPlayServerWorldParticles packet = getNormalPacket(particle, location, count, offsetX, offsetY, offsetZ, speed);

        packet.sendPacket(player);
    }


    public static void playRGB(Location location, int count, float offsetX, float offsetY, float offsetZ, float red, float green, float blue) {
        for (int i = 0; i < count; i++) {
            WrapperPlayServerWorldParticles packet = getRGBPacket(location, offsetX, offsetY, offsetZ, red, green, blue);

            Bukkit.getOnlinePlayers().forEach(packet::sendPacket);
        }
    }

    public static void playRGB(Location location, int count, float offsetX, float offsetY, float offsetZ, float red, float green, float blue, Player player) {
        for (int i = 0; i < count; i++) {
            WrapperPlayServerWorldParticles packet = getRGBPacket(location, offsetX, offsetY, offsetZ, red, green, blue);

            packet.sendPacket(player);
        }
    }

    public static void playBlock(Material material, int data, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = getBlockPacket(material, data, location, count, offsetX, offsetY, offsetZ, speed);

        Bukkit.getOnlinePlayers().forEach(packet::sendPacket);
    }

    public static void playBlock(Material material, int data, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed, Player player) {
        WrapperPlayServerWorldParticles packet = getBlockPacket(material, data, location, count, offsetX, offsetY, offsetZ, speed);

        packet.sendPacket(player);
    }


    private static WrapperPlayServerWorldParticles getNormalPacket(Particle particle, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
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

    private static WrapperPlayServerWorldParticles getRGBPacket(Location location, float offsetX, float offsetY, float offsetZ, float red, float green, float blue) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        float finalOffsetX = (float) ((Math.random() - Math.random()) * offsetX);
        float finalOffsetY = (float) ((Math.random() - Math.random()) * offsetY);
        float finalOffsetZ = (float) ((Math.random() - Math.random()) * offsetZ);
        packet.setParticleType(EnumWrappers.Particle.REDSTONE);
        packet.setX((float) location.getX() + finalOffsetX);
        packet.setY((float) location.getY() + finalOffsetY);
        packet.setZ((float) location.getZ() + finalOffsetZ);
        packet.setNumberOfParticles(0);
        packet.setOffsetX(red / 255);
        packet.setOffsetY(green / 255);
        packet.setOffsetZ(blue / 255);
        packet.setParticleData(1);

        return packet;
    }

    private static WrapperPlayServerWorldParticles getBlockPacket(Material material, int data, Location location, int count, float offsetX, float offsetY, float offsetZ, float speed) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        packet.setParticleType(EnumWrappers.Particle.BLOCK_DUST);
        packet.setX((float) location.getX());
        packet.setY((float) location.getY());
        packet.setZ((float) location.getZ());
        packet.setNumberOfParticles(count);
        packet.setOffsetX(offsetX);
        packet.setOffsetY(offsetY);
        packet.setOffsetZ(offsetZ);
        packet.setParticleData(speed);
        packet.setData(new int[]{material.getId() + 4096 * data});

        return packet;
    }
}
