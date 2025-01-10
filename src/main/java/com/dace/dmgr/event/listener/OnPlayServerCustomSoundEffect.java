package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerCustomSoundEffect;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.PacketEventListener;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;

public final class OnPlayServerCustomSoundEffect extends PacketEventListener<WrapperPlayServerCustomSoundEffect> {
    @Getter
    private static final OnPlayServerCustomSoundEffect instance = new OnPlayServerCustomSoundEffect();

    private OnPlayServerCustomSoundEffect() {
        super(WrapperPlayServerCustomSoundEffect.class);
    }

    @Override
    protected void onEvent(@NonNull PacketEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));

        if (combatUser != null && combatUser.getStatusEffectModule().hasAnyRestriction(CombatRestrictions.HEAR))
            event.setCancelled(true);
    }
}
