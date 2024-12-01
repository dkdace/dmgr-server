package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.util.DefinedSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;

public final class NeaceA1Info extends ActiveSkillInfo<NeaceA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20L;
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 250;
    /** 최대 치유량 */
    public static final int MAX_HEAL = 1000;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 15 * 20L;
    @Getter
    private static final NeaceA1Info instance = new NeaceA1Info();

    private NeaceA1Info() {
        super(NeaceA1.class, "구원의 표식",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("바라보는 아군에게 표식을 남겨 일정 시간동안 <:HEAL:치유>합니다. " +
                                "이미 표식이 있는 아군에게 사용할 수 없으며, 치유량이 최대치에 도달하거나 지속 시간이 지나면 사라집니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN / 20.0)
                        .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                        .addValueInfo(TextIcon.HEAL, Format.PER_SECOND + " / 최대 {1}", HEAL_PER_SECOND, MAX_HEAL)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
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
                new DefinedSound.SoundEffect(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 2, 1.6),
                new DefinedSound.SoundEffect("new.block.respawn_anchor.charge", 2, 1.4),
                new DefinedSound.SoundEffect("new.block.note_block.chime", 2, 1.6),
                new DefinedSound.SoundEffect("new.block.note_block.chime", 2, 1.2)
        );
    }
}
