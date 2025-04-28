package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayClientUseEntity;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.event.PacketEventListener;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class OnPlayClientUseEntity extends PacketEventListener<WrapperPlayClientUseEntity> {
    @Getter
    private static final OnPlayClientUseEntity instance = new OnPlayClientUseEntity();

    private OnPlayClientUseEntity() {
        super(WrapperPlayClientUseEntity.class);
    }

    @Override
    protected void onEvent(@NonNull PacketEvent event) {
        WrapperPlayClientUseEntity packet = createPacketWrapper(event);
        Player player = event.getPlayer();
        Entity target = packet.getTarget(event);

        if (!player.isOp() && target instanceof Hanging)
            event.setCancelled(true);

        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
        if (combatUser == null)
            return;

        if (packet.getType() == EnumWrappers.EntityUseAction.ATTACK)
            new BukkitRunnable() {
                @Override
                public void run() {
                    combatUser.getActionManager().useAction(ActionKey.LEFT_CLICK);
                }
            }.runTask(DMGR.getPlugin());
    }
}
