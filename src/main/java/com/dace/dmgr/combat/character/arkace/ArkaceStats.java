package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.SkillTrigger;
import com.dace.dmgr.combat.Weapon;
import com.dace.dmgr.combat.character.ICharacterStats;
import com.dace.dmgr.combat.character.ISkill;
import com.dace.dmgr.combat.character.Skill;
import com.dace.dmgr.gui.ItemBuilder;

public class ArkaceStats implements ICharacterStats {
    private static final int HEALTH = 1000;
    private static final float SPEED = 1.0F;
    private static final float HITBOX = 1.0F;

    @Override
    public int getHealth() {
        return HEALTH;
    }

    @Override
    public float getSpeed() {
        return SPEED;
    }

    @Override
    public float getHitbox() {
        return HITBOX;
    }

    @Override
    public Weapon getWeapon() {
        return new Normal();
    }

    @Override
    public ISkill getPassive(int number) {
        switch (number) {
            case 1:
                return new Passive1();
            default:
                return null;
        }
    }

    @Override
    public ISkill getActive(int number) {
        switch (number) {
            case 2:
                return new Active2();
            case 3:
                return new Active3();
            case 4:
                return new Ultimate();
            default:
                return null;
        }
    }

    static class Normal extends Weapon {
        static int DAMAGE = 75;
        static int DAMAGE_DISTANCE = 25;

        public Normal() {
            super(ItemBuilder.fromCSItem("HLN-12").setLore(
                    "§f",
                    "§f뛰어난 안정성을 가진 전자동 돌격소총입니다.",
                    "§7사격§f하여 ䷀ §c피해§f를 입힙니다.",
                    "§f").build());
        }
    }

    static class Passive1 extends Skill {
        static int SPRINT_SPEED = 30;

        public Passive1() {
            super("강화된 신체",
                    "",
                    "§f달리기의 §e⬆§b➠ 속도§f가 빨라집니다.",
                    "",
                    "§e⬆§b➠ §f30%");
            setSkillTrigger(SkillTrigger.SPRINT);
        }
    }

    static class Active2 extends Skill {
        static int COOLDOWN = 7;
        static int DAMAGE_DIRECT = 50;
        static float RADIUS = 3.5F;
        static int DAMAGE_EXPLODE = 100;

        public Active2() {
            super("다이아코어 미사일",
                    "",
                    "§f소형 미사일을 3회 연속으로 발사하여 §c⚔ 광역 피해",
                    "§f를 입힙니다.",
                    "",
                    "§c⚔ §f직격 50 + 폭발 100",
                    "§c✸ §f3.5m",
                    "§f⟳ §f7초",
                    "",
                    "§7§l[2] [좌클릭] §f사용");
        }

        @Override
        public int getCooldown() {
            return COOLDOWN;
        }
    }

    static class Active3 extends Skill {
        static int COOLDOWN = 12;
        static int HEAL = 350;
        static float DURATION = 2.5F;

        public Active3() {
            super("생체 회복막",
                    "",
                    "§6⌛ 지속시간§f동안 동안 회복막을 활성화하여 §a✚ 회복§f합니다.",
                    "",
                    "§6⌛ §f2.5초",
                    "§a✚ §f350",
                    "§f⟳ §f12초",
                    "",
                    "§7§l[3] §f사용");
        }

        @Override
        public int getCooldown() {
            return COOLDOWN;
        }
    }

    static class Ultimate extends Skill {
        static int COST = 7000;
        static float DURATION = 12F;

        public Ultimate() {
            super("인피니버스터",
                    "",
                    "§6⌛ 지속시간§f동안 기본 무기에 장탄수 무한, 탄퍼짐 제거, 거리별",
                    "§f피해 감소 제거 효과가 적용됩니다.",
                    "",
                    "§6⌛ §f12초",
                    "§f⚡ §f7000",
                    "",
                    "§7§l[4] §f사용");
            setUltimate(true);
        }

        @Override
        public int getCost() {
            return COST;
        }
    }
}
