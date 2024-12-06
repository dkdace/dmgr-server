package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class ChedA1Info extends ActiveSkillInfo<ChedA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.3 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 스택 충전 쿨타임 (tick) */
    public static final long STACK_COOLDOWN = 6 * 20L;
    /** 최대 스택 충전량 */
    public static final int MAX_STACK = 3;
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 70;
    /** 화염 지속 시간 (tick) */
    public static final long FIRE_DURATION = 3 * 20L;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 95;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 4;
    @Getter
    private static final ChedA1Info instance = new ChedA1Info();

    private ChedA1Info() {
        super(ChedA1.class, "불화살",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("충전 없이 <3::불화살>을 속사할 수 있습니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME_WITH_MAX_STACK, STACK_COOLDOWN / 20.0, MAX_STACK)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build(),
                        new ActionInfoLore.NamedSection("불화살", ActionInfoLore.Section
                                .builder("불화살을 발사하여 <:DAMAGE:피해>와 <:FIRE:화염 피해>를 입힙니다.")
                                .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                                .addValueInfo(TextIcon.FIRE, Format.TIME_WITH_PER_SECOND, FIRE_DURATION / 20.0, FIRE_DAMAGE_PER_SECOND)
                                .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN / 20.0)
                                .addActionKeyInfo("발사", ActionKey.RIGHT_CLICK)
                                .build()
                        ),
                        new ActionInfoLore.NamedSection("불화살: 전탄 사용/재사용 시", ActionInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addActionKeyInfo("해제", ActionKey.SLOT_1)
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
        public static final SoundEffect USE = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.item.crossbow.loading_end").volume(0.7).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_CAT_PURREOW).volume(0.7).pitch(2).build()
        );
        /** 사격 */
        public static final SoundEffect SHOOT = new SoundEffect(
                SoundEffect.SoundInfo.builder("new.item.crossbow.shoot").volume(1.4).pitch(1.6).build(),
                SoundEffect.SoundInfo.builder("random.gun.bow").volume(1.4).pitch(1.2).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_GHAST_SHOOT).volume(1.6).pitch(1.4).build()
        );
    }
}
