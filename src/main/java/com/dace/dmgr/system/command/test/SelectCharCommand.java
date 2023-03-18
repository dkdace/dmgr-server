package com.dace.dmgr.system.command.test;

import com.dace.dmgr.combat.CombatTick;
import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.HashMapList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * 전투원 선택 명령어 클래스.
 *
 * <p>Usage: /선택 플레이어 팀 전투원</p>
 */
public class SelectCharCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        String team = args[1];
        String character = args[2];

        CombatUser combatUser = HashMapList.combatUserMap.getOrDefault(player, new CombatUser(player));

        combatUser.setTeam(team);
        switch (character.toLowerCase()) {
            case "아케이스":
            case "arkace":
                combatUser.setCharacter(Arkace.getInstance());
                break;
        }
        if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING))
            CombatTick.run(combatUser);

        return true;
    }
}
