package com.dace.dmgr.combat.ability.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.bukkit.ChatColor;

/**
 * 패시브 스킬의 상태를 관리하는 클래스.
 */
public abstract class PassiveSkill extends AbstractSkill {
    /**
     * 패시브 스킬 인스턴스를 생성한다.
     *
     * @param combatUser       사용자 플레이어
     * @param passiveSkillInfo 패시브 스킬 정보 인스턴스
     * @param defaultCooldown  기본 쿨타임
     * @param defaultDuration  기본 지속시간
     */
    protected PassiveSkill(@NonNull CombatUser combatUser, @NonNull PassiveSkillInfo<?> passiveSkillInfo, @NonNull Timespan defaultCooldown,
                           @NonNull Timespan defaultDuration) {
        super(combatUser, passiveSkillInfo, defaultCooldown, defaultDuration);
    }

    @Override
    @NonNull
    protected ChatColor getDisplayNameColor() {
        return ChatColor.YELLOW;
    }
}
