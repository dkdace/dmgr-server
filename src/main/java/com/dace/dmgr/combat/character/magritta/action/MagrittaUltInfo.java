package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class MagrittaUltInfo extends UltimateSkillInfo<MagrittaUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 11000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SLOW = 40;
    /** 공격 쿨타임 (tick) */
    public static final long ATTACK_COOLDOWN = (long) (0.1 * 20);
    /** 지속시간 (tick) */
    public static final long DURATION = 3 * 20L;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    @Getter
    private static final MagrittaUltInfo instance = new MagrittaUltInfo();

    private MagrittaUltInfo() {
        super(MagrittaUlt.class, "초토화",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 기본 무기를 난사하여 강력한 <:DAMAGE:피해>를 입힙니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME_WITH_RPM, ATTACK_COOLDOWN / 20.0, 60 / ATTACK_COOLDOWN * 20)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build()
                )
        );
    }

    /**
     * 효과음 정보.
     */
    @UtilityClass
    public static final class SOUND {
        /** 사용 */
        public static final DefinedSound USE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_SHAKE, 1, 0.6),
                new DefinedSound.SoundEffect(Sound.ENTITY_WOLF_SHAKE, 1, 0.6),
                new DefinedSound.SoundEffect(Sound.BLOCK_LAVA_EXTINGUISH, 1, 0.6)
        );
        /** 사격 */
        public static final DefinedSound SHOOT = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.BLOCK_FIRE_EXTINGUISH, 2, 0.8, 0.1),
                new DefinedSound.SoundEffect("random.gun2.xm1014_1", 3, 1),
                new DefinedSound.SoundEffect("random.gun2.spas_12_1", 3, 1),
                new DefinedSound.SoundEffect("random.gun_reverb", 5, 0.9),
                new DefinedSound.SoundEffect("random.gun_reverb", 5, 0.8)
        );
        /** 사용 종료 */
        public static final DefinedSound END = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 0.8),
                new DefinedSound.SoundEffect(Sound.ENTITY_ITEM_BREAK, 2, 0.8),
                new DefinedSound.SoundEffect(Sound.ENTITY_GENERIC_EXPLODE, 2, 1.4)
        );
    }
}
