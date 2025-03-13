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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * 밟았을 때 특정 기능이 실행되는 블록(힐 팩, 궁극기 팩, 점프대 등)을 나타내는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class FunctionalBlock {
    /**
     * 지정한 위치에 기능 블록이 존재하면 해당 기능 블록을 사용한다.
     *
     * @param location   대상 위치
     * @param combatUser 사용한 플레이어
     */
    public static void use(@NonNull Location location, @NonNull CombatUser combatUser) {
        Block block = location.getBlock();
        Material material = block.getType();

        FunctionalBlock functionalBlock;
        if (material == JumpPad.MATERIAL)
            functionalBlock = JumpPad.instance;
        else if (material == FallZone.MATERIAL)
            functionalBlock = FallZone.instance;
        else
            functionalBlock = CooldownBlock.fromLocation(block.getLocation(), material);

        if (functionalBlock != null && functionalBlock.canUse(combatUser))
            functionalBlock.onUse(combatUser);
    }

    /**
     * 기능 블록을 사용할 수 있는지 확인한다.
     *
     * @param combatUser 사용한 플레이어
     * @return 사용 가능 여부
     */
    abstract boolean canUse(@NonNull CombatUser combatUser);

    /**
     * 기능 블록을 사용했을 때 실행할 작업.
     *
     * @param combatUser 사용한 플레이어
     */
    abstract void onUse(@NonNull CombatUser combatUser);

    /**
     * 점프대 블록 클래스.
     */
    @NoArgsConstructor
    private static final class JumpPad extends FunctionalBlock {
        private static final JumpPad instance = new JumpPad();

        /** 블록 타입 */
        private static final Material MATERIAL = GeneralConfig.getCombatConfig().getJumpPadBlock();
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SMALL_FALL).volume(1.5).pitch(1.5).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(0.8).pitchVariance(0.05).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(1.4).pitchVariance(0.05).build());

        @Override
        boolean canUse(@NonNull CombatUser combatUser) {
            return combatUser.getJumpPadCooldownTimestamp().isBefore(Timestamp.now());
        }

        @Override
        void onUse(@NonNull CombatUser combatUser) {
            combatUser.setJumpPadCooldownTimestamp(Timestamp.now().plus(Timespan.ofTicks(10)));

            combatUser.getMoveModule().push(new Vector(0, GeneralConfig.getCombatConfig().getJumpPadVelocity(), 0), true);
            USE_SOUND.play(combatUser.getLocation());
        }
    }

    /**
     * 낙사 구역 블록 클래스.
     */
    @NoArgsConstructor
    private static final class FallZone extends FunctionalBlock {
        private static final FallZone instance = new FallZone();
        /** 블록 타입 */
        private static final Material MATERIAL = GeneralConfig.getCombatConfig().getFallZoneBlock();

        @Override
        boolean canUse(@NonNull CombatUser combatUser) {
            return true;
        }

        @Override
        void onUse(@NonNull CombatUser combatUser) {
            combatUser.setFallZoneTimestamp(Timestamp.now().plus(Timespan.ofTicks(10)));
            combatUser.onDeath(null);
        }
    }

    /**
     * 쿨타임이 있는 기능 블록을 나타내는 클래스.
     */
    @AllArgsConstructor
    private abstract static class CooldownBlock extends FunctionalBlock {
        /** 위치별 쿨타임 기능 블록 목록 (위치 : 쿨타임 기능 블록) */
        private static final HashMap<Location, CooldownBlock> BLOCK_MAP = new HashMap<>();
        /** 위치 */
        private final Location location;
        /** 쿨타임 */
        private final Timespan cooldown;

        @Nullable
        private static CooldownBlock fromLocation(@NonNull Location location, @NonNull Material material) {
            CooldownBlock functionalBlock = BLOCK_MAP.get(location);
            if (functionalBlock != null)
                return functionalBlock;

            if (material == HealPack.MATERIAL)
                return new HealPack(location);
            else if (material == UltPack.MATERIAL)
                return new UltPack(location);

            return null;
        }

        @Override
        @MustBeInvokedByOverriders
        boolean canUse(@NonNull CombatUser combatUser) {
            return BLOCK_MAP.get(location) == null;
        }

        @MustBeInvokedByOverriders
        void onUse(@NonNull CombatUser combatUser) {
            BLOCK_MAP.put(location, this);

            Game game = combatUser.getGame();
            Location hologramLoc = location.clone().add(0.5, 1.7, 0.5);
            TextHologram hologram = new TextHologram(hologramLoc, player -> LocationUtil.canPass(player.getEyeLocation(), hologramLoc));

            long durationTicks = cooldown.divide(5).toTicks();

            new IntervalTask(i -> {
                hologram.setContent(MessageFormat.format("§f§l[ §6{0} {1} §f§l]",
                        TextIcon.COOLDOWN,
                        Math.ceil(Timespan.ofTicks((durationTicks - i) * 5).toSeconds())));

                return game == null || !game.isDisposed();
            }, isCancelled -> {
                hologram.dispose();
                BLOCK_MAP.remove(location);
            }, 5, durationTicks);
        }
    }

    /**
     * 힐 팩 블록 클래스.
     */
    private static final class HealPack extends CooldownBlock {
        /** 블록 타입 */
        private static final Material MATERIAL = GeneralConfig.getCombatConfig().getHealPackBlock();
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.5).pitch(1.2).build());

        private HealPack(@NonNull Location location) {
            super(location, GeneralConfig.getCombatConfig().getHealPackCooldown());
        }

        @Override
        boolean canUse(@NonNull CombatUser combatUser) {
            return super.canUse(combatUser) && !combatUser.getDamageModule().isFullHealth();
        }

        @Override
        void onUse(@NonNull CombatUser combatUser) {
            super.onUse(combatUser);

            combatUser.getDamageModule().heal(combatUser, GeneralConfig.getCombatConfig().getHealPackHeal(), false);
            combatUser.getCombatantType().getCombatant().onUseHealPack(combatUser);

            USE_SOUND.play(combatUser.getLocation());
        }
    }

    /**
     * 궁극기 팩 블록 클래스.
     */
    private static final class UltPack extends CooldownBlock {
        /** 블록 타입 */
        private static final Material MATERIAL = GeneralConfig.getCombatConfig().getUltPackBlock();
        /** 사용 입자 효과 */
        private static final FireworkEffect USE_PARTICLE = FireworkEffect.builder(org.bukkit.FireworkEffect.Type.BALL, 48, 85, 251)
                .fadeColor(255, 255, 255).build();
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_BREWING_STAND_BREW).volume(1).pitch(2).build(),
                SoundEffect.SoundInfo.builder(Sound.ITEM_ARMOR_EQUIP_GOLD).volume(1).pitch(1.3).build());

        private UltPack(@NonNull Location location) {
            super(location, GeneralConfig.getCombatConfig().getUltPackCooldown());
        }

        @Override
        boolean canUse(@NonNull CombatUser combatUser) {
            return super.canUse(combatUser) && (combatUser.getGame() == null || combatUser.getGame().isUltPackActivated())
                    && combatUser.getUltGaugePercent() < 1;
        }

        @Override
        void onUse(@NonNull CombatUser combatUser) {
            super.onUse(combatUser);

            combatUser.addUltGauge(GeneralConfig.getCombatConfig().getUltPackCharge());

            USE_PARTICLE.play(super.location.clone().add(0.5, 1.1, 0.5));
            USE_SOUND.play(combatUser.getLocation());
        }
    }
}
