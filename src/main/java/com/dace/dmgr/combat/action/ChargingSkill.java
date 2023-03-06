package com.dace.dmgr.combat.action;

import lombok.Getter;
import lombok.Setter;

public abstract class ChargingSkill extends ActiveSkill implements Toggleable {
    @Getter @Setter
    private int state;

    public ChargingSkill(int number, String name, String... lore) {
        super(number, name, lore);
    }

    /**
     * 상태 변수의 최댓값을 가져오는 메소드입니다. 오버라이딩하여 상태 변수의 최댓값을 설정합니다.
     *
     * 기본값은 정수 최댓값입니다.
     * @return 상태 변수의 최댓값
     */
    public int getStateMax() {
        return Integer.MAX_VALUE;
    };

    /**
     * 상태 변수값을 증감시킵니다.
     * @param delta 증감값
     * @return 증감 후 상태 변수값
     */
    public int updateState(int delta) {
        state += delta;
        if (state > getStateMax())
            state = getStateMax();
        if (state < 0)
            state = 0;
        return state;
    }
}
