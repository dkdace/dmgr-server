package com.dace.dmgr.item.gui;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.TrainingCenter;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 훈련장 아레나 설정 GUI 클래스.
 */
public final class ArenaOption extends ChestGUI {
    /** 더미 생명력 */
    private int health = 1000;
    /** 더미 공격력 */
    private int damage = 100;
    /** 더미 이동속도 배수 */
    private double speedMultiplier = 1;
    /** 더미 생성 주기 (초) */
    private double spawnPeriodSeconds = 3;
    /** 최대 더미 수 */
    private int maxCount = 3;
    /** 진행 시간 (초) */
    private int durationSeconds = 30;
    /** 공격 방식 */
    private TrainingCenter.Arena.AttackMethod attackMethod = TrainingCenter.Arena.AttackMethod.MELEE;

    /**
     * 아레나 설정 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public ArenaOption(@NonNull Player player) {
        super(2, "§c§l아레나 훈련", player);

        fillAll(GUIItem.EMPTY);

        CombatUser combatUser = Validate.notNull(CombatUser.fromUser(User.fromPlayer(player)));
        boolean isUsing = TrainingCenter.Arena.getInstance().isUsing(combatUser);

        if (isUsing)
            set(0, 8, new DefinedItem(new ItemBuilder(Material.BARRIER)
                    .setName("§c§l종료")
                    .setLore("§f아레나 훈련을 종료합니다.")
                    .build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, target -> {
                        TrainingCenter.Arena.getInstance().finish();
                        target.closeInventory();

                        return true;
                    })));
        else
            set(0, 8, new DefinedItem(new ItemBuilder(Material.ARMOR_STAND)
                    .setName("§e§l시작")
                    .setLore("§f아레나 훈련을 시작합니다.")
                    .build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, target -> {
                        TrainingCenter.Arena.Option option = new TrainingCenter.Arena.Option(health, damage, speedMultiplier,
                                Timespan.ofSeconds(spawnPeriodSeconds), maxCount, Timespan.ofSeconds(durationSeconds), attackMethod);
                        if (!TrainingCenter.Arena.getInstance().start(combatUser, option))
                            return false;

                        target.closeInventory();
                        return true;
                    })));

        set(1, 8, GUIItem.EXIT);

        if (!isUsing)
            update();
    }

    /**
     * 설정을 업데이트한다.
     */
    private void update() {
        set(0, 0, ArenaOptionItem.HEALTH.create(this));
        set(0, 1, ArenaOptionItem.DAMAGE.create(this));
        set(0, 2, ArenaOptionItem.SPEED.create(this));
        set(0, 3, ArenaOptionItem.SPAWN_PERIOD.create(this));
        set(0, 4, ArenaOptionItem.MAX_COUNT.create(this));
        set(0, 5, ArenaOptionItem.DURATION.create(this));
        set(0, 6, new DefinedItem(new ItemBuilder(Material.ITEM_FRAME)
                .setName("§e§l공격 방식")
                .setLore("§f설정된 값 : §b" + attackMethod,
                        "",
                        "§7§n클릭§f하여 공격 방식을 변경합니다.")
                .build(),
                new DefinedItem.ClickHandler(ClickType.LEFT, target -> {
                    TrainingCenter.Arena.AttackMethod[] values = TrainingCenter.Arena.AttackMethod.values();
                    attackMethod = values[(attackMethod.ordinal() + 1) % values.length];

                    update();
                    return true;
                })));
    }

    /**
     * 아레나 설정 아이템.
     */
    @AllArgsConstructor
    private enum ArenaOptionItem {
        HEALTH(Material.IRON_CHESTPLATE, "생명력", "", 100, 100, 3000, gui -> gui.health,
                (gui, value) -> gui.health = value.intValue()),
        DAMAGE(Material.IRON_SWORD, "공격력", "", 50, 0, 1000, gui -> gui.damage,
                (gui, value) -> gui.damage = value.intValue()),
        SPEED(Material.FEATHER, "이동 속도", "", 0.02, 0, 2, gui -> gui.speedMultiplier,
                (gui, value) -> gui.speedMultiplier = value.doubleValue()),
        SPAWN_PERIOD(Material.DIODE, "생성 주기", "초", 0.5, 1, 5, gui -> gui.spawnPeriodSeconds,
                (gui, value) -> gui.spawnPeriodSeconds = value.doubleValue()),
        MAX_COUNT(Material.MOB_SPAWNER, "최대 더미 수", "", 1, 1, 5, gui -> gui.maxCount,
                (gui, value) -> gui.maxCount = value.intValue()),
        DURATION(Material.WATCH, "진행 시간", "초", 1, 10, 60, gui -> gui.durationSeconds,
                (gui, value) -> gui.durationSeconds = value.intValue());

        private final Material material;
        private final String name;
        private final String lorePatternSuffix;
        private final double increment;
        private final double min;
        private final double max;
        private final Function<ArenaOption, Number> onGetValue;
        private final BiConsumer<ArenaOption, Number> onSetValue;

        private void increaseValue(@NonNull ArenaOption gui, double increment) {
            onSetValue.accept(gui, Math.min(max, Math.max(min, onGetValue.apply(gui).doubleValue() + increment)));
            gui.update();
        }

        /**
         * 현재 GUI의 커스텀 더미 설정 아이템을 생성하여 반환한다.
         *
         * @param gui 현재 GUI
         * @return 커스텀 더미 설정 아이템
         */
        @NonNull
        private DefinedItem create(@NonNull ArenaOption gui) {
            return new DefinedItem(new ItemBuilder(material)
                    .setName("§e§l" + name)
                    .setLore("§f설정된 값 : §b{0}{1}",
                            "",
                            "§7§n좌클릭§f하여 값을 §b{2}{1}§f 감소시킵니다.",
                            "§7§n우클릭§f하여 값을 §b{2}{1}§f 증가시킵니다.",
                            "§7§nSHIFT§f를 누르고 클릭하여 §b{3}{1}§f 단위로 조정합니다.")
                    .formatLore(onGetValue.apply(gui), lorePatternSuffix, increment, increment * 10)
                    .build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                        increaseValue(gui, -increment);
                        return true;
                    }),
                    new DefinedItem.ClickHandler(ClickType.SHIFT_LEFT, player -> {
                        increaseValue(gui, -increment * 10);
                        return true;
                    }),
                    new DefinedItem.ClickHandler(ClickType.RIGHT, player -> {
                        increaseValue(gui, increment);
                        return true;
                    }),
                    new DefinedItem.ClickHandler(ClickType.SHIFT_RIGHT, player -> {
                        increaseValue(gui, increment * 10);
                        return true;
                    }));
        }
    }
}
