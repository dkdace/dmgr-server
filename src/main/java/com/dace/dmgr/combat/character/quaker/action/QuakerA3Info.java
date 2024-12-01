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

public final class QuakerA3Info extends ActiveSkillInfo<QuakerA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20L;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.8 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 1.2;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2;
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = (long) (0.8 * 20);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1.5;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 5;
    @Getter
    private static final QuakerA3Info instance = new QuakerA3Info();

    private QuakerA3Info() {
        super(QuakerA3.class, "돌풍 강타",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("검기를 날려 처음 맞은 적을 크게 <:KNOCKBACK:밀쳐내고> <:DAMAGE:피해>와 <:SNARE:속박>을 입힙니다. " +
                                "적이 날아가며 부딪힌 적에게도 같은 효과를 입힙니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.SNARE, Format.TIME, SNARE_DURATION / 20.0)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, DISTANCE)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_3)
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
                new DefinedSound.SoundEffect(Sound.BLOCK_LAVA_EXTINGUISH, 1, 0.8),
                new DefinedSound.SoundEffect("random.gun2.shovel_leftclick", 1, 0.5),
                new DefinedSound.SoundEffect("random.gun2.shovel_leftclick", 1, 0.8)
        );
        /** 사용 준비 */
        public static final DefinedSound USE_READY = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_GHAST_SHOOT, 2, 0.5),
                new DefinedSound.SoundEffect("new.item.trident.throw", 2, 0.7),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2, 0.7)
        );
        /** 틱 효과음 */
        public static final DefinedSound TICK = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_GHAST_SHOOT, 0.6, 0.5));
        /** 타격 */
        public static final DefinedSound HIT = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 2, 0.6),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 2, 0.7),
                new DefinedSound.SoundEffect(Sound.ENTITY_PLAYER_ATTACK_CRIT, 2, 0.7)
        );
    }
}
