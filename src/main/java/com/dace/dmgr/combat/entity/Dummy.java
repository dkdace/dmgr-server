package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.KnockbackModule;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 더미(훈련용 봇) 엔티티 클래스.
 */
@Getter
public final class Dummy extends TemporalEntity<Zombie> implements Damageable, Living, HasCritHitbox {
    /** 넉백 모듈 */
    @NonNull
    private final KnockbackModule knockbackModule;
    /** 피해 모듈 */
    @NonNull
    private final DamageModule damageModule;
    /** 치명타 히트박스 객체 */
    private final Hitbox critHitbox;

    /**
     * 더미 인스턴스를 생성한다.
     *
     * @param entity    대상 엔티티
     * @param maxHealth 최대 체력
     */
    public Dummy(Zombie entity, int maxHealth) {
        super(entity, "훈련용 봇", null,
                new FixedPitchHitbox(entity.getLocation(), 0.5, 0.75, 0.3, 0, 0, 0, 0, 0.375, 0),
                new FixedPitchHitbox(entity.getLocation(), 0.8, 0.75, 0.45, 0, 0, 0, 0, 1.125, 0),
                new Hitbox(entity.getLocation(), 0.45, 0.45, 0.45, 0, 0.225, 0, 0, 1.5, 0),
                new Hitbox(entity.getLocation(), 0.45, 0.1, 0.45, 0, 0.4, 0, 0, 1.5, 0)
        );
        knockbackModule = new KnockbackModule(this);
        damageModule = new DamageModule(this, true, maxHealth);
        critHitbox = hitboxes[3];

        onInit();
    }

    private void onInit() {
        entity.setBaby(false);
        entity.setSilent(true);
        entity.setAI(false);

        List<ItemStack> equipment = new ArrayList<>();
        equipment.add(new ItemBuilder(Material.LEATHER_CHESTPLATE).build());
        equipment.add(new ItemBuilder(Material.LEATHER_LEGGINGS).build());
        equipment.add(new ItemBuilder(Material.LEATHER_BOOTS).build());
        equipment.forEach((ItemStack itemStack) -> {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(Color.fromRGB(255, 255, 255));
            itemStack.setItemMeta(leatherArmorMeta);
        });

        entity.getEquipment().setHelmet(new ItemBuilder(Material.STAINED_GLASS).setDamage((short) 15).build());
        entity.getEquipment().setChestplate(equipment.get(0));
        entity.getEquipment().setLeggings(equipment.get(1));
        entity.getEquipment().setBoots(equipment.get(2));
        entity.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOW, 99999, 5, false, false));
    }

    @Override
    protected void onTick(long i) {
        // 미사용
    }

    @Override
    public void onDamage(Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        // 미사용
    }

    @Override
    public void onDeath(Attacker attacker) {
        dispose();
    }
}
