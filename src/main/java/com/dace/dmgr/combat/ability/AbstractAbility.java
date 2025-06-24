package com.dace.dmgr.combat.ability;

import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * {@link Ability}의 기본 구현체, 모든 능력(무기, 스킬, 특성)의 기반 클래스.
 *
 * @see AbstractAction
 * @see Trait
 */
public abstract class AbstractAbility implements Ability {
    /** 사용자 플레이어 */
    @NonNull
    @Getter
    protected final CombatUser combatUser;
    /** 이름 */
    private final String name;

    /**
     * 능력 인스턴스를 생성한다.
     *
     * @param combatUser 사용자 플레이어
     * @param name       이름
     */
    protected AbstractAbility(@NonNull CombatUser combatUser, @NonNull String name) {
        this.combatUser = combatUser;
        this.name = name;
    }

    @Override
    @NonNull
    public final String getDisplayName() {
        return MessageFormat.format("{0}［{1}］", getDisplayNameColor(), name);
    }

    /**
     * 능력 표시 이름에 사용될 색상을 반환한다.
     *
     * @return 능력 표시 이름 색상
     */
    @NonNull
    protected abstract ChatColor getDisplayNameColor();
}
