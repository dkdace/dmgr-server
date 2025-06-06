package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.BulletBarrier;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

@Getter
public final class No7A2 extends ActiveSkill implements Summonable<No7A2.No7A2Entity> {
    /** 소환 엔티티 모듈 */
    @NonNull
    private final EntityModule<No7A2Entity> entityModule;

    public No7A2(@NonNull CombatUser combatUser) {
        super(combatUser, No7A2Info.getInstance(), No7A2Info.COOLDOWN, No7A2Info.DURATION, 1);
        this.entityModule = new EntityModule<>(this);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.LEFT_CLICK};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished())
            return null;

        return ActionBarStringUtil.getDurationBar(this) + ActionBarStringUtil.getKeyInfo(this, "해제");
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ActionManager actionManager = combatUser.getActionManager();
        return super.canUse(actionKey) && actionManager.getSkill(No7A1Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(No7A3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            cancel();
            return;
        }

        setDuration();

        combatUser.setGlobalCooldown(Timespan.ofTicks(1));
        combatUser.getActionManager().getWeapon().cancel();

        Location loc = combatUser.getLocation();
        entityModule.set(new No7A2Entity(loc));

        No7A2Info.Effects.ON.play(loc);

        addActionTask(new IntervalTask(i -> {
            No7A2Info.Effects.TICK_SOUND.play(combatUser.getLocation());
            playTickEffect(i, loc.getYaw());
        }, 1, No7A2Info.DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        entityModule.removeEntity();
        No7A2Info.Effects.OFF.play(combatUser.getLocation());
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i   인덱스
     * @param yaw 원본 Yaw 값
     */
    private void playTickEffect(long i, float yaw) {
        Location loc = combatUser.getLocation().add(0, 1, 0);
        loc.setYaw(yaw);
        loc.setPitch(0);
        Vector vector = VectorUtil.getPitchAxis(loc);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 4; j++) {
            long index = i * 3 + j;
            long yaw2 = index * 7;
            long pitch = -90;

            for (int k = 0; k < 4; k++) {
                yaw2 += 360 / 4;
                pitch += 180 / 4;
                Vector vec = VectorUtil.getRotatedVector(axis, VectorUtil.getRotatedVector(vector, axis, pitch), yaw2);
                Location loc2 = loc.clone().add(vec.clone().multiply(2.5));

                No7A2Info.Effects.TICK_PARTICLE.apply(vec).play(loc2);
            }
        }
    }

    /**
     * 능동방어 자기장 클래스.
     */
    public final class No7A2Entity extends BulletBarrier {
        private final HashSet<Class<?>> bulletClasses = new HashSet<>();

        private No7A2Entity(@NonNull Location spawnLocation) {
            super(spawnLocation, combatUser.getName() + "의 능동방어 자기장", combatUser,
                    Hitbox.builder(No7A2Info.SIZE, No7A2Info.SIZE, No7A2Info.SIZE).axisOffsetY(1).pitchFixed().build());
            onInit();
        }

        private void onInit() {
            entity.setGravity(false);
            addOnTick(i -> entity.teleport(owner.getLocation().add(0, 1, 0)));
        }

        @Override
        public double getWidth() {
            return No7A2Info.SIZE;
        }

        @Override
        public double getHeight() {
            return No7A2Info.SIZE;
        }

        @Override
        public void onRemove(@NonNull Bullet<?> bullet, @NonNull Location location) {
            No7A2Info.Effects.DAMAGE.play(location);

            if (bulletClasses.add(bullet.getClass()))
                combatUser.addScore("피해 흡수", No7A2Info.BLOCK_SCORE);
        }
    }
}
