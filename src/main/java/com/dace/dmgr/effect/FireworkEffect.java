package com.dace.dmgr.effect;

import com.dace.dmgr.util.task.DelayTask;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * 폭죽 효과 생성 기능을 제공하는 클래스.
 */
public final class FireworkEffect {
    /** 폭죽 효과 정보 인스턴스 */
    private final org.bukkit.FireworkEffect mcFireworkEffect;

    private FireworkEffect(@NonNull Builder builder) {
        this.mcFireworkEffect = builder.mcBuilder.build();
    }

    /**
     * 빌더 인스턴스를 생성하여 반환한다.
     *
     * @param type  폭죽 효과 타입
     * @param red   빨강. 0~255 사이의 값
     * @param green 초록. 0~255 사이의 값
     * @param blue  파랑. 0~255 사이의 값
     * @return {@link Builder}
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public static Builder builder(@NonNull org.bukkit.FireworkEffect.Type type, int red, int green, int blue) {
        Validate.inclusiveBetween(0, 255, red);
        Validate.inclusiveBetween(0, 255, green);
        Validate.inclusiveBetween(0, 255, blue);

        return new Builder(org.bukkit.FireworkEffect.builder().with(type).withColor(Color.fromRGB(red, green, blue)));
    }

    /**
     * 지정한 위치에 폭죽 효과를 생성한다.
     *
     * @param location 위치
     */
    public void play(@NonNull Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);

        firework.setSilent(true);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(mcFireworkEffect);
        firework.setFireworkMeta(fireworkMeta);

        new DelayTask(firework::detonate, 1);
    }

    /**
     * {@link FireworkEffect}의 빌더 클래스.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        private final org.bukkit.FireworkEffect.Builder mcBuilder;

        /**
         * 폭죽의 사라지는 색을 설정한다.
         *
         * @param red   빨강. 0~255 사이의 값
         * @param green 초록. 0~255 사이의 값
         * @param blue  파랑. 0~255 사이의 값
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public Builder fadeColor(int red, int green, int blue) {
            Validate.inclusiveBetween(0, 255, red);
            Validate.inclusiveBetween(0, 255, green);
            Validate.inclusiveBetween(0, 255, blue);

            mcBuilder.withFade(Color.fromRGB(red, green, blue));

            return this;
        }

        /**
         * 폭죽에 반짝임 효과를 추가한다.
         *
         * @return {@link Builder}
         */
        @NonNull
        public Builder flicker() {
            mcBuilder.withFlicker();
            return this;
        }

        /**
         * 폭죽에 잔상 효과를 추가한다.
         *
         * @return {@link Builder}
         */
        @NonNull
        public Builder trail() {
            mcBuilder.withTrail();
            return this;
        }

        /**
         * 폭죽 효과를 생성하여 반환한다.
         *
         * @return {@link FireworkEffect}
         */
        @NonNull
        public FireworkEffect build() {
            return new FireworkEffect(this);
        }
    }
}