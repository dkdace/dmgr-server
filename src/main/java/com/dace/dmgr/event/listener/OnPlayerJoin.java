package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TimedSoundEffect;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.MessageFormat;
import java.util.function.LongConsumer;

public final class OnPlayerJoin implements Listener {
    /** 입장 효과음 */
    private static final TimedSoundEffect JOIN_SOUND = TimedSoundEffect.builder()
            .add(0, SoundEffect.SoundInfo.builder(Sound.BLOCK_NOTE_PLING).volume(1000).pitch(Math.pow(2, -6 / 12.0)).build())
            .add(3, SoundEffect.SoundInfo.builder(Sound.BLOCK_NOTE_PLING).volume(1000).pitch(Math.pow(2, 1 / 12.0)).build())
            .build();
    /** 접속 전체 메시지 */
    private static final String BROADCAST_MESSAGE = "{0}현재 인원수는 §3§l{1}명§b입니다.";

    @EventHandler
    public static void event(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(StringFormUtil.ADD_PREFIX + player.getName());

        User.fromPlayer(player).onJoin();

        new DelayTask(() -> {
            DMGR.getPlugin().getServer().broadcastMessage(MessageFormat.format(BROADCAST_MESSAGE,
                    StringFormUtil.ADD_PREFIX,
                    Bukkit.getOnlinePlayers().size()));

            new IntervalTask((LongConsumer) i ->
                    Bukkit.getOnlinePlayers().forEach(target -> JOIN_SOUND.play(i, target)), 1, 4);
        }, 1);
    }
}
