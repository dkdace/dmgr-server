package com.dace.dmgr.effect;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * 입자(파티클) 효과 생성 기능을 제공하는 클래스.
 *
 * @see Normal
 * @see Colored
 * @see Directional
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ParticleEffect extends PlayableEffect {
    /** 입자 종류 */
    private final Particle particle;

    /**
     * 입자 패킷을 반환한다.
     *
     * @param location          생성 위치
     * @param numberOfParticles 입자의 개수
     * @param offsetX           X축 오프셋
     * @param offsetY           Y축 오프셋
     * @param offsetZ           Z축 오프셋
     * @param data              데이터 (주로 블록 데이터)
     * @param particleData      파티클 데이터 (주로 속도)
     * @return 입자 패킷 인스턴스
     */
    @NonNull
    private WrapperPlayServerWorldParticles getParticlePacket(@NonNull Location location, int numberOfParticles, float offsetX, float offsetY,
                                                              float offsetZ, int data, float particleData) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

        packet.setParticleType(EnumWrappers.Particle.valueOf(particle.toString()));
        packet.setX((float) location.getX());
        packet.setY((float) location.getY());
        packet.setZ((float) location.getZ());
        packet.setNumberOfParticles(numberOfParticles);
        packet.setOffsetX(offsetX);
        packet.setOffsetY(offsetY);
        packet.setOffsetZ(offsetZ);
        packet.setData(new int[]{data});
        packet.setParticleData(particleData);
        packet.setLongDistance(true);

        return packet;
    }

    /**
     * 블록 입자 종류.
     */
    @AllArgsConstructor
    public enum BlockParticleType {
        /** 블록 파편 입자 */
        BLOCK_DUST(Particle.BLOCK_DUST),
        /** 블록 먼지 입자 */
        FALLING_DUST(Particle.FALLING_DUST);

        /** 입자 종류 */
        private final Particle particle;
    }

    /**
     * 일반 입자 효과를 나타내는 클래스.
     */
    public static final class Normal extends ParticleEffect {
        /** 입자의 개수 */
        private final int count;
        /** 입자가 수평으로 퍼지는 범위 (단위: 블록) */
        private final float horizontalSpread;
        /** 입자가 수직으로 퍼지는 범위 (단위: 블록) */
        private final float verticalSpread;
        /** 속력 */
        private final float speed;
        /** 데이터 값 */
        private final int data;

        @SuppressWarnings("deprecation")
        private Normal(Builder builder) {
            super(builder.particle);

            this.count = builder.count;
            this.horizontalSpread = (float) builder.horizontalSpread;
            this.verticalSpread = (float) builder.verticalSpread;
            this.speed = (float) builder.speed;
            this.data = builder.data.getItemTypeId() + 4096 * builder.data.getData();
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * @param particle 입자 종류
         * @return {@link Builder}
         */
        @NonNull
        public static Builder builder(@NonNull Particle particle) {
            return new Builder(particle, new MaterialData(Material.AIR));
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * @param blockParticleType 블록 입자 종류
         * @param material          블록 종류
         * @param data              블록의 데이터. 나무의 종류나 양털의 색 등
         * @return {@link Builder}
         */
        @NonNull
        @SuppressWarnings("deprecation")
        public static Builder builder(@NonNull BlockParticleType blockParticleType, @NonNull Material material, int data) {
            return new Builder(blockParticleType.particle, new MaterialData(material, (byte) data));
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * @param blockParticleType 블록 입자 종류
         * @param block             블록
         * @return {@link Builder}
         */
        @NonNull
        public static Builder builder(@NonNull BlockParticleType blockParticleType, @NonNull Block block) {
            return new Builder(blockParticleType.particle, block.getState().getData());
        }

        @Override
        public void play(@NonNull Location location, @NonNull Player player) {
            super.getParticlePacket(location, count, horizontalSpread, verticalSpread, horizontalSpread, data, speed).sendPacket(player);
        }

        /**
         * {@link Normal}의 빌더 클래스.
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {
            private final Particle particle;
            private final MaterialData data;
            private int count = 1;
            private double horizontalSpread = 0;
            private double verticalSpread = 0;
            private double speed = 0;

            /**
             * 입자의 개수를 설정한다.
             *
             * @param count 입자의 개수. 0 이상의 값
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder count(int count) {
                Validate.isTrue(count >= 0, "count >= 0 (%d)", count);

                this.count = count;
                return this;
            }

            /**
             * 입자가 수평으로 퍼지는 범위를 설정한다.
             *
             * @param horizontalSpread 수평으로 퍼지는 범위. (단위: 블록)
             * @return {@link Builder}
             */
            @NonNull
            public Builder horizontalSpread(double horizontalSpread) {
                this.horizontalSpread = horizontalSpread;
                return this;
            }

            /**
             * 입자가 수직으로 퍼지는 범위를 설정한다.
             *
             * @param verticalSpread 수직으로 퍼지는 범위. (단위: 블록)
             * @return {@link Builder}
             */
            @NonNull
            public Builder verticalSpread(double verticalSpread) {
                this.verticalSpread = verticalSpread;
                return this;
            }

            /**
             * 입자의 속력을 설정한다.
             *
             * @param speed 속력
             * @return {@link Builder}
             */
            @NonNull
            public Builder speed(double speed) {
                this.speed = speed;
                return this;
            }

            /**
             * 일반 입자 효과를 생성하여 반환한다.
             *
             * @return {@link Normal}
             */
            @NonNull
            public Normal build() {
                return new Normal(this);
            }
        }
    }

    /**
     * 색이 있는 입자 효과를 나타내는 클래스.
     */
    public static final class Colored extends ParticleEffect {
        /** 빨강 값 */
        private final float red;
        /** 초록 값 */
        private final float green;
        /** 파랑 값 */
        private final float blue;
        /** 입자가 수평으로 퍼지는 범위 (단위: 블록) */
        private final double horizontalSpread;
        /** 입자가 수직으로 퍼지는 범위 (단위: 블록) */
        private final double verticalSpread;
        /** 입자의 개수 */
        private final int count;

        private Colored(Builder builder) {
            super(builder.particleType.particle);

            Color color = builder.color;
            this.red = Math.max(1, color.getRed()) / 255F;
            this.green = Math.max(1, color.getGreen()) / 255F;
            this.blue = Math.max(1, color.getBlue()) / 255F;
            this.horizontalSpread = builder.horizontalSpread;
            this.verticalSpread = builder.verticalSpread;
            this.count = builder.count;
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * @param particleType 색이 있는 입자 종류
         * @param color        색상
         * @return {@link Builder}
         */
        @NonNull
        public static Builder builder(@NonNull ParticleType particleType, @NonNull Color color) {
            return new Builder(particleType, Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()));
        }

        @Override
        public void play(@NonNull Location location, @NonNull Player player) {
            for (int i = 0; i < count; i++) {
                Location loc = location.clone();

                loc.setX(loc.getX() + (Math.random() - Math.random()) * horizontalSpread);
                loc.setY(loc.getY() + (Math.random() - Math.random()) * verticalSpread);
                loc.setZ(loc.getZ() + (Math.random() - Math.random()) * horizontalSpread);

                super.getParticlePacket(loc, 0, red, green, blue, 0, 1).sendPacket(player);
            }
        }

        /**
         * 색이 있는 입자 종류.
         */
        @AllArgsConstructor
        public enum ParticleType {
            /** 레드스톤 입자 */
            REDSTONE(Particle.REDSTONE),
            /** 포션 이펙트 입자 */
            SPELL_MOB(Particle.SPELL_MOB),
            /** 포션 이펙트 입자(반투명) */
            SPELL_MOB_AMBIENT(Particle.SPELL_MOB_AMBIENT);

            /** 입자 종류 */
            private final Particle particle;
        }

        /**
         * {@link Colored}의 빌더 클래스.
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {
            private final ParticleType particleType;
            private final Color color;
            private int count = 1;
            private double horizontalSpread = 0;
            private double verticalSpread = 0;

            /**
             * 입자의 개수를 설정한다.
             *
             * @param count 입자의 개수. 0 이상의 값
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder count(int count) {
                Validate.isTrue(count >= 0, "count >= 0 (%d)", count);

                this.count = count;
                return this;
            }

            /**
             * 입자가 수평으로 퍼지는 범위를 설정한다.
             *
             * @param horizontalSpread 수평으로 퍼지는 범위. (단위: 블록)
             * @return {@link Builder}
             */
            @NonNull
            public Builder horizontalSpread(double horizontalSpread) {
                this.horizontalSpread = horizontalSpread;
                return this;
            }

            /**
             * 입자가 수직으로 퍼지는 범위를 설정한다.
             *
             * @param verticalSpread 수직으로 퍼지는 범위. (단위: 블록)
             * @return {@link Builder}
             */
            @NonNull
            public Builder verticalSpread(double verticalSpread) {
                this.verticalSpread = verticalSpread;
                return this;
            }

            /**
             * 색이 있는 입자 효과를 생성하여 반환한다.
             *
             * @return {@link Colored}
             */
            @NonNull
            public Colored build() {
                return new Colored(this);
            }
        }
    }

    /**
     * 특정 방향으로 움직이는 입자 효과를 나타내는 클래스.
     */
    public static final class Directional extends ParticleEffect {
        /** X축 속력 */
        private final float speedX;
        /** Y축 속력 */
        private final float speedY;
        /** Z축 속력 */
        private final float speedZ;

        private Directional(Particle particle, Vector velocity) {
            super(particle);

            this.speedX = (float) velocity.getX();
            this.speedY = (float) velocity.getY();
            this.speedZ = (float) velocity.getZ();
        }

        /**
         * 움직이는 입자 효과를 생성하여 반환한다.
         *
         * @param particle 입자 종류
         * @param velocity 속도
         * @return {@link Directional}
         */
        @NonNull
        public static Directional create(@NonNull Particle particle, @NonNull Vector velocity) {
            return new Directional(particle, velocity);
        }

        @Override
        public void play(@NonNull Location location, @NonNull Player player) {
            super.getParticlePacket(location, 0, speedX, speedY, speedZ, 0, 1).sendPacket(player);
        }
    }
}
