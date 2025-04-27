package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;

public final class ChedWeaponInfo extends WeaponInfo<ChedWeapon> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.15);
    /** 최대 피해량 */
    public static final int MAX_DAMAGE = 500;
    /** 최대 투사체 속력 (단위: 블록/s) */
    public static final int MAX_VELOCITY = 110;
    /** 화살 저장 인벤토리 슬롯 */
    public static final int ARROW_INVENTORY_SLOT = 30;

    /** 치명타 점수 */
    public static final int CRIT_SCORE = 6;

    @Getter
    private static final ChedWeaponInfo instance = new ChedWeaponInfo();

    private ChedWeaponInfo() {
        super(ChedWeapon.class, Material.BOW, Resource.DEFAULT, "아폴론",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("불의 힘이 깃든 체드의 주력 활입니다. 화살을 걸고 발사하여 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, MAX_DAMAGE / 10, MAX_DAMAGE)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN.toSeconds())
                        .addActionKeyInfo("충전 및 발사", ActionKey.RIGHT_CLICK)
                        .build()));
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 1;
        /** 불화살 */
        public static final short FIRE = 11;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class Sounds {
        /** 충전 */
        public static final SoundEffect CHARGE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.item.crossbow.loading_middle").volume(0.6).pitch(1).build());
        /** 사용 */
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.item.crossbow.shoot").volume(0.3, 0.8).pitch(1.1, 1.25).build(),
                SoundEffect.SoundInfo.builder("random.gun.bow").volume(0.3, 0.8).pitch(0.7, 0.85).build(),
                SoundEffect.SoundInfo.builder("random.gun2.shovel_leftclick").volume(0.4, 0.9).pitch(0.75, 0.9).build());
        /** 타격 */
        public static final SoundEffect HIT = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.gun.arrowhit").volume(0.3, 0.8).pitch(1).build());
    }
}
