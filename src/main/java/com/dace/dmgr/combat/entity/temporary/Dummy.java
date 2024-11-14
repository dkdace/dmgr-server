package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.entity.module.HealModule;
import com.dace.dmgr.combat.entity.module.KnockbackModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 더미(훈련용 봇) 엔티티 클래스.
 */
@Getter
public final class Dummy extends TemporaryEntity<Zombie> implements Healable, HasCritHitbox, CombatEntity {
    /** 넉백 모듈 */
    @NonNull
    private final KnockbackModule knockbackModule;
    /** 상태 효과 모듈 */
    @NonNull
    private final StatusEffectModule statusEffectModule;
    /** 피해 모듈 */
    @NonNull
    private final HealModule damageModule;
    /** 치명타 히트박스 객체 */
    private final Hitbox critHitbox;
    /** 팀 식별자 */
    private final String teamIdentifier;

    /**
     * 더미 인스턴스를 생성한다.
     *
     * @param entity         대상 엔티티
     * @param maxHealth      최대 체력
     * @param teamIdentifier 팀 식별자
     */
    public Dummy(@NonNull Zombie entity, int maxHealth, @NonNull String teamIdentifier) {
        super(entity, "훈련용 봇", null,
                new FixedPitchHitbox(entity.getLocation(), 0.5, 0.75, 0.3, 0, 0, 0, 0, 0.375, 0),
                new FixedPitchHitbox(entity.getLocation(), 0.8, 0.75, 0.45, 0, 0, 0, 0, 1.125, 0),
                new Hitbox(entity.getLocation(), 0.45, 0.35, 0.45, 0, 0.225, 0, 0, 1.5, 0),
                new Hitbox(entity.getLocation(), 0.45, 0.1, 0.45, 0, 0.4, 0, 0, 1.5, 0)
        );
        knockbackModule = new KnockbackModule(this);
        statusEffectModule = new StatusEffectModule(this);
        damageModule = new HealModule(this, true, true, true, 0, maxHealth);
        critHitbox = hitboxes[3];
        this.teamIdentifier = teamIdentifier;

        onInit();
    }

    /**
     * 더미 인스턴스를 생성한다.
     *
     * @param entity    대상 엔티티
     * @param maxHealth 최대 체력
     */
    public Dummy(@NonNull Zombie entity, int maxHealth) {
        this(entity, maxHealth, "Dummy");
    }

    private void onInit() {
        entity.setBaby(false);
        entity.setSilent(true);
        entity.setAI(false);

        List<ItemStack> equipment = new ArrayList<>();
        equipment.add(new ItemBuilder(Material.LEATHER_CHESTPLATE).build());
        equipment.add(new ItemBuilder(Material.LEATHER_LEGGINGS).build());
        equipment.add(new ItemBuilder(Material.LEATHER_BOOTS).build());
        equipment.forEach(itemStack -> {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(Color.fromRGB(255, 255, 255));
            itemStack.setItemMeta(leatherArmorMeta);
        });

        entity.getEquipment().setHelmet(new ItemBuilder(Material.STAINED_GLASS).setDamage((short) (teamIdentifier.equals("Dummy") ? 14 : 15)).build());
        entity.getEquipment().setChestplate(equipment.get(0));
        entity.getEquipment().setLeggings(equipment.get(1));
        entity.getEquipment().setBoots(equipment.get(2));
        entity.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5, false, false));
    }

    @Override
    protected void onTick(long i) {
        // 미사용
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location,
                         boolean isCrit, boolean isUlt) {
        CombatEffectUtil.playBleedingEffect(location, entity, damage);
    }

    @Override
    public void onTakeHeal(@Nullable Healer provider, int amount, boolean isUlt) {
        // 미사용
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        dispose();
    }
}
