package com.dace.dmgr.event.listener;

import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.silia.action.SiliaA3Info;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.user.User;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public final class OnPlayServerNamedSoundEffect extends PacketAdapter {
    public OnPlayServerNamedSoundEffect() {
        super(DMGR.getPlugin(), PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerNamedSoundEffect packet = new WrapperPlayServerNamedSoundEffect(event.getPacket());
        Player player = event.getPlayer();
        Location loc = new Location(player.getWorld(),
                packet.getEffectPositionX() / 8.0, packet.getEffectPositionY() / 8.0, packet.getEffectPositionZ() / 8.0);

        if (isMuted(player, packet.getSoundEffect(), SoundCategory.valueOf(packet.getSoundCategory().toString()), loc))
            event.setCancelled(true);
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
    private boolean isMuted(Player player, Sound sound, SoundCategory soundCategory, Location location) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
        if (combatUser == null)
            return false;

        if (combatUser.getStatusEffectModule().hasStatusEffect(StatusEffectType.SILENCE))
            return true;

        Player targetPlayer = (Player) location.getWorld().getNearbyEntities(location, 0.3, 0.3, 0.3).stream()
                .filter(Player.class::isInstance)
                .findFirst()
                .orElse(null);
        if (targetPlayer != null && targetPlayer != player) {
            CombatUser targetCombatUser = CombatUser.fromUser(User.fromPlayer(targetPlayer));
            return targetCombatUser != null && targetCombatUser.getCharacterType() == CharacterType.SILIA &&
                    !targetCombatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished() &&
                    sound.toString().contains("_STEP") && soundCategory == SoundCategory.PLAYERS;
        }

        return false;
    }
}
