package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.util.SoundEffect;
import com.dace.dmgr.util.TimedSoundEffect;
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
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("random.gun2.xm1014_1").volume(3).pitch(1).build(),
                SoundEffect.SoundInfo.builder("random.gun2.xm1014_1").volume(3).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder("random.gun2.spas_12_1").volume(3).pitch(1).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb").volume(5).pitch(0.9).build(),
                SoundEffect.SoundInfo.builder("random.gun_reverb").volume(5).pitch(0.8).build()
        );
        /** 재장전 */
        public static final TimedSoundEffect RELOAD = TimedSoundEffect.builder()
                .add(3, SoundEffect.SoundInfo.builder(Sound.BLOCK_PISTON_EXTEND).volume(0.6).pitch(1.3).build())
                .add(5, SoundEffect.SoundInfo.builder(Sound.ENTITY_VILLAGER_NO).volume(0.6).pitch(1.3).build())
                .add(20, SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.6).pitch(0.5).build())
                .add(21, SoundEffect.SoundInfo.builder(Sound.ITEM_FLINTANDSTEEL_USE).volume(0.6).pitch(0.8).build())
                .add(22, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.7).build())
                .add(28, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_HOWL).volume(0.6).pitch(0.9).build())
                .add(33, SoundEffect.SoundInfo.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.9).build())
                .build();
    }
}
