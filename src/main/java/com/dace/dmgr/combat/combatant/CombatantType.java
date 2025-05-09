package com.dace.dmgr.combat.combatant;

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
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 지정할 수 있는 전투원의 목록.
 *
 * @see Combatant
 */
@AllArgsConstructor
@Getter
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
    private final Combatant combatant;

    /**
     * 모든 전투원의 스킨을 불러온다.
     *
     * <p>플러그인 활성화 시 호출해야 한다.</p>
     */
    @NonNull
    public static AsyncTask<Void> loadSkins() {
        return AsyncTask.all(Arrays.stream(values())
                .map(combatantType -> combatantType.getCombatant().getPlayerSkin().init())
                .collect(Collectors.toList()));
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
        return new ItemBuilder(combatant.getPlayerSkin().get())
                .setName(MessageFormat.format("§f{0} {1}{2} §8§o{3}",
                        combatant.getIcon(),
                        combatant.getRole().getColor(),
                        combatant.getName(),
                        combatant.getNickname()))
                .build();
    }
}
