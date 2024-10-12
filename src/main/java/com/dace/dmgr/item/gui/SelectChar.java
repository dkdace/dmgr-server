package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * 전투원 선택 GUI 클래스.
 */
public final class SelectChar extends Gui {
    @Getter
    private static final SelectChar instance = new SelectChar();

    private SelectChar() {
        super(6, "§c§l전투원 선택");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        User user = User.fromPlayer(player);

        guiController.fillColumn(2, DisplayItem.EMPTY.getStaticItem());
        guiController.set(0, SelectCharInfoItem.SCUFFLER.staticItem);
        guiController.set(9, SelectCharInfoItem.MARKSMAN.staticItem);
        guiController.set(18, SelectCharInfoItem.VANGUARD.staticItem);
        guiController.set(27, SelectCharInfoItem.GUARDIAN.staticItem);
        guiController.set(36, SelectCharInfoItem.SUPPORT.staticItem);
        guiController.set(45, SelectCharInfoItem.CONTROLLER.staticItem);

        TaskUtil.addTask(user, new IntervalTask(i -> {
            if (!player.getOpenInventory().getTitle().equals("§c§l전투원 선택"))
                return false;

            CombatUser combatUser = CombatUser.fromUser(user);
            if (combatUser == null)
                return true;

            GameUser gameUser = combatUser.getGameUser();

            if (i % 10 == 0) {
                int[] indexes = {2, 11, 20, 29, 38, 47};

                for (CharacterType characterType : CharacterType.values()) {
                    int index = indexes[characterType.getCharacter().getRole().ordinal()]++;
                    boolean isDuplicated = gameUser != null && gameUser.getTeam() != null &&
                            Arrays.stream(gameUser.getTeam().getTeamUsers()).anyMatch(targetGameUser -> {
                                CombatUser targetCombatUser = CombatUser.fromUser(targetGameUser.getUser());
                                Validate.notNull(targetCombatUser);

                                return targetCombatUser.getCharacterType() == characterType;
                            });

                    guiController.set(index, CharacterType.valueOf(characterType.toString()).getGuiItem(), itemBuilder -> {
                        if (isDuplicated)
                            itemBuilder.addLore("", "§c§l팀원이 이미 선택했습니다.");
                    });
                }
            }

            return true;
        }, 1));
    }

    private enum SelectCharInfoItem {
        SCUFFLER(Material.IRON_SWORD, 4, Role.SCUFFLER),
        MARKSMAN(Material.BOW, 0, Role.MARKSMAN),
        VANGUARD(Material.IRON_CHESTPLATE, 0, Role.VANGUARD),
        GUARDIAN(Material.SHIELD, 0, Role.GUARDIAN),
        SUPPORT(Material.END_CRYSTAL, 0, Role.SUPPORT),
        CONTROLLER(Material.EYE_OF_ENDER, 0, Role.CONTROLLER);

        /** 정적 아이템 객체 */
        private final StaticItem staticItem;

        SelectCharInfoItem(Material material, int damage, Role role) {
            this.staticItem = new StaticItem("SelectCharInfo" + this, new ItemBuilder(material)
                    .setDamage((short) damage)
                    .setName(MessageFormat.format("{0}§l{1} §7§o{2}", role.getColor(), role.getName(),
                            StringUtils.capitalize(toString().toLowerCase())))
                    .setLore(role.getDescription())
                    .build());
        }
    }
}
