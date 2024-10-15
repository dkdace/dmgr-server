package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

public final class NeaceWeaponInfo extends WeaponInfo<NeaceWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 20;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 40;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.16;
    @Getter
    private static final NeaceWeaponInfo instance = new NeaceWeaponInfo();

    private NeaceWeaponInfo() {
        super(NeaceWeapon.class, RESOURCE.DEFAULT, "이중성",
                "",
                "§f▍ 적을 공격하거나 아군을 치유할 수 있는 완드입니다.",
                "",
                "§7§l[좌클릭] §f마법 구체 §7§l[우클릭] §f치유 광선",
                "",
                "§3[마법 구체]",
                "",
                "§f▍ §7마법 구체§f를 발사하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}초", TextIcon.ATTACK_SPEED, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                "",
                "§3[치유 광선]",
                "",
                "§f▍ 바라보는 아군에게 §7치유 광선§f을 고정하여",
                "§f▍ 지속적으로 §a" + TextIcon.HEAL + " 치유§f합니다.",
                "§f▍ §d구원의 표식§f이 있는 아군은 치유할 수 없습니다.",
                "",
                MessageFormat.format("§a{0}§f {1}/초", TextIcon.HEAL, HEAL.HEAL_PER_SECOND),
                MessageFormat.format("§a{0}§f {1}m", TextIcon.DISTANCE, HEAL.MAX_DISTANCE));
    }

    /**
     * 치유 광선의 정보.
     */
    @UtilityClass
    public static class HEAL {
        /** 초당 치유량 */
        public static final int HEAL_PER_SECOND = 250;
        /** 최대 거리 (단위: 블록) */
        public static final int MAX_DISTANCE = 15;
        /** 대상 위치 통과 불가 시 초기화 제한 시간 (tick) */
        public static final long BLOCK_RESET_DELAY = 2 * 20;
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 5;
    }
}
