package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.*;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.GunCharacter;
import com.dace.dmgr.combat.character.ICharacter;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Arkace extends Character implements ICharacter, GunCharacter {
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super("아케이스", Weapon.ARKACE, new ArkaceStats(), "DVArkace");
    }

    public static Arkace getInstance() {
        return instance;
    }

    @Override
    public void useWeaponShoot(CombatUser combatUser) {
        new Bullet(combatUser, "평") {
            @Override
            public void trail(Location location) {
                location.getWorld().spawnParticle(Particle.CRIT, location, 1, 0, 0, 0, 0);
            }

            @Override
            public void onHitBlock(Location location) {

            }

            @Override
            public void onHitEntity(Location location, ICombatEntity target) {
//                Combat.attack(combatUser, target, ArkaceStats.Normal.DAMAGE, "", false, false);
            }
        }.shoot(combatUser.getEntity().getEyeLocation(), combatUser.getEntity().getLocation().getDirection().multiply(0.25));
    }

    @Override
    public void useWeaponLeft(CombatUser combatUser) {

    }

    @Override
    public void useWeaponRight(CombatUser combatUser) {

    }

    @Override
    public void useSkill1(CombatUser combatUser) {

    }

    @Override
    public void useSkill2(CombatUser combatUser) {

    }

    @Override
    public void useSkill3(CombatUser combatUser) {

    }

    @Override
    public void useSkill4(CombatUser combatUser) {

    }

}
