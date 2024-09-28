package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;

import java.text.MessageFormat;

public final class ChedWeaponInfo extends WeaponInfo<ChedWeapon> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.15 * 20);
    /** 최대 피해량 */
    public static final int MAX_DAMAGE = 500;
    /** 최대 투사체 속력 (단위: 블록/s) */
    public static final int MAX_VELOCITY = 110;

    /** 치명타 점수 */
    public static final int CRIT_SCORE = 6;
    @Getter
    private static final ChedWeaponInfo instance = new ChedWeaponInfo();

    private ChedWeaponInfo() {
        super(ChedWeapon.class, Material.BOW, RESOURCE.DEFAULT, "아폴론",
                "",
                "§f▍ 불의 힘이 깃든 체드의 주력 활입니다.",
                "§f▍ 화살을 걸고 §7발사§f하여 §c" + TextIcon.DAMAGE + " 피해§f를 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1} ~ {2}", TextIcon.DAMAGE, MAX_DAMAGE / 10, MAX_DAMAGE),
                MessageFormat.format("§c{0}§f {1}초", TextIcon.ATTACK_SPEED, COOLDOWN / 20.0),
                "",
                "§7§l[우클릭] §f충전 및 발사");
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 1;
        /** 불화살 */
        public static final short FIRE = 11;
    }
}
