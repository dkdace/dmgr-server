package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.util.DelayedDefinedSound;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

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
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("근거리에 강력한 피해를 입히는 산탄총입니다. " +
                                "사격하여 <:DAMAGE:피해>를 입힙니다. " +
                                "산탄이 " + PELLET_AMOUNT / 2 + "발 이상 적중하면 적에게 <d::파쇄>를 적용합니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE + " (×{4})",
                                DAMAGE, DAMAGE / 2, DISTANCE / 2, DISTANCE, PELLET_AMOUNT)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("사격", ActionKey.LEFT_CLICK)
                        .build()
                )
        );
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

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final DefinedSound USE = new DefinedSound(
                new DefinedSound.SoundEffect("random.gun2.xm1014_1", 3, 1),
                new DefinedSound.SoundEffect("random.gun2.xm1014_1", 3, 0.8),
                new DefinedSound.SoundEffect("random.gun2.spas_12_1", 3, 1),
                new DefinedSound.SoundEffect("random.gun_reverb", 5, 0.9),
                new DefinedSound.SoundEffect("random.gun_reverb", 5, 0.8)
        );
        /** 재장전 */
        public static final DelayedDefinedSound RELOAD = DelayedDefinedSound.builder()
                .add(3, new DefinedSound.SoundEffect(Sound.BLOCK_PISTON_EXTEND, 0.6, 1.3))
                .add(5, new DefinedSound.SoundEffect(Sound.ENTITY_VILLAGER_NO, 0.6, 1.3))
                .add(20, new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_HURT, 0.6, 0.5))
                .add(21, new DefinedSound.SoundEffect(Sound.ITEM_FLINTANDSTEEL_USE, 0.6, 0.8))
                .add(22, new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_SHAKE, 0.6, 0.7))
                .add(28, new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_HOWL, 0.6, 0.9))
                .add(33, new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_SHAKE, 0.6, 0.9))
                .build();
    }
}
