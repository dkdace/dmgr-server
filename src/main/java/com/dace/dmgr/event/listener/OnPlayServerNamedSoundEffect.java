package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.silia.action.SiliaA3Info;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.PacketEventListener;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class OnPlayServerNamedSoundEffect extends PacketEventListener<WrapperPlayServerNamedSoundEffect> {
    @Getter
    private static final OnPlayServerNamedSoundEffect instance = new OnPlayServerNamedSoundEffect();

    private OnPlayServerNamedSoundEffect() {
        super(WrapperPlayServerNamedSoundEffect.class);
    }

    /**
     * 특정 소리의 묵음 처리 여부를 반환한다.
     *
     * @param player        플레이어
     * @param sound         소리 종류
     * @param soundCategory 소리 카테고리
     * @param location      발생 위치
     * @return 묵음 처리 여부
     */
    private static boolean isMuted(@NonNull Player player, @NonNull Sound sound, @NonNull EnumWrappers.SoundCategory soundCategory,
                                   @NonNull Location location) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
        if (combatUser == null)
            return false;

        if (combatUser.getStatusEffectModule().hasRestriction(CombatRestriction.HEAR))
            return true;

        Player target = (Player) location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5).stream()
                .filter(targetPlayer -> targetPlayer instanceof Player && targetPlayer != player)
                .findFirst()
                .orElse(null);
        if (target != null) {
            CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(target));
            return targetCombatUser != null && targetCombatUser.getCharacterType() == CharacterType.SILIA
                    && !targetCombatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished()
                    && sound.toString().endsWith("_STEP") && soundCategory == EnumWrappers.SoundCategory.PLAYERS;
        }

        return false;
    }

    @Override
    protected void onEvent(@NonNull PacketEvent event) {
        WrapperPlayServerNamedSoundEffect packet = createPacketWrapper(event);
        Player player = event.getPlayer();
        Location loc = new Location(player.getWorld(),
                packet.getEffectPositionX() / 8.0, packet.getEffectPositionY() / 8.0, packet.getEffectPositionZ() / 8.0);

        if (isMuted(player, packet.getSoundEffect(), packet.getSoundCategory(), loc))
            event.setCancelled(true);
    }
}
