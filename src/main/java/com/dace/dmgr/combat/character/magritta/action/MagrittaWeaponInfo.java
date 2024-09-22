package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public final class MagrittaWeaponInfo extends WeaponInfo<MagrittaWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 40;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 16;
    /** 산탄 수 */
    public static final int PELLET_AMOUNT = 8;
    /** 탄퍼짐 */
    public static final double SPREAD = 18;
    /** 장탄수 */
    public static final int CAPACITY = 8;
    /** 재장전 시간 (tick) */
    public static final long RELOAD_DURATION = (long) (1.8 * 20);
    @Getter
    private static final MagrittaWeaponInfo instance = new MagrittaWeaponInfo();

    private MagrittaWeaponInfo() {
        super(MagrittaWeapon.class, RESOURCE.DEFAULT, "데스페라도",
                "",
                "§f▍ 근거리에 강력한 피해를 입히는 산탄총입니다.",
                "§f▍ §7사격§f하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "§f▍ 산탄이 4발 이상 적중하면 적에게 §d파쇄§f를",
                "§f▍ 적용합니다.",
                "",
                MessageFormat.format("§c{0}§f {1} ~ {2} ({3}m~{4}m) (×{5})", TextIcon.DAMAGE, DAMAGE, DAMAGE / 2, DISTANCE / 2, DISTANCE, PELLET_AMOUNT),
                MessageFormat.format("§c{0}§f {1}초", TextIcon.ATTACK_SPEED, COOLDOWN / 20.0),
                MessageFormat.format("§f{0} {1}발", TextIcon.CAPACITY, CAPACITY),
                "",
                "§7§l[좌클릭] §f사격");
    }

    /**
     * 반동 정보.
     */
    @UtilityClass
    public static class RECOIL {
        /** 수직 반동 */
        public static final double UP = 9.0;
        /** 수평 반동 */
        public static final double SIDE = 0;
        /** 수직 반동 분산도 */
        public static final double UP_SPREAD = 1.0;
        /** 수평 반동 분산도 */
        public static final double SIDE_SPREAD = 3.2;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 13;
    }
}
