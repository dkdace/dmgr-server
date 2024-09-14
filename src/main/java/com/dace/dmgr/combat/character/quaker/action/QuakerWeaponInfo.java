package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public final class QuakerWeaponInfo extends WeaponInfo<QuakerWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (1.1 * 20);
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 320;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3.5;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 0.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.3;
    @Getter
    private static final QuakerWeaponInfo instance = new QuakerWeaponInfo();

    private QuakerWeaponInfo() {
        super(QuakerWeapon.class, RESOURCE.DEFAULT, "타바르진",
                "",
                "§f▍ 거대한 도끼를 휘둘러 근거리에 §c" + TextIcon.DAMAGE + " 광역 피해§f를",
                "§f▍ 입히고 옆으로 §5" + TextIcon.KNOCKBACK + " 밀쳐냅니다§f.",
                "",
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}초", TextIcon.ATTACK_SPEED, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                "",
                "§7§l[좌클릭] §f사용");
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 3;
    }
}
