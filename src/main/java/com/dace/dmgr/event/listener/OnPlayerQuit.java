package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.effect.TimedSoundEffect;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.MessageFormat;
import java.util.function.LongConsumer;

public final class OnPlayerQuit implements Listener {
    /** 퇴장 효과음 */
    private static final TimedSoundEffect QUIT_SOUND = TimedSoundEffect.builder()
            .add(0, SoundEffect.SoundInfo.builder(Sound.BLOCK_NOTE_PLING).volume(1000).pitch(Math.pow(2, -4 / 12.0)).build())
            .add(3, SoundEffect.SoundInfo.builder(Sound.BLOCK_NOTE_PLING).volume(1000).pitch(Math.pow(2, -11 / 12.0)).build())
            .build();

    @EventHandler
    public static void event(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        user.dispose();

        event.setQuitMessage(StringFormUtil.REMOVE_PREFIX + player.getName());

        new DelayTask(() -> {
            DMGR.getPlugin().getServer().broadcastMessage(MessageFormat.format("{0}현재 인원수는 §3§l{1}명§b입니다.",
                    StringFormUtil.REMOVE_PREFIX, Bukkit.getOnlinePlayers().size()));

            new IntervalTask((LongConsumer) i ->
                    Bukkit.getOnlinePlayers().forEach(target -> QUIT_SOUND.play(i, target)), 1, 4);
        }, 1);
    }
}
