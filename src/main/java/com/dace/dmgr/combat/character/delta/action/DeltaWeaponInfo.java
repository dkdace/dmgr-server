package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public class DeltaWeaponInfo extends WeaponInfo<DeltaWeapon> {
    /** 초당 데미지 */
    public static final int DAMAGE_PER_SECOND = 256;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 16;
    /** 대상 위치 통과 불가 시 초기화 제한 시간 (tick) */
    public static final long BLOCK_RESET_DELAY = 2 * 20;

    @Getter
    private static final DeltaWeaponInfo instance = new DeltaWeaponInfo();

    private DeltaWeaponInfo() {
        super(DeltaWeapon.class, RESOURCE.DEFAULT, "광자 투사기",
                "",
                "§f▍ 바라보는 적에게 광선을 고정하여 지속적으로 §c" + TextIcon.DAMAGE + " 피해를 입힙니다.",
                MessageFormat.format("§c{0}§f {1}/초", TextIcon.DAMAGE, DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                MessageFormat.format("§c{0}§f 0.05초 (1200/분)", TextIcon.ATTACK_SPEED),
                "",
                "§7§l[우클릭] §f사격");
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        public static final short DEFAULT = 20;
    }
}
