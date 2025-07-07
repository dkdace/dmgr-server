package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.StackableSkill;
import com.dace.dmgr.combat.ability.skill.module.MultiSummonModule;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class MetarA2 extends StackableSkill {
    /** 다중 엔티티 소환 모듈 */
    private final MultiSummonModule<MetarA2Entity> multiSummonModule;

    public MetarA2(@NonNull CombatUser combatUser, @NonNull MetarA2Info skillInfo) {
        super(combatUser, skillInfo, MetarA2Info.COOLDOWN, MetarA2Info.STACK_COOLDOWN, Timespan.MAX, MetarA2Info.MAX_STACK);
        this.multiSummonModule = new MultiSummonModule<>(this, MetarA2Info.MAX_STACK);
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_2;
    }

    @Override
    public void onSlot() {
        setCooldown();
        addStack(-1);

        MetarA2Info.Effects.USE.play(combatUser.getLocation());

        Location loc = combatUser.getLocation().add(0, 0.75, 0);
        multiSummonModule.add(new MetarA2Entity(loc));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 에너지 방벽 클래스.
     */
    private final class MetarA2Entity extends Barrier {
        private MetarA2Entity(@NonNull Location spawnLocation) {
            super(spawnLocation, combatUser.getName() + "의 방벽", combatUser, MetarA2Info.HEALTH, MetarA2Info.DEATH_SCORE,
                    Hitbox.builder(7, 4, 0.3).axisOffsetY(1.5).build());
            onInit();
        }

        private void onInit() {
            entity.setGravity(false);
            entity.setHelmet(new ItemBuilder(Material.IRON_HOE).setDamage((short) 2).build());
            entity.setHeadPose(new EulerAngle(Math.toRadians(owner.getLocation().getPitch()), 0, 0));

            Vector dir = getLocation().getDirection().multiply(0.15);

            addTask(new IntervalTask(i -> {
                if (LocationUtil.isNonSolid(getCenterLocation().add(dir)))
                    entity.teleport(getLocation().add(dir));
            }, 1, 10));
            addTask(new DelayTask(() -> onDeath(null), MetarA2Info.DURATION.toTicks()));
        }

        @Override
        public double getWidth() {
            return 7;
        }

        @Override
        public double getHeight() {
            return 3;
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
            super.onDamage(attacker, damage, reducedDamage, location, isCrit);

            combatUser.addScore(MetarA2Info.BLOCK_SCORE.multiplyScore(damage));

            MetarA2Info.Effects.DAMAGE.apply(location, damage).play(CombatEffectUtil.getHitLocation(this, location));
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();
            cancel();

            MetarA2Info.Effects.playDeath(getCenterLocation());
        }
    }
}
