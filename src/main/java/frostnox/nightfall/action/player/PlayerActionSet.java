package frostnox.nightfall.action.player;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.registry.ActionsNF;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class PlayerActionSet {
    public static final PlayerActionSet SWORD = new PlayerActionSet("sword", ActionsNF.SWORD_GUARD, ActionsNF.SWORD_BASIC_1, ActionsNF.SWORD_ALTERNATE_1, ActionsNF.SWORD_CRAWLING, null, 25, 0.8F);
    public static final PlayerActionSet SABRE = new PlayerActionSet("sabre", ActionsNF.SABRE_GUARD, ActionsNF.SABRE_BASIC_1, ActionsNF.SABRE_ALTERNATE_1, ActionsNF.SABRE_CRAWLING, null, 24, 0.8F, ActionsNF.bleeding(0.4F));
    public static final PlayerActionSet MACE = new PlayerActionSet("mace", ActionsNF.MACE_GUARD, ActionsNF.MACE_BASIC_1, ActionsNF.MACE_ALTERNATE_1, ActionsNF.MACE_CRAWLING, null, 28, 0.8F, ActionsNF.bleeding(0.3F));
    public static final PlayerActionSet CLUB = new PlayerActionSet("club", ActionsNF.EMPTY, ActionsNF.CLUB_BASIC_1, ActionsNF.CLUB_ALTERNATE_1, ActionsNF.CLUB_CRAWLING, null, 28, 0.4F);
    public static final PlayerActionSet SPEAR = new PlayerActionSet("spear", ActionsNF.SPEAR_THROW, ActionsNF.SPEAR_BASIC_1, ActionsNF.SPEAR_ALTERNATE_1, ActionsNF.SPEAR_CRAWLING, null, 22, 0.6F);
    public static final PlayerActionSet KNIFE = new PlayerActionSet("knife", ActionsNF.EMPTY, ActionsNF.KNIFE_BASIC_1, ActionsNF.KNIFE_ALTERNATE_1, ActionsNF.KNIFE_CRAWLING, ActionsNF.KNIFE_CARVE, 20, 0.6F);
    public static final PlayerActionSet CHISEL = new PlayerActionSet("chisel", ActionsNF.EMPTY, ActionsNF.CHISEL_BASIC_1, ActionsNF.CHISEL_ALTERNATE_1, ActionsNF.CHISEL_CRAWLING, ActionsNF.CHISEL_CARVE, 15, 0.4F);
    public static final PlayerActionSet HAMMER = new PlayerActionSet("hammer", ActionsNF.HAMMER_UPSET, ActionsNF.HAMMER_BASIC_1, ActionsNF.HAMMER_ALTERNATE_1, ActionsNF.HAMMER_CRAWLING, ActionsNF.HAMMER_BREAK, 25, 0.5F);
    public static final PlayerActionSet AXE = new PlayerActionSet("axe", ActionsNF.AXE_THROW, ActionsNF.AXE_BASIC_1, ActionsNF.AXE_ALTERNATE_1, ActionsNF.AXE_CRAWLING, ActionsNF.AXE_CARVE, 25, 0.5F);
    public static final PlayerActionSet PICKAXE = new PlayerActionSet("pickaxe", ActionsNF.EMPTY, ActionsNF.PICKAXE_BASIC_1, ActionsNF.PICKAXE_ALTERNATE_1, ActionsNF.PICKAXE_CRAWLING, null, 25, 0.5F);
    public static final PlayerActionSet SHOVEL = new PlayerActionSet("shovel", ActionsNF.EMPTY, ActionsNF.SHOVEL_BASIC_1, ActionsNF.SHOVEL_ALTERNATE_1, ActionsNF.SHOVEL_CRAWLING, null, 25, 0.5F);
    public static final PlayerActionSet SICKLE = new PlayerActionSet("sickle", ActionsNF.SICKLE_GUARD, ActionsNF.SICKLE_BASIC_1, ActionsNF.SICKLE_ALTERNATE_1, ActionsNF.SICKLE_CRAWLING, null, 24, 0.7F, ActionsNF.bleeding(0.35F));
    public static final PlayerActionSet ADZE = new PlayerActionSet("adze", ActionsNF.EMPTY, ActionsNF.ADZE_BASIC_1, ActionsNF.ADZE_ALTERNATE_1, ActionsNF.ADZE_CRAWLING, ActionsNF.ADZE_CARVE, 20, 0.5F);
    public static final PlayerActionSet CHISEL_AND_HAMMER = new PlayerActionSet("chisel_and_hammer", ActionsNF.EMPTY, ActionsNF.CHISEL_AND_HAMMER_BASIC_1, ActionsNF.CHISEL_AND_HAMMER_ALTERNATE, ActionsNF.CHISEL_AND_HAMMER_CRAWLING, null, 20, 0.4F);
    public static final PlayerActionSet FLINT_CHISEL_AND_HAMMER = new PlayerActionSet("flint_chisel_and_hammer", ActionsNF.EMPTY, ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_1, ActionsNF.FLINT_CHISEL_AND_HAMMER_ALTERNATE, ActionsNF.FLINT_CHISEL_AND_HAMMER_CRAWLING, null, 20, 0.4F);
    public static final PlayerActionSet MAUL = new PlayerActionSet("maul", ActionsNF.EMPTY, ActionsNF.MAUL_BASIC_1, ActionsNF.MAUL_ALTERNATE_1, ActionsNF.MAUL_CRAWLING, null, 30, 0.5F);
    public static final List<PlayerActionSet> SETS = List.of(SWORD, SABRE, MACE, CLUB, SPEAR, KNIFE, CHISEL, HAMMER, AXE, PICKAXE, SHOVEL, SICKLE, ADZE, CHISEL_AND_HAMMER, FLINT_CHISEL_AND_HAMMER, MAUL);

    public final RegistryObject<? extends Action> defaultTech, basic, alternate, crawl;
    public final @Nullable RegistryObject<? extends Action> recipeAction;
    public final float attack, defenseMul;
    public final @Nullable AttackEffect defaultEffect;
    protected final Set<ResourceLocation> set = new ObjectArraySet<>(); //Contains every Action ID that is part of this object
    private final String name;

    public PlayerActionSet(String name, RegistryObject<? extends Action> defaultTech, RegistryObject<? extends Action> basic, RegistryObject<? extends Action> alternate, RegistryObject<? extends Action> crawl, @Nullable RegistryObject<? extends Action> recipeAction, float attack, float defenseMul, @Nullable AttackEffect defaultEffect) {
        this.name = name;
        this.defaultTech = defaultTech;
        this.basic = basic;
        this.alternate = alternate;
        this.crawl = crawl;
        this.recipeAction = recipeAction;
        this.attack = attack;
        this.defenseMul = defenseMul;
        this.defaultEffect = defaultEffect;
    }

    public PlayerActionSet(String name, RegistryObject<? extends Action> defaultTech, RegistryObject<? extends Action> basic, RegistryObject<? extends Action> alternate, RegistryObject<? extends Action> crawl, @Nullable RegistryObject<? extends Action> recipeAction, float attack, float defenseMul) {
        this(name, defaultTech, basic, alternate, crawl, recipeAction, attack, defenseMul, null);
    }

    public static void init() {
        for(PlayerActionSet actionSet : SETS) {
            actionSet.set.add(actionSet.defaultTech.getId());
            actionSet.set.add(actionSet.basic.getId());
            actionSet.set.add(actionSet.alternate.getId());
            actionSet.set.add(actionSet.crawl.getId());
            if(actionSet.recipeAction != null) actionSet.set.add(actionSet.recipeAction.getId());
            actionSet.set.addAll(actionSet.defaultTech.get().linkedActions);
            actionSet.set.addAll(actionSet.basic.get().linkedActions);
            actionSet.set.addAll(actionSet.alternate.get().linkedActions);
            actionSet.set.addAll(actionSet.crawl.get().linkedActions);
            if(actionSet.recipeAction != null) actionSet.set.addAll(actionSet.recipeAction.get().linkedActions);
        }
    }

    public boolean containsAction(ResourceLocation id) {
        return set.contains(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
