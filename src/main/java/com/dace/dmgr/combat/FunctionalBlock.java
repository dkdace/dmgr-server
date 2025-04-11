package com.dace.dmgr.combat;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.effect.FireworkEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.HashMap;

/**
 * 밟았을 때 특정 기능이 실행되는 블록(힐 팩, 궁극기 팩, 점프대 등)을 나타내는 클래스.
 */
public abstract class FunctionalBlock {
    /** 블록 타입별 기능 블록 목록 (블록 타입 : 기능 블록) */
    private static final EnumMap<Material, FunctionalBlock> FUNCTIONAL_BLOCK_MAP = new EnumMap<>(Material.class);

    static {
        new JumpPad();
        new FallZone();

        new HealPack();
        new UltPack();
    }

    /**
     * 기능 블록 인스턴스를 생성하고 등록한다.
     *
     * @param material 블록 타입
     */
    protected FunctionalBlock(@NonNull Material material) {
        FUNCTIONAL_BLOCK_MAP.put(material, this);
    }

    /**
     * 지정한 위치에 기능 블록이 존재하면 해당 기능 블록을 사용한다.
     *
     * @param location   대상 위치
     * @param combatUser 사용한 플레이어
     */
    public static void use(@NonNull Location location, @NonNull CombatUser combatUser) {
        Block block = location.getBlock();
        location = block.getLocation();

        FunctionalBlock functionalBlock = FUNCTIONAL_BLOCK_MAP.get(block.getType());

        if (functionalBlock != null && functionalBlock.canUse(combatUser, location))
            functionalBlock.onUse(combatUser, location);
    }

    /**
     * 기능 블록을 사용할 수 있는지 확인한다.
     *
     * @param combatUser 사용한 플레이어
     * @param location   블록 위치
     * @return 사용 가능 여부
     */
    protected abstract boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location);

    /**
     * 기능 블록을 사용했을 때 실행할 작업.
     *
     * @param combatUser 사용한 플레이어
     * @param location   블록 위치
     */
    protected abstract void onUse(@NonNull CombatUser combatUser, @NonNull Location location);

    /**
     * 점프대 블록 클래스.
     */
    private static final class JumpPad extends FunctionalBlock {
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SMALL_FALL).volume(1.5).pitch(1.5).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(0.8).pitchVariance(0.05).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(1.4).pitchVariance(0.05).build());

        private JumpPad() {
            super(GeneralConfig.getCombatConfig().getJumpPadBlock());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return combatUser.getJumpPadCooldownTimestamp().isBefore(Timestamp.now());
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            combatUser.setJumpPadCooldownTimestamp(Timestamp.now().plus(Timespan.ofTicks(10)));

            combatUser.getMoveModule().push(new Vector(0, GeneralConfig.getCombatConfig().getJumpPadVelocity(), 0), true);
            USE_SOUND.play(location);
        }
    }

    /**
     * 낙사 구역 블록 클래스.
     */
    private static final class FallZone extends FunctionalBlock {
        private FallZone() {
            super(GeneralConfig.getCombatConfig().getFallZoneBlock());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return true;
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            combatUser.setFallZoneTimestamp(Timestamp.now().plus(Timespan.ofTicks(10)));
            combatUser.onDeath(null);
        }
    }

    /**
     * 쿨타임이 있는 기능 블록을 나타내는 클래스.
     */
    public abstract static class CooldownBlock extends FunctionalBlock {
        /** 위치별 쿨타임 기능 블록 목록 (위치 : 쿨타임 기능 블록) */
        private static final HashMap<Location, Timestamp> BLOCK_MAP = new HashMap<>();
        /** 쿨타임 */
        private final Timespan cooldown;

        /**
         * 쿨타임 기능 블록 인스턴스를 생성하고 등록한다.
         *
         * @param material 블록 타입
         * @param cooldown 쿨타임
         */
        protected CooldownBlock(@NonNull Material material, @NonNull Timespan cooldown) {
            super(material);
            this.cooldown = cooldown;
        }

        @Override
        @MustBeInvokedByOverriders
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return !BLOCK_MAP.containsKey(location);
        }

        @MustBeInvokedByOverriders
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            BLOCK_MAP.put(location, Timestamp.now().plus(cooldown));

            Game game = combatUser.getGame();
            Location hologramLoc = location.clone().add(0.5, 1.7, 0.5);
            TextHologram hologram = new TextHologram(hologramLoc, player -> LocationUtil.canPass(player.getEyeLocation(), hologramLoc));

            long durationTicks = cooldown.divide(5).toTicks();

            new IntervalTask(i -> {
                hologram.setContent(MessageFormat.format("§f§l[ §6{0} {1} §f§l]",
                        TextIcon.COOLDOWN,
                        Math.ceil(Timespan.ofTicks((durationTicks - i) * 5).toSeconds())));

                return game == null || !game.isFinished();
            }, isCancelled -> {
                hologram.remove();
                BLOCK_MAP.remove(location);
            }, 5, durationTicks);
        }
    }

    /**
     * 힐 팩 블록 클래스.
     */
    private static final class HealPack extends CooldownBlock {
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.5).pitch(1.2).build());

        private HealPack() {
            super(GeneralConfig.getCombatConfig().getHealPackBlock(), GeneralConfig.getCombatConfig().getHealPackCooldown());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return super.canUse(combatUser, location) && !combatUser.getDamageModule().isFullHealth();
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            super.onUse(combatUser, location);

            combatUser.getDamageModule().heal(combatUser, GeneralConfig.getCombatConfig().getHealPackHeal(), false);
            combatUser.getCombatantType().getCombatant().onUseHealPack(combatUser);

            USE_SOUND.play(location);
        }
    }

    /**
     * 궁극기 팩 블록 클래스.
     */
    private static final class UltPack extends CooldownBlock {
        /** 사용 입자 효과 */
        private static final FireworkEffect USE_PARTICLE = FireworkEffect.builder(org.bukkit.FireworkEffect.Type.BALL,
                Color.fromRGB(48, 85, 251)).fadeColor(Color.fromRGB(255, 255, 255)).build();
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_BREWING_STAND_BREW).volume(1).pitch(2).build(),
                SoundEffect.SoundInfo.builder(Sound.ITEM_ARMOR_EQUIP_GOLD).volume(1).pitch(1.3).build());

        private UltPack() {
            super(GeneralConfig.getCombatConfig().getUltPackBlock(), GeneralConfig.getCombatConfig().getUltPackCooldown());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return super.canUse(combatUser, location) && (combatUser.getGame() == null || combatUser.getGame().isUltPackActivated())
                    && combatUser.getUltGaugePercent() < 1;
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            super.onUse(combatUser, location);

            combatUser.addUltGauge(GeneralConfig.getCombatConfig().getUltPackCharge());

            USE_PARTICLE.play(location.clone().add(0.5, 1.1, 0.5));
            USE_SOUND.play(location);
        }
    }
}
