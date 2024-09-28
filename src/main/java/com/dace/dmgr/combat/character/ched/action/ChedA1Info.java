package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class ChedA1Info extends ActiveSkillInfo<ChedA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = (long) (0.3 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 스택 충전 쿨타임 (tick) */
    public static final long STACK_COOLDOWN = 6 * 20;
    /** 최대 스택 충전량 */
    public static final int MAX_STACK = 3;
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 70;
    /** 화염 지속 시간 (tick) */
    public static final long FIRE_DURATION = 3 * 20;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 95;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 4;
    @Getter
    private static final ChedA1Info instance = new ChedA1Info();

    private ChedA1Info() {
        super(ChedA1.class, "불화살",
                "",
                "§f▍ 충전 없이 §3불화살§f을 속사할 수 있습니다.",
                "",
                MessageFormat.format("§f{0} {1}초 / {2}회 충전", TextIcon.COOLDOWN, STACK_COOLDOWN / 20.0, MAX_STACK),
                "",
                "§7§l[1] §f사용",
                "",
                "§3[불화살]",
                "",
                "§f▍ 불화살을 §7발사§f하여 §c" + TextIcon.DAMAGE + " 피해§f와 §c" + TextIcon.FIRE + " 화염 피해§f를",
                "§f▍ 입힙니다.",
                "",
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}초 / {2}/초", TextIcon.FIRE, FIRE_DURATION / 20.0, FIRE_DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}초", TextIcon.ATTACK_SPEED, COOLDOWN / 20.0),
                "",
                "§7§l[우클릭] §f발사",
                "",
                "§3[전탄 사용/재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                "§7§l[1] §f해제");
    }
}
