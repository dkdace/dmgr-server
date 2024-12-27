package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.effect.TimedSoundEffect;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
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

    @EventHandler
    public static void event(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        user.init();
        user.reset();

        event.setJoinMessage(StringFormUtil.ADD_PREFIX + player.getName());

        user.sendTitle("§bWelcome!", "§f메뉴를 사용하려면 §nF키§f를 누르십시오.", Timespan.ZERO, Timespan.ofSeconds(5), Timespan.ofSeconds(3));
        TaskUtil.addTask(user, new DelayTask(user::clearChat, 10));

        new DelayTask(() -> {
            DMGR.getPlugin().getServer().broadcastMessage(MessageFormat.format("{0}현재 인원수는 §3§l{1}명§b입니다.",
                    StringFormUtil.ADD_PREFIX, Bukkit.getOnlinePlayers().size()));

            new IntervalTask((LongConsumer) i ->
                    Bukkit.getOnlinePlayers().forEach(target -> JOIN_SOUND.play(i, target)), 1, 4);
        }, 1);
    }
}
