package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public final class VellionWeaponInfo extends WeaponInfo<VellionWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 120;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 30;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.3;
    @Getter
    private static final VellionWeaponInfo instance = new VellionWeaponInfo();

    private VellionWeaponInfo() {
        super(VellionWeapon.class, RESOURCE.DEFAULT, "절멸",
                "",
                "§f▍ 마법 구체를 발사하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}초", TextIcon.ATTACK_SPEED, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE));
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 14;
    }
}
