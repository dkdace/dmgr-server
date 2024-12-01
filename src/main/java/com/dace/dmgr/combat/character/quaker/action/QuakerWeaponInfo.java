package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

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
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("거대한 도끼를 휘둘러 근거리에 <:DAMAGE:광역 피해>를 입히고 옆으로 <:KNOCKBACK:밀쳐냅니다>.")
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.LEFT_CLICK)
                        .build()
                )
        );
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static class RESOURCE {
        /** 기본 */
        public static final short DEFAULT = 3;
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final DefinedSound USE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_IRONGOLEM_ATTACK, 1, 0.5),
                new DefinedSound.SoundEffect("random.gun2.shovel_leftclick", 1, 0.6, 0.1)
        );
        /** 타격 */
        public static final DefinedSound HIT = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8, 0.75, 0.1),
                new DefinedSound.SoundEffect(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6, 0.85, 0.1)
        );
        /** 엔티티 타격 */
        public static final DefinedSound HIT_ENTITY = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 0.9, 0.05),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1.2, 0.1)
        );
    }
}
