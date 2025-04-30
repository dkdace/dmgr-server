package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerAbilities;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.event.PacketEventListener;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;

public final class OnPlayServerAbilities extends PacketEventListener<WrapperPlayServerAbilities> {
    @Getter
    private static final OnPlayServerAbilities instance = new OnPlayServerAbilities();

    private OnPlayServerAbilities() {
        super(WrapperPlayServerAbilities.class);
    }

    @Override
    protected void onEvent(@NonNull PacketEvent event) {
        WrapperPlayServerAbilities packet = createPacketWrapper(event);
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));
        if (combatUser == null)
            return;

        packet.setWalkingSpeed((float) (combatUser.getMoveModule().getSpeedStatus().getValue() * 2 * combatUser.getFovValue()));
    }
}
