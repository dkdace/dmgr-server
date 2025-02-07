package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.character.ched.Ched;
import com.dace.dmgr.combat.character.inferno.Inferno;
import com.dace.dmgr.combat.character.jager.Jager;
import com.dace.dmgr.combat.character.magritta.Magritta;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.character.palas.Palas;
import com.dace.dmgr.combat.character.quaker.Quaker;
import com.dace.dmgr.combat.character.silia.Silia;
import com.dace.dmgr.combat.character.vellion.Vellion;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.PlayerSkullUtil;
import com.dace.dmgr.item.gui.SelectCharInfo;
import com.dace.dmgr.item.gui.SelectCore;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 지정할 수 있는 전투원의 목록.
 *
 * @see Character
 */
@Getter
public enum CharacterType {
    MAGRITTA(Magritta.getInstance()),
    SILIA(Silia.getInstance()),

    ARKACE(Arkace.getInstance()),
    JAGER(Jager.getInstance()),
    CHED(Ched.getInstance()),

    INFERNO(Inferno.getInstance()),

    QUAKER(Quaker.getInstance()),

    NEACE(Neace.getInstance()),
    PALAS(Palas.getInstance()),

    VELLION(Vellion.getInstance());

    /** 전투원 정보 */
    @NonNull
    private final Character character;
    /** 전투원 프로필 정보 아이템 */
    @NonNull
    private final ItemStack profileItem;
    /** 전투원 선택 GUI 아이템 */
    @NonNull
    private final DefinedItem selectItem;

    CharacterType(Character character) {
        this.character = character;

        this.profileItem = new ItemBuilder(PlayerSkullUtil.fromCharacter(this))
                .setName(MessageFormat.format("§f{0} {1}{2} §8§o{3}",
                        character.getIcon(),
                        character.getRole().getColor(),
                        character.getName(),
                        character.getNickname()))
                .setLore("",
                        MessageFormat.format("§f▍ 역할군 : {0}", character.getRole().getColor() + character.getRole().getName() +
                                (character.getSubRole() == null
                                        ? ""
                                        : " §f/ " + character.getSubRole().getColor() + character.getSubRole().getName())),
                        "",
                        "§7§n좌클릭§f하여 전투원을 선택합니다.",
                        "§7§n우클릭§f하여 전투원 정보를 확인합니다.")
                .build();
        this.selectItem = new DefinedItem(profileItem,
                (clickType, player) -> {
                    User user = User.fromPlayer(player);
                    CombatUser combatUser = CombatUser.fromUser(user);
                    if (combatUser == null)
                        return false;

                    if (clickType == ClickType.LEFT) {
                        GameUser gameUser = combatUser.getGameUser();
                        boolean isDuplicated = gameUser != null && gameUser.getTeam().getTeamUsers().stream()
                                .anyMatch(targetGameUser -> {
                                    CombatUser targetCombatUser = CombatUser.fromUser(targetGameUser.getUser());
                                    Validate.notNull(targetCombatUser);

                                    return targetCombatUser.getCharacterType() == this;
                                });

                        if (isDuplicated)
                            return false;

                        combatUser.setCharacterType(this);
                        player.closeInventory();

                        if (!user.getUserData().getCharacterRecord(this).getCores().isEmpty())
                            TaskUtil.addTask(combatUser, new DelayTask(() -> new SelectCore(player), 10));
                    } else if (clickType == ClickType.RIGHT)
                        new SelectCharInfo(player, this);

                    return true;
                });
    }

    /**
     * 이름({@link Character#getName()}) 순으로 정렬된 전투원의 목록을 반환한다.
     *
     * @return 이름 순으로 정렬된 전투원 목록
     */
    @NonNull
    public static CharacterType @NonNull [] sortedValues() {
        CharacterType[] characterTypes = CharacterType.values();
        Arrays.sort(characterTypes, Comparator.comparing(characterType -> characterType.getCharacter().getName()));

        return characterTypes;
    }
}
