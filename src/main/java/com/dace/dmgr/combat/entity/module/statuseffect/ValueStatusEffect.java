package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.combatant.delta.DeltaT1;
import com.dace.dmgr.combat.combatant.jager.JagerT1;
import com.dace.dmgr.combat.combatant.magritta.MagrittaT1;
import com.dace.dmgr.combat.combatant.neace.NeaceA1;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import lombok.Getter;
import lombok.NonNull;

import java.util.function.Supplier;

/**
 * 상태 변수를 가지고 있는 상태 효과를 처리하는 클래스.
 */
public abstract class ValueStatusEffect extends StatusEffect {
    /** 상태 변수 최댓값 */
    protected final double maxValue;
    /** 상태 변수 */
    @Getter
    private double value = 0;

    /**
     * 상태 변수를 가지고 있는 상태 효과 인스턴스를 생성한다.
     *
     * @param statusEffectType 상태 효과의 유형
     * @param isPositive       이로운 효과 여부
     * @param maxValue         상태 변수 최댓값
     */
    protected ValueStatusEffect(@NonNull StatusEffectType statusEffectType, boolean isPositive, double maxValue) {
        super(statusEffectType, isPositive);
        this.maxValue = maxValue;
    }

    /**
     * 수치 값을 설정한다.
     *
     * @param value 값
     */
    public final void setValue(double value) {
        this.value = Math.min(value, maxValue);
    }

    /**
     * 상태 변수의 종류 목록.
     *
     * <p>상태 변수 종류에 해당하는 상태 효과 인스턴스는 {@link StatusEffectModule#getValueStatusEffect(Type)}를 이용하여 가져올 수 있다.</p>
     */
    public static final class Type<T extends ValueStatusEffect> {
        /** 치유 표식 */
        public static final Type<NeaceA1.NeaceA1Mark> HEALING_MARK;
        /** 빙결 */
        public static final Type<JagerT1.FreezeValue> FREEZE;
        /** 파쇄 */
        public static final Type<MagrittaT1.ShreddingValue> SHREDDING;
        /** 글리치 */
        public static final Type<DeltaT1.GlitchValue> GLITCH;
        /** 상태 변수 종류 목록 */
        private static final Type<?>[] values = new Type[3];

        static {
            HEALING_MARK = new Type<>(0, NeaceA1.NeaceA1Mark::new);
            FREEZE = new Type<>(1, JagerT1.FreezeValue::new);
            SHREDDING = new Type<>(2, MagrittaT1.ShreddingValue::new);
            GLITCH = new Type<>(3, DeltaT1.GlitchValue::new);
        }

        /** 상태 효과 반환에 실행할 작업 */
        private final Supplier<T> onGetStatusEffect;

        private Type(int id, @NonNull Supplier<T> onGetStatusEffect) {
            Type.values[id] = this;
            this.onGetStatusEffect = onGetStatusEffect;
        }

        /**
         * @return 상태 변수를 가진 상태 효과 목록
         */
        @NonNull
        public static Type<?> @NonNull [] values() {
            return values.clone();
        }

        /**
         * 상태 변수를 가진 상태 효과 인스턴스를 생성하여 반환한다.
         *
         * @return 상태 변수를 가진 상태 효과
         */
        @NonNull
        public T createStatusEffect() {
            return onGetStatusEffect.get();
        }
    }
}
