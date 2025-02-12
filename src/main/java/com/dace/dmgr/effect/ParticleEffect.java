package com.dace.dmgr.effect;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.*;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * 일련의 입자(파티클) 효과 생성 기능을 제공하는 클래스.
 */
public final class ParticleEffect {
    /** 입자 효과 목록 */
    private final ParticleInfo[] particleInfos;

    /**
     * 일련의 입자 효과를 생성한다.
     *
     * @param particleInfos 입자 효과 목록
     */
    public ParticleEffect(@NonNull ParticleInfo @NonNull ... particleInfos) {
        this.particleInfos = particleInfos;
    }

    private static void validateDataIndex(int dataIndex) {
        Validate.isTrue(dataIndex >= 0, "dataIndex >= 0 (%d)", dataIndex);
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 입자 효과를 생성한다.
     *
     * @param location      위치
     * @param player        대상 플레이어
     * @param variableDatas 가변 데이터(입자 규모나 블록 데이터 등) 값 목록
     * @throws IllegalArgumentException 지정한 가변 데이터의 값 타입이 일치하지 않으면 발생
     */
    public void play(@NonNull Location location, @NonNull Player player, @NonNull Object @NonNull ... variableDatas) {
        for (ParticleInfo particleInfo : particleInfos)
            particleInfo.onPlay(location, player, variableDatas);
    }

    /**
     * 지정한 위치에 특정 플레이어만 볼 수 있는 입자 효과를 생성한다.
     *
     * @param location 위치
     * @param player   대상 플레이어
     */
    public void play(@NonNull Location location, @NonNull Player player) {
        for (ParticleInfo particleInfo : particleInfos)
            particleInfo.onPlay(location, player, null);
    }

    /**
     * 지정한 위치에 입자 효과를 생성한다.
     *
     * @param location      위치
     * @param variableDatas 가변 데이터(입자 규모나 블록 데이터 등) 값 목록
     * @throws IllegalArgumentException 지정한 가변 데이터의 값 타입이 일치하지 않으면 발생
     */
    public void play(@NonNull Location location, @NonNull Object @NonNull ... variableDatas) {
        location.getWorld().getPlayers().forEach(player -> play(location, player, variableDatas));
    }

    /**
     * 지정한 위치에 입자 효과를 생성한다.
     *
     * @param location 위치
     */
    public void play(@NonNull Location location) {
        location.getWorld().getPlayers().forEach(player -> play(location, player));
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
     * 입자(파티클) 효과를 나타내는 클래스.
     *
     * @see NormalParticleInfo
     * @see ColoredParticleInfo
     * @see DirectionalParticleInfo
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public abstract static class ParticleInfo {
        /** 입자 종류 */
        private final Particle particle;

        /**
         * 입자를 생성했을 때 실행할 작업.
         *
         * @param location      위치
         * @param player        대상 플레이어
         * @param variableDatas 가변 데이터(입자 규모나 블록 데이터 등) 값 목록
         */
        abstract void onPlay(@NonNull Location location, @NonNull Player player, Object @Nullable [] variableDatas);

        /**
         * 입자 패킷을 반환한다.
         *
         * @param x                 생성 위치 X 좌표
         * @param y                 생성 위치 Y 좌표
         * @param z                 생성 위치 Z 좌표
         * @param numberOfParticles 입자의 개수
         * @param offsetX           X축 오프셋
         * @param offsetY           Y축 오프셋
         * @param offsetZ           Z축 오프셋
         * @param data              데이터 (주로 블록 데이터)
         * @param particleData      파티클 데이터 (주로 속도)
         * @return 입자 패킷 인스턴스
         */
        @NonNull
        private WrapperPlayServerWorldParticles getParticlePacket(float x, float y, float z, int numberOfParticles,
                                                                  float offsetX, float offsetY, float offsetZ, int data, float particleData) {
            WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();

            packet.setParticleType(EnumWrappers.Particle.valueOf(particle.toString()));
            packet.setX(x);
            packet.setY(y);
            packet.setZ(z);
            packet.setNumberOfParticles(numberOfParticles);
            packet.setOffsetX(offsetX);
            packet.setOffsetY(offsetY);
            packet.setOffsetZ(offsetZ);
            packet.setData(new int[]{data});
            packet.setParticleData(particleData);
            packet.setLongDistance(true);

            return packet;
        }
    }

    /**
     * 일반 입자 효과를 나타내는 클래스.
     */
    public static final class NormalParticleInfo extends ParticleInfo {
        /** 입자의 개수 */
        private final VariableValue count;
        /** 입자가 수평으로 퍼지는 범위 (단위: 블록) */
        private final VariableValue horizontalSpread;
        /** 입자가 수직으로 퍼지는 범위 (단위: 블록) */
        private final VariableValue verticalSpread;
        /** 속력 */
        private final VariableValue speed;
        /** 데이터 */
        private final FixedValue<MaterialData> data;

        private NormalParticleInfo(Builder builder) {
            super(builder.particle);

            this.count = builder.count;
            this.horizontalSpread = builder.horizontalSpread;
            this.verticalSpread = builder.verticalSpread;
            this.speed = builder.speed;
            this.data = builder.data;
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * @param particle 입자 종류
         * @return {@link Builder}
         */
        @NonNull
        public static Builder builder(@NonNull Particle particle) {
            return new Builder(particle, new FixedValue<>(new MaterialData(Material.AIR)));
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
            return new Builder(blockParticleType.particle, new FixedValue<>(new MaterialData(material, (byte) data)));
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * <p>가변 데이터: {@link MaterialData} 타입의 블록 데이터.</p>
         *
         * @param dataIndex         가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
         * @param blockParticleType 블록 입자 종류
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public static Builder builder(int dataIndex, @NonNull BlockParticleType blockParticleType) {
            validateDataIndex(dataIndex);
            return new Builder(blockParticleType.particle, new FixedValue<>(dataIndex, new MaterialData(Material.AIR)));
        }

        @Override
        @SuppressWarnings("deprecation")
        void onPlay(@NonNull Location location, @NonNull Player player, Object @Nullable [] variableDatas) {
            float offsetXZ = (float) horizontalSpread.getValue(variableDatas);
            float offsetY = (float) verticalSpread.getValue(variableDatas);
            MaterialData materialData = data.getValue(variableDatas);

            super.getParticlePacket((float) location.getX(), (float) location.getY(), (float) location.getZ(), (int) count.getValue(variableDatas),
                    offsetXZ, offsetY, offsetXZ, materialData.getItemTypeId() + 4096 * materialData.getData(),
                    (float) speed.getValue(variableDatas)).sendPacket(player);
        }

        /**
         * {@link NormalParticleInfo}의 빌더 클래스.
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {
            private final Particle particle;
            private final FixedValue<MaterialData> data;
            private final VariableValue count = new VariableValue(1);
            private final VariableValue horizontalSpread = new VariableValue();
            private final VariableValue verticalSpread = new VariableValue();
            private final VariableValue speed = new VariableValue();

            /**
             * 입자의 개수를 설정한다.
             *
             * <p>가변 데이터: {@code double} 타입의 개수의 규모.</p>
             *
             * @param dataIndex 가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
             * @param minCount  입자의 최소 개수. 0 이상의 값
             * @param maxCount  입자의 최대 개수. {@code minCount} 이상의 값
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder count(int dataIndex, int minCount, int maxCount) {
                validateDataIndex(dataIndex);
                Validate.isTrue(minCount >= 0, "minCount >= 0 (%d)", minCount);
                Validate.isTrue(maxCount >= minCount, "maxCount >= %d (%d)", minCount, maxCount);

                count.setValue(dataIndex, minCount, maxCount);
                return this;
            }

            /**
             * 입자의 개수를 설정한다.
             *
             * @param count 입자의 개수. 0 이상의 값
             * @return {@link  Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder count(int count) {
                return count(Integer.MAX_VALUE, count, count);
            }

            /**
             * 입자가 수평으로 퍼지는 범위를 설정한다.
             *
             * <p>가변 데이터: {@code double} 타입의 입자가 수평으로 퍼지는 범위의 규모.</p>
             *
             * @param dataIndex           가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
             * @param minHorizontalSpread 수평으로 퍼지는 최소 범위. (단위: 블록)
             * @param maxHorizontalSpread 수평으로 퍼지는 최대 범위. (단위: 블록)
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder horizontalSpread(int dataIndex, double minHorizontalSpread, double maxHorizontalSpread) {
                validateDataIndex(dataIndex);

                horizontalSpread.setValue(dataIndex, minHorizontalSpread, maxHorizontalSpread);
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
                return horizontalSpread(Integer.MAX_VALUE, horizontalSpread, horizontalSpread);
            }


            /**
             * 입자가 수직으로 퍼지는 범위를 설정한다.
             *
             * <p>가변 데이터: {@code double} 타입의 입자가 수직으로 퍼지는 범위의 규모.</p>
             *
             * @param dataIndex         가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
             * @param minVerticalSpread 수직으로 퍼지는 최소 범위. (단위: 블록)
             * @param maxVerticalSpread 수직으로 퍼지는 최대 범위. (단위: 블록)
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder verticalSpread(int dataIndex, double minVerticalSpread, double maxVerticalSpread) {
                validateDataIndex(dataIndex);

                verticalSpread.setValue(dataIndex, minVerticalSpread, maxVerticalSpread);
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
                return verticalSpread(Integer.MAX_VALUE, verticalSpread, verticalSpread);
            }

            /**
             * 입자의 속력을 설정한다.
             *
             * <p>가변 데이터: {@code double} 타입의 속력의 규모.</p>
             *
             * @param dataIndex 가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
             * @param minSpeed  최소 속력
             * @param maxSpeed  최대 속력
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder speed(int dataIndex, double minSpeed, double maxSpeed) {
                validateDataIndex(dataIndex);

                speed.setValue(dataIndex, minSpeed, maxSpeed);
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
                return speed(Integer.MAX_VALUE, speed, speed);
            }

            /**
             * 입자 효과를 생성하여 반환한다.
             *
             * @return {@link NormalParticleInfo}
             */
            @NonNull
            public NormalParticleInfo build() {
                return new NormalParticleInfo(this);
            }
        }
    }

    /**
     * 색이 있는 입자 효과를 나타내는 클래스.
     */
    public static final class ColoredParticleInfo extends ParticleInfo {
        /** 빨강 값 */
        private final VariableValue red;
        /** 초록 값 */
        private final VariableValue green;
        /** 파랑 값 */
        private final VariableValue blue;
        /** 입자가 수평으로 퍼지는 범위 (단위: 블록) */
        private final VariableValue horizontalSpread;
        /** 입자가 수직으로 퍼지는 범위 (단위: 블록) */
        private final VariableValue verticalSpread;
        /** 입자의 개수 */
        private final int count;

        private ColoredParticleInfo(Builder builder) {
            super(builder.particleType.particle);

            this.red = builder.red;
            this.green = builder.green;
            this.blue = builder.blue;
            this.horizontalSpread = builder.horizontalSpread;
            this.verticalSpread = builder.verticalSpread;
            this.count = builder.count;
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * <p>가변 데이터: {@code double} 타입의 색상의 규모.</p>
         *
         * @param dataIndex    가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
         * @param particleType 색이 있는 입자 종류
         * @param minRed       최소 빨강 값. 0~255 사이의 값
         * @param maxRed       최대 빨강 값. 0~255 사이의 값
         * @param minGreen     최소 초록 값. 0~255 사이의 값
         * @param maxGreen     최대 초록 값. 0~255 사이의 값
         * @param minBlue      최소 파랑 값. 0~255 사이의 값
         * @param maxBlue      최대 파랑 값. 0~255 사이의 값
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public static Builder builder(int dataIndex, @NonNull ParticleType particleType, int minRed, int maxRed, int minGreen, int maxGreen,
                                      int minBlue, int maxBlue) {
            validateDataIndex(dataIndex);
            Validate.inclusiveBetween(0, 255, minRed, "255 >= minRed >= 0 (%d)", minRed);
            Validate.inclusiveBetween(0, 255, maxRed, "255 >= maxRed >= 0 (%d)", maxRed);
            Validate.inclusiveBetween(0, 255, minGreen, "255 >= minGreen >= 0 (%d)", minGreen);
            Validate.inclusiveBetween(0, 255, maxGreen, "255 >= maxGreen >= 0 (%d)", maxGreen);
            Validate.inclusiveBetween(0, 255, minBlue, "255 >= minBlue >= 0 (%d)", minBlue);
            Validate.inclusiveBetween(0, 255, maxBlue, "255 >= maxBlue >= 0 (%d)", maxBlue);

            return new Builder(particleType,
                    new VariableValue(dataIndex, Math.max(1, minRed) / 255.0, Math.max(1, maxRed) / 255.0),
                    new VariableValue(dataIndex, Math.max(1, minGreen) / 255.0, Math.max(1, maxGreen) / 255.0),
                    new VariableValue(dataIndex, Math.max(1, minBlue) / 255.0, Math.max(1, maxBlue) / 255.0));
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * @param particle 입자 종류
         * @param red      빨강. 0~255 사이의 값
         * @param green    초록. 0~255 사이의 값
         * @param blue     파랑. 0~255 사이의 값
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public static Builder builder(@NonNull ParticleType particle, int red, int green, int blue) {
            return builder(Integer.MAX_VALUE, particle, red, red, green, green, blue, blue);
        }

        @Override
        void onPlay(@NonNull Location location, @NonNull Player player, Object @Nullable [] variableDatas) {
            for (int i = 0; i < count; i++) {
                float x = (float) (location.getX() + (Math.random() - Math.random()) * horizontalSpread.getValue(variableDatas));
                float y = (float) (location.getY() + (Math.random() - Math.random()) * verticalSpread.getValue(variableDatas));
                float z = (float) (location.getZ() + (Math.random() - Math.random()) * horizontalSpread.getValue(variableDatas));

                super.getParticlePacket(x, y, z, 0, (float) red.getValue(variableDatas), (float) green.getValue(variableDatas),
                        (float) blue.getValue(variableDatas), 0, 1).sendPacket(player);
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
         * {@link ColoredParticleInfo}의 빌더 클래스.
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {
            private final ParticleType particleType;
            private final VariableValue red;
            private final VariableValue green;
            private final VariableValue blue;
            private final VariableValue horizontalSpread = new VariableValue();
            private final VariableValue verticalSpread = new VariableValue();
            private int count = 1;

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
             * <p>가변 데이터: {@code double} 타입의 입자가 수평으로 퍼지는 범위의 규모.</p>
             *
             * @param dataIndex           가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
             * @param minHorizontalSpread 수평으로 퍼지는 최소 범위. (단위: 블록)
             * @param maxHorizontalSpread 수평으로 퍼지는 최대 범위. (단위: 블록)
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder horizontalSpread(int dataIndex, double minHorizontalSpread, double maxHorizontalSpread) {
                validateDataIndex(dataIndex);

                horizontalSpread.setValue(dataIndex, minHorizontalSpread, maxHorizontalSpread);
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
                return horizontalSpread(Integer.MAX_VALUE, horizontalSpread, horizontalSpread);
            }

            /**
             * 입자가 수직으로 퍼지는 범위를 설정한다.
             *
             * <p>가변 데이터: {@code double} 타입의 입자가 수직으로 퍼지는 범위의 규모.</p>
             *
             * @param dataIndex         가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
             * @param minVerticalSpread 수직으로 퍼지는 최소 범위. (단위: 블록)
             * @param maxVerticalSpread 수직으로 퍼지는 최대 범위. (단위: 블록)
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder verticalSpread(int dataIndex, double minVerticalSpread, double maxVerticalSpread) {
                validateDataIndex(dataIndex);

                verticalSpread.setValue(dataIndex, minVerticalSpread, maxVerticalSpread);
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
                return verticalSpread(Integer.MAX_VALUE, verticalSpread, verticalSpread);
            }

            /**
             * 색이 있는 입자 효과를 생성하여 반환한다.
             *
             * @return {@link ColoredParticleInfo}
             */
            @NonNull
            public ColoredParticleInfo build() {
                return new ColoredParticleInfo(this);
            }
        }
    }


    /**
     * 특정 방향으로 움직이는 입자 효과를 나타내는 클래스.
     */
    public static final class DirectionalParticleInfo extends ParticleInfo {
        /** 속도 */
        private final FixedValue<Vector> velocity;
        /** 속력 배수 */
        private final VariableValue speedMultiplier;

        private DirectionalParticleInfo(Builder builder) {
            super(builder.particle);

            this.velocity = builder.velocity;
            this.speedMultiplier = builder.speedMultiplier;
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * <p>가변 데이터: {@link Vector} 타입의 속도.</p>
         *
         * @param dataIndex 가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
         * @param particle  입자 종류
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public static Builder builder(int dataIndex, @NonNull Particle particle) {
            validateDataIndex(dataIndex);
            return new Builder(particle, new FixedValue<>(dataIndex, new Vector()));
        }

        /**
         * 빌더 인스턴스를 생성하여 반환한다.
         *
         * @param particle 입자 종류
         * @param velocity 속도
         * @return {@link Builder}
         */
        @NonNull
        public static Builder builder(@NonNull Particle particle, @NonNull Vector velocity) {
            return new Builder(particle, new FixedValue<>(velocity.clone()));
        }

        @Override
        void onPlay(@NonNull Location location, @NonNull Player player, Object @Nullable [] variableDatas) {
            Vector offset = velocity.getValue(variableDatas);

            super.getParticlePacket((float) location.getX(), (float) location.getY(), (float) location.getZ(), 0,
                    (float) offset.getX(), (float) offset.getY(), (float) offset.getZ(), 0,
                    (float) speedMultiplier.getValue(variableDatas)).sendPacket(player);
        }

        /**
         * {@link DirectionalParticleInfo}의 빌더 클래스.
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {
            private final Particle particle;
            private final FixedValue<Vector> velocity;
            private final VariableValue speedMultiplier = new VariableValue(1);

            /**
             * 입자의 속력 배수를 설정한다.
             *
             * <p>가변 데이터: {@code double} 타입의 속력 배수의 규모.</p>
             *
             * @param dataIndex          가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨. 0 이상의 값
             * @param minSpeedMultiplier 최소 속력 배수
             * @param maxSpeedMultiplier 최대 속력 배수
             * @return {@link Builder}
             */
            @NonNull
            public Builder speedMultiplier(int dataIndex, double minSpeedMultiplier, double maxSpeedMultiplier) {
                validateDataIndex(dataIndex);

                speedMultiplier.setValue(dataIndex, minSpeedMultiplier, maxSpeedMultiplier);
                return this;
            }

            /**
             * 입자의 속력 배수를 설정한다.
             *
             * @param speedMultiplier 속력 배수
             * @return {@link Builder}
             */
            @NonNull
            public Builder speedMultiplier(double speedMultiplier) {
                return speedMultiplier(Integer.MAX_VALUE, speedMultiplier, speedMultiplier);
            }

            /**
             * 움직이는 입자 효과를 생성하여 반환한다.
             *
             * @return {@link DirectionalParticleInfo}
             */
            @NonNull
            public DirectionalParticleInfo build() {
                return new DirectionalParticleInfo(this);
            }
        }
    }

    /**
     * 입자의 고정 값을 나타내는 클래스.
     */
    @AllArgsConstructor
    private static final class FixedValue<T> {
        /** 가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨 */
        private final int dataIndex;
        /** 기본값 */
        private final T value;

        private FixedValue(@NonNull T value) {
            this(Integer.MAX_VALUE, value);
        }

        /**
         * 최종 값을 반환한다.
         *
         * @param variableDatas 가변 데이터(입자 규모나 블록 데이터 등) 값 목록
         * @return {@link FixedValue#dataIndex}에 해당하는 가변 데이터 값이 존재하면 해당 값, 존재하지 않으면 {@link FixedValue#value} 반환
         * @throws IllegalArgumentException 지정한 가변 데이터의 값 타입이 일치하지 않으면 발생
         */
        @NonNull
        @SuppressWarnings("unchecked")
        private T getValue(Object @Nullable [] variableDatas) {
            if (variableDatas == null || variableDatas.length <= dataIndex || variableDatas[dataIndex] == null)
                return value;

            Validate.isInstanceOf(value.getClass(), variableDatas[dataIndex]);

            return (T) variableDatas[dataIndex];
        }
    }

    /**
     * 입자의 가변 값을 나타내는 클래스.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    private static final class VariableValue {
        /** 가변 데이터 인덱스. {@link ParticleEffect#play(Location, Object...)}에서 사용됨 */
        private int dataIndex = Integer.MAX_VALUE;
        /** 최솟값 */
        private double min = 0;
        /** 최댓값 */
        private double max = 0;

        private VariableValue(double value) {
            this(Integer.MAX_VALUE, value, value);
        }

        private void setValue(int dataIndex, double min, double max) {
            this.dataIndex = dataIndex;
            this.min = min;
            this.max = max;
        }

        /**
         * 최종 값을 반환한다.
         *
         * <p>가변 데이터를 규모 값으로 사용해 다음과 같이 계산한다.</p>
         *
         * <p>{@link VariableValue#min} + 가변 데이터 * ({@link VariableValue#max}-{@link VariableValue#min})</p>
         *
         * @param variableDatas 가변 데이터(입자 규모나 블록 데이터 등) 값 목록
         * @return {@link VariableValue#dataIndex}에 해당하는 가변 데이터 값이 존재하면 규모 값으로 계산, 존재하지 않으면 {@link VariableValue#max} 반환
         * @throws IllegalArgumentException 지정한 가변 데이터의 값 타입이 숫자가 아니면 발생
         */
        private double getValue(Object @Nullable [] variableDatas) {
            if (variableDatas == null || variableDatas.length <= dataIndex || min == max)
                return max;

            Validate.isInstanceOf(Number.class, variableDatas[dataIndex]);

            return min + ((Number) variableDatas[dataIndex]).doubleValue() * (max - min);
        }
    }
}
