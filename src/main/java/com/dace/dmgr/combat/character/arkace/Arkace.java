package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.Bullet;
import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.combat.SkillController;
import com.dace.dmgr.combat.WeaponController;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.HasCSWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.SoundPlayer;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Arkace extends Character implements HasCSWeapon {
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super("아케이스", new ArkaceStats(), "DVArkace");
    }

    public static Arkace getInstance() {
        return instance;
    }

    @Override
    public void useWeaponShoot(CombatUser combatUser) {
        SoundPlayer.play("random.gun2.scarlight_1", combatUser.getEntity().getLocation(), 3F, 1F);
        SoundPlayer.play("random.gun_reverb", combatUser.getEntity().getLocation(), 5F, 1.2F);
        new Bullet(combatUser, 7) {
            @Override
            public void trail(Location location) {
                Location trailLoc = location.add(VectorUtil.getPitchAxis(location).multiply(-0.2)).add(0, -0.2, 0);
                location.getWorld().spawnParticle(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
            }

            @Override
            public void onHitEntity(Location location, ICombatEntity target) {
                Combat.attack(combatUser, target, ArkaceStats.Normal.DAMAGE, "", false, false);
            }
        }.shoot();
    }

    @Override
    public void useWeaponLeft(CombatUser combatUser, WeaponController weaponController) {
        combatUser.getEntity().sendMessage("left click");
    }

    @Override
    public void useWeaponRight(CombatUser combatUser, WeaponController weaponController) {
        combatUser.getEntity().sendMessage("right click");
    }

    @Override
    protected void usePassive1(CombatUser combatUser, SkillController skillController) {
        if (!skillController.isUsing()) {
            skillController.runDuration();
            combatUser.addSpeedIncrement(ArkaceStats.Passive1.SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability((short) (getCharacterStats().getWeapon().getItemStack().getDurability() + 1000));
        } else {
            skillController.runCooldown();
            combatUser.addSpeedIncrement(-ArkaceStats.Passive1.SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability(getCharacterStats().getWeapon().getItemStack().getDurability());
        }
    }

    protected void useActive2(CombatUser combatUser, SkillController skillController) {
        combatUser.getEntity().sendMessage("skill 2");
        if (skillController.isCooldownFinished())
            skillController.runCooldown();
        else
            combatUser.getEntity().sendMessage("스킬 쿨타임");
    }

    protected void useActive3(CombatUser combatUser, SkillController skillController) {
        combatUser.getEntity().sendMessage("skill 3");
        if (skillController.isCooldownFinished())
            skillController.runDuration(ArkaceStats.Active3.DURATION);
        else
            combatUser.getEntity().sendMessage("스킬 쿨타임");
    }

    protected void useUltimate(CombatUser combatUser, SkillController skillController) {
        combatUser.getEntity().sendMessage("ultimate");
    }
}
