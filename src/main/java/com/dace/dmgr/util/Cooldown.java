package com.dace.dmgr.util;

import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.config.GeneralConfig;

public enum Cooldown {
    CHAT(GeneralConfig.chatCooldown),
    COMMAND(GeneralConfig.commandCooldown),
    DAMAGE_ANIMATION(6),
    DAMAGE_SUM_TIME_LIMIT(Combat.DAMAGE_SUM_TIME_LIMIT),
    FASTKILL_TIME_LIMIT(Combat.FASTKILL_TIME_LIMIT),
    RESPAWN_TIME(Combat.RESPAWN_TIME),
    SKILL_COOLDOWN(0),
    SKILL_DURATION(0);

    private final long defaultValue;

    Cooldown(long defaultValue) {
        this.defaultValue = defaultValue;
    }

    public long getDefaultValue() {
        return defaultValue;
    }
}
