package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.character.jager.Jager;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 지정할 수 있는 전투원의 목록.
 *
 * @see Character
 */
@AllArgsConstructor
@Getter
public enum CharacterType {
    ARKACE(Arkace.getInstance()),
    JAGER(Jager.getInstance());

    /** 전투원 정보 */
    private final Character character;
}
