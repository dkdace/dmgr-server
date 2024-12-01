package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class QuakerA1Info extends ActiveSkillInfo<QuakerA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 20;
    /** 사망 시 쿨타임 (tick) */
    public static final long COOLDOWN_DEATH = 4 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 체력 */
    public static final int HEALTH = 5000;
    /** 체력 최대 회복 시간 (tick) */
    public static final int RECOVER_DURATION = 14 * 20;
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SLOW = 25;

    /** 방어 점수 */
    public static final int BLOCK_SCORE = 50;
    /** 파괴 점수 */
    public static final int DEATH_SCORE = 20;
    @Getter
    private static final QuakerA1Info instance = new QuakerA1Info();

    private QuakerA1Info() {
        super(QuakerA1.class, "불굴의 방패",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("커다란 <3::방패>를 펼쳐 전방의 공격을 방어합니다. " +
                                "사용 중에는 <:WALK_SPEED_DECREASE:이동 속도>가 느려집니다.")
                        .addValueInfo(TextIcon.WALK_SPEED_DECREASE, Format.PERCENT, USE_SLOW)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK)
                        .build(),
                        new ActionInfoLore.NamedSection("방패", ActionInfoLore.Section
                                .builder("공격을 막는 방벽입니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME + " (파괴 시)", COOLDOWN_DEATH / 20.0)
                                .addValueInfo(TextIcon.HEAL, HEALTH)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("해제", ActionKey.SLOT_1, ActionKey.RIGHT_CLICK)
                                .build()
                        )
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
                new DefinedSound.SoundEffect(Sound.ENTITY_ENDERDRAGON_FLAP, 1, 0.6),
                new DefinedSound.SoundEffect(Sound.BLOCK_SHULKER_BOX_OPEN, 1, 0.7)
        );
        /** 해제 */
        public static final DefinedSound DISABLE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.BLOCK_SHULKER_BOX_CLOSE, 1, 1.4));
        /** 피격 */
        public static final DefinedSound DAMAGE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.BLOCK_ANVIL_LAND, 0.25, 1.2, 0.1),
                new DefinedSound.SoundEffect("random.metalhit", 0.3, 0.85, 0.1)
        );
        /** 파괴 */
        public static final DefinedSound DEATH = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_IRONGOLEM_HURT, 2, 0.5),
                new DefinedSound.SoundEffect(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 0.7),
                new DefinedSound.SoundEffect("random.metalhit", 2, 0.7),
                new DefinedSound.SoundEffect(Sound.ITEM_SHIELD_BLOCK, 2, 0.5)
        );
    }
}
