package com.dace.dmgr.system.command;

import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Dummy;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

/**
 * 디버그 및 테스트용 명령어 클래스.
 *
 * <p>Usage:</p>
 *
 * <p>/test dummy 체력 - 훈련용 봇 소환</p>
 */
public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "dummy": {
                new Dummy(player.getLocation(), Integer.parseInt(args[1]));
                break;
            }
        }

        return true;
    }
}
