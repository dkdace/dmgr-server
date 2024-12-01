package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class ChedUltInfo extends UltimateSkillInfo<ChedUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 10000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (1.5 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 피해량 */
    public static final int DAMAGE = 1500;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 7;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 200;
    /** 화염 지대 지속 시간 (tick) */
    public static final long FIRE_FLOOR_DURATION = 8 * 20L;
    /** 화염 지대 범위 (단위: 블록) */
    public static final double FIRE_FLOOR_RADIUS = 7;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    /** 궁극기 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 2 * 20L;
    @Getter
    private static final ChedUltInfo instance = new ChedUltInfo();

    private ChedUltInfo() {
        super(ChedUlt.class, "피닉스 스트라이크",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("벽을 관통하는 불사조를 날려보내 적과 부딪히면 크게 폭발하여 <:DAMAGE:광역 피해>를 입히고 <3::화염 지대>를 만듭니다. " +
                                "플레이어가 아닌 적은 통과합니다.")
                        .addValueInfo(TextIcon.ULTIMATE, COST)
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE, DAMAGE, DAMAGE / 2)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, SIZE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_4)
                        .build(),
                        new ActionInfoLore.NamedSection("화염 지대", ActionInfoLore.Section
                                .builder("지속적인 <:FIRE:화염 피해>를 입히는 지역입니다.")
                                .addValueInfo(TextIcon.DURATION, Format.TIME, FIRE_FLOOR_DURATION / 20.0)
                                .addValueInfo(TextIcon.FIRE, Format.PER_SECOND, FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, FIRE_FLOOR_RADIUS)
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
                new DefinedSound.SoundEffect(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 2, 1.4),
                new DefinedSound.SoundEffect(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR, 2, 0.8),
                new DefinedSound.SoundEffect("new.entity.squid.squirt", 2, 0.7)
        );
        /** 사용 준비 */
        public static final DefinedSound USE_READY = new DefinedSound(
                new DefinedSound.SoundEffect("new.entity.phantom.death", 3, 0.7),
                new DefinedSound.SoundEffect("new.entity.phantom.death", 3, 0.7),
                new DefinedSound.SoundEffect(Sound.ENTITY_WITHER_SHOOT, 3, 0.5),
                new DefinedSound.SoundEffect(Sound.ENTITY_VEX_CHARGE, 3, 0.85)
        );
        /** 틱 효과음 */
        public static final DefinedSound TICK = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ENTITY_ENDERDRAGON_FLAP, 1.5, 1.2));
        /** 폭발 */
        public static final DefinedSound EXPLODE = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.ITEM_TOTEM_USE, 5, 1.3),
                new DefinedSound.SoundEffect(Sound.ENTITY_GENERIC_EXPLODE, 5, 0.7),
                new DefinedSound.SoundEffect(Sound.ENTITY_GHAST_SHOOT, 5, 0.6),
                new DefinedSound.SoundEffect(Sound.ENTITY_GHAST_SHOOT, 5, 0.8),
                new DefinedSound.SoundEffect("random.explosion_reverb", 7, 0.6)
        );
        /** 화염 지대 틱 효과음 */
        public static final DefinedSound FIRE_FLOOR_TICK = new DefinedSound(
                new DefinedSound.SoundEffect(Sound.BLOCK_FIRE_AMBIENT, 2, 0.75, 0.1));
    }
}
