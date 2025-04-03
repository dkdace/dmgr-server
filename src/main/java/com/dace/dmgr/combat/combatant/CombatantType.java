package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.combatant.arkace.Arkace;
import com.dace.dmgr.combat.combatant.ched.Ched;
import com.dace.dmgr.combat.combatant.inferno.Inferno;
import com.dace.dmgr.combat.combatant.jager.Jager;
import com.dace.dmgr.combat.combatant.magritta.Magritta;
import com.dace.dmgr.combat.combatant.metar.Metar;
import com.dace.dmgr.combat.combatant.neace.Neace;
import com.dace.dmgr.combat.combatant.palas.Palas;
import com.dace.dmgr.combat.combatant.quaker.Quaker;
import com.dace.dmgr.combat.combatant.silia.Silia;
import com.dace.dmgr.combat.combatant.vellion.Vellion;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.PlayerSkullUtil;
import com.dace.dmgr.item.gui.SelectCharInfo;
import com.dace.dmgr.item.gui.SelectCore;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 지정할 수 있는 전투원의 목록.
 *
 * @see Combatant
 */
public enum CombatantType {
    MAGRITTA(Magritta.getInstance()),
    SILIA(Silia.getInstance()),

    ARKACE(Arkace.getInstance()),
    JAGER(Jager.getInstance()),
    CHED(Ched.getInstance()),

    INFERNO(Inferno.getInstance()),

    QUAKER(Quaker.getInstance()),
    METAR(Metar.getInstance()),

    NEACE(Neace.getInstance()),
    PALAS(Palas.getInstance()),

    VELLION(Vellion.getInstance());

    /** 전투원 정보 */
    @NonNull
    @Getter
    private final Combatant combatant;
    /** 전투원 프로필 정보 아이템 */
    private final ItemStack profileItem;
    /** 전투원 정보 GUI 아이템 */
    @NonNull
    @Getter
    private final DefinedItem infoItem;
    /** 전투원 선택 GUI 아이템 */
    @NonNull
    @Getter
    private final DefinedItem selectItem;

    CombatantType(Combatant combatant) {
        this.combatant = combatant;

        this.profileItem = new ItemBuilder(PlayerSkullUtil.fromCombatant(this))
                .setName(MessageFormat.format("§f{0} {1}{2} §8§o{3}",
                        combatant.getIcon(),
                        combatant.getRole().getColor(),
                        combatant.getName(),
                        combatant.getNickname()))
                .build();

        this.infoItem = new DefinedItem(new ItemBuilder(profileItem)
                .setLore("",
                        "§e✪ 난이도 §7:: §f{0}",
                        "§a{1} 생명력 §7:: §f{2}",
                        "§b{3} 이동속도 배수 §7:: §f{4}",
                        "§6⬜ 히트박스 배수 §7:: §f{5}")
                .formatLore(
                        StringFormUtil.getProgressBar(combatant.getDifficulty(), 5, ChatColor.YELLOW, 5, '✰')
                                .replace("§0", "§8"),
                        TextIcon.HEALTH,
                        combatant.getHealth(),
                        TextIcon.WALK_SPEED,
                        combatant.getSpeedMultiplier(),
                        combatant.getHitboxMultiplier())
                .build());
        this.selectItem = new DefinedItem(new ItemBuilder(profileItem)
                .setLore("",
                        MessageFormat.format("§f▍ 역할군 : {0}", combatant.getRole().getColor() + combatant.getRole().getName() +
                                (combatant.getSubRole() == null
                                        ? ""
                                        : " §f/ " + combatant.getSubRole().getColor() + combatant.getSubRole().getName())),
                        "",
                        "§7§n좌클릭§f하여 전투원을 선택합니다.",
                        "§7§n우클릭§f하여 전투원 정보를 확인합니다.")
                .build(),
                new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                    User user = User.fromPlayer(player);

                    GameUser gameUser = GameUser.fromUser(user);
                    if (gameUser != null && gameUser.getTeam().checkCombatantDuplication(this))
                        return false;

                    CombatUser combatUser = CombatUser.fromUser(user);
                    if (combatUser == null)
                        combatUser = new CombatUser(this, user);
                    else
                        combatUser.setCombatantType(this);

                    player.closeInventory();

                    if (!user.getUserData().getCombatantRecord(this).getCores().isEmpty())
                        combatUser.addTask(new DelayTask(() -> new SelectCore(player), 10));

                    return true;
                }),
                new DefinedItem.ClickHandler(ClickType.RIGHT, player -> {
                    new SelectCharInfo(player, this);
                    return true;
                }));
    }

    /**
     * 이름({@link Combatant#getName()}) 순으로 정렬된 전투원의 목록을 반환한다.
     *
     * @return 이름 순으로 정렬된 전투원 목록
     */
    @NonNull
    public static CombatantType @NonNull [] sortedValues() {
        CombatantType[] combatantTypes = CombatantType.values();
        Arrays.sort(combatantTypes, Comparator.comparing(combatantType -> combatantType.getCombatant().getName()));

        return combatantTypes;
    }

    /**
     * 전투원의 프로필 정보 아이템을 반환한다.
     *
     * @return 프로필 정보 아이템
     */
    @NonNull
    public ItemStack getProfileItem() {
        return profileItem.clone();
    }
}
