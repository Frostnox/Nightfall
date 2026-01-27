package frostnox.nightfall.registry;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.*;
import frostnox.nightfall.action.npc.cockatrice.CockatriceBite;
import frostnox.nightfall.action.npc.cockatrice.CockatriceClaw;
import frostnox.nightfall.action.npc.cockatrice.CockatriceSpit;
import frostnox.nightfall.action.npc.deer.DeerGraze;
import frostnox.nightfall.action.npc.drakefowl.DrakefowlClaw;
import frostnox.nightfall.action.npc.drakefowl.DrakefowlCollapse;
import frostnox.nightfall.action.npc.drakefowl.DrakefowlSpit;
import frostnox.nightfall.action.npc.dreg.DregBuff;
import frostnox.nightfall.action.npc.dreg.DregBuild;
import frostnox.nightfall.action.npc.dreg.DregCower;
import frostnox.nightfall.action.npc.dreg.DregResurrect;
import frostnox.nightfall.action.npc.ectoplasm.EctoplasmClubLarge;
import frostnox.nightfall.action.npc.ectoplasm.EctoplasmClubMedium;
import frostnox.nightfall.action.npc.ectoplasm.EctoplasmExplode;
import frostnox.nightfall.action.npc.husk.HuskLeftSwipe;
import frostnox.nightfall.action.npc.husk.HuskOverhead;
import frostnox.nightfall.action.npc.husk.HuskRightSwipe;
import frostnox.nightfall.action.npc.merbor.MerborBreed;
import frostnox.nightfall.action.npc.merbor.MerborCollapse;
import frostnox.nightfall.action.npc.merbor.MerborGore;
import frostnox.nightfall.action.npc.pit_devil.PitDevilBite;
import frostnox.nightfall.action.npc.pit_devil.PitDevilGrowl;
import frostnox.nightfall.action.npc.rockworm.RockwormBite;
import frostnox.nightfall.action.npc.rockworm.RockwormEmerge;
import frostnox.nightfall.action.npc.rockworm.RockwormRetreat;
import frostnox.nightfall.action.npc.skeleton.SkeletonShoot;
import frostnox.nightfall.action.npc.skeleton.SkeletonThrust;
import frostnox.nightfall.action.npc.spider.SpiderBite;
import frostnox.nightfall.action.npc.wolf.WolfBite;
import frostnox.nightfall.action.npc.wolf.WolfBiteChain;
import frostnox.nightfall.action.npc.wolf.WolfGrowl;
import frostnox.nightfall.action.player.action.*;
import frostnox.nightfall.action.player.action.guard.ShieldGuardAction;
import frostnox.nightfall.action.player.action.guard.WeaponGuardAction;
import frostnox.nightfall.action.player.action.thrown.ThrowAxeTechnique;
import frostnox.nightfall.action.player.action.thrown.ThrowKnifeTechnique;
import frostnox.nightfall.action.player.action.thrown.ThrowSpearTechnique;
import frostnox.nightfall.action.player.attack.*;
import frostnox.nightfall.action.player.technique.HammerTechnique;
import frostnox.nightfall.block.block.anvil.AnvilAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.CombatUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ActionsNF {
    public static final DeferredRegister<Action> ACTIONS = DeferredRegister.create(RegistriesNF.ACTIONS_KEY, Nightfall.MODID);

    protected final static DamageType[] STRIKING_SLASHING = new DamageType[] {DamageType.STRIKING, DamageType.SLASHING};
    protected final static DamageType[] PIERCING_SLASHING = new DamageType[] {DamageType.PIERCING, DamageType.SLASHING};
    protected final static DamageType[] PIERCING_STRIKING = new DamageType[] {DamageType.PIERCING, DamageType.STRIKING};
    protected final static float SWORD_KNOCKBACK = 0.1F;
    protected final static float SABRE_KNOCKBACK = 0.1F;
    protected final static float MACE_KNOCKBACK = 0.3F;
    protected final static float CLUB_KNOCKBACK = 0.25F;
    protected final static float SPEAR_KNOCKBACK = 0.1F;
    protected final static float KNIFE_KNOCKBACK = 0.05F;
    protected final static float CHISEL_KNOCKBACK = 0.05F;
    protected final static float HAMMER_KNOCKBACK = 0.2F;
    protected final static float AXE_KNOCKBACK = 0.25F;
    protected final static float PICKAXE_KNOCKBACK = 0.2F;
    protected final static float SICKLE_KNOCKBACK = 0.1F;
    protected final static float ADZE_KNOCKBACK = 0.1F;
    protected final static float MAUL_KNOCKBACK = 0.35F;
    protected final static Function<LivingEntity, Boolean> HITSTOP = user -> user instanceof Player player && PlayerData.get(player).getHitStopFrame() != -1;
    protected final static Function<LivingEntity, Boolean> DUG_BLOCK = user -> user instanceof Player player && PlayerData.get(player).hasDugBlock();

    public static final RegistryObject<GenericAttack> EMPTY = ACTIONS.register("empty", () ->
            new GenericAttack(0, DamageType.ABSOLUTE.asArray(), HurtSphere.NONE, 0, 0, new int[] {1}));

    //Special
    public static final RegistryObject<HoldEntity> HOLD_ENTITY = ACTIONS.register("hold_entity", () ->
            new HoldEntity(new Action.Properties().setIdle(), 1));

    //Melee weapons
    public static final RegistryObject<WideSwingRight> SWORD_BASIC_1 = ACTIONS.register("sword_basic_1", () ->
            new WideSwingRight(DamageType.SLASHING.asArray(), HurtSphere.SWORD, 3, CombatUtil.STUN_MEDIUM, new int[] {6, 11, 2, 8}, new Action.Properties().setChainTo(ActionsNF.SWORD_BASIC_2).setChainState(2).setKnockback(SWORD_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING)));
    public static final RegistryObject<WideSwingLeft> SWORD_BASIC_2 = ACTIONS.register("sword_basic_2", () ->
            new WideSwingLeft(DamageType.SLASHING.asArray(), HurtSphere.SWORD, 3, CombatUtil.STUN_MEDIUM, new int[] {6, 11, 2, 8}, new Action.Properties().setChainFrom(SWORD_BASIC_1).setChainTo(ActionsNF.SWORD_BASIC_3).setChainState(2).setKnockback(SWORD_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING)));
    public static final RegistryObject<WideSwingRight> SWORD_BASIC_3 = ACTIONS.register("sword_basic_3", () ->
            new WideSwingRight(DamageType.SLASHING.asArray(), HurtSphere.SWORD, 3, CombatUtil.STUN_MEDIUM, new int[] {6, 11, 2, 8}, new Action.Properties().setChainFrom(SWORD_BASIC_2).setChainTo(SWORD_BASIC_2).setChainState(2).setKnockback(SWORD_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING)));
    public static final RegistryObject<ThrustCharged> SWORD_ALTERNATE_1 = ACTIONS.register("sword_alternate_1", () ->
            new ThrustCharged(DamageType.PIERCING.asArray(), HurtSphere.SWORD, 1, CombatUtil.STUN_LONG, new int[] {24, 5, 4, 7}, new Action.Properties().setChargeState(0).setSound(SoundsNF.BLADE_SWING).setKnockback(SWORD_KNOCKBACK).setImpact(Impact.MEDIUM).setExtraSound(SoundsNF.BLADE_SWING_CHARGED)));
    public static final RegistryObject<CrawlingThrust> SWORD_CRAWLING = ACTIONS.register("sword_crawling", () ->
            new CrawlingThrust(DamageType.PIERCING.asArray(), HurtSphere.SWORD, 1, CombatUtil.STUN_SHORT, new int[] {9, 5, 3, 8}, new Action.Properties().setSprinting().setCrawling().setKnockback(SWORD_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING)));

    public static final RegistryObject<DiagonalSwingRight> SABRE_BASIC_1 = ACTIONS.register("sabre_basic_1", () ->
            new DiagonalSwingRight(DamageType.SLASHING.asArray(), HurtSphere.SABRE, 2, CombatUtil.STUN_SHORT, new int[] {7, 7, 3, 8}, new Action.Properties().setChainTo(ActionsNF.SABRE_BASIC_2).setChainState(2).setKnockback(SABRE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING), bleeding(0.4F)));
    public static final RegistryObject<DiagonalSwingLeft> SABRE_BASIC_2 = ACTIONS.register("sabre_basic_2", () ->
            new DiagonalSwingLeft(DamageType.SLASHING.asArray(), HurtSphere.SABRE, 2, CombatUtil.STUN_SHORT, new int[] {7, 7, 3, 7}, new Action.Properties().setChainFrom(SABRE_BASIC_1).setChainTo(ActionsNF.SABRE_BASIC_3).setChainState(2).setKnockback(SABRE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING), bleeding(0.4F)));
    public static final RegistryObject<DiagonalSwingRight> SABRE_BASIC_3 = ACTIONS.register("sabre_basic_3", () ->
            new DiagonalSwingRight(DamageType.SLASHING.asArray(), HurtSphere.SABRE, 2, CombatUtil.STUN_SHORT, new int[] {7, 7, 3, 8}, new Action.Properties().setChainFrom(SABRE_BASIC_2).setChainTo(SABRE_BASIC_2).setChainState(2).setKnockback(SABRE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING), bleeding(0.4F)));
    public static final RegistryObject<WideSwingCharged> SABRE_ALTERNATE_1 = ACTIONS.register("sabre_alternate_1", () ->
            new WideSwingCharged(DamageType.SLASHING.asArray(), HurtSphere.SABRE, 3, CombatUtil.STUN_LONG, new int[] {24, 10, 4, 8}, new Action.Properties().setChargeState(0).setKnockback(SABRE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING).setExtraSound(SoundsNF.BLADE_SWING_CHARGED), bleeding(0.4F)));
    public static final RegistryObject<CrawlingSwing> SABRE_CRAWLING = ACTIONS.register("sabre_crawling", () ->
            new CrawlingSwing(DamageType.SLASHING.asArray(), HurtSphere.SABRE, 1, CombatUtil.STUN_SHORT, new int[] {8, 6, 3, 7}, new Action.Properties().setSprinting().setCrawling().setKnockback(SABRE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING), bleeding(0.4F)));

    public static final RegistryObject<LongVerticalSwing> MACE_BASIC_1 = ACTIONS.register("mace_basic_1", () ->
            new LongVerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.MACE, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 3, 8}, new Action.Properties().setChainTo(ActionsNF.MACE_BASIC_2).setChainState(2).setKnockback(MACE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING), bleeding(0.3F)));
    public static final RegistryObject<LongVerticalSwing> MACE_BASIC_2 = ACTIONS.register("mace_basic_2", () ->
            new LongVerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.MACE, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 3, 8}, new Action.Properties().setChainFrom(MACE_BASIC_1).setChainTo(ActionsNF.MACE_BASIC_3).setChainState(2).setKnockback(MACE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING), bleeding(0.3F)));
    public static final RegistryObject<LongVerticalSwing> MACE_BASIC_3 = ACTIONS.register("mace_basic_3", () ->
            new LongVerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.MACE, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 3, 8}, new Action.Properties().setChainFrom(MACE_BASIC_2).setChainTo(MACE_BASIC_2).setChainState(2).setKnockback(MACE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING), bleeding(0.3F)));
    public static final RegistryObject<LongVerticalSwingCharged> MACE_ALTERNATE_1 = ACTIONS.register("mace_alternate_1", () ->
            new LongVerticalSwingCharged(DamageType.STRIKING.asArray(), HurtSphere.MACE, 1, CombatUtil.STUN_VERY_LONG, new int[] {28, 6, 4, 8}, new Action.Properties().setChargeState(0).setKnockback(MACE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED), bleeding(0.3F)));
    public static final RegistryObject<CrawlingSwing> MACE_CRAWLING = ACTIONS.register("mace_crawling", () ->
            new CrawlingSwing(DamageType.STRIKING.asArray(), HurtSphere.MACE, 1, CombatUtil.STUN_SHORT, new int[] {9, 6, 3, 8}, new Action.Properties().setSprinting().setCrawling().setKnockback(MACE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING), bleeding(0.3F)));

    public static final RegistryObject<LongVerticalSwing> CLUB_BASIC_1 = ACTIONS.register("club_basic_1", () ->
            new LongVerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.CLUB, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 3, 8}, new Action.Properties().setChainTo(ActionsNF.CLUB_BASIC_2).setChainState(2).setKnockback(CLUB_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING)));
    public static final RegistryObject<LongVerticalSwing> CLUB_BASIC_2 = ACTIONS.register("club_basic_2", () ->
            new LongVerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.CLUB, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 3, 8}, new Action.Properties().setChainFrom(CLUB_BASIC_1).setChainTo(ActionsNF.CLUB_BASIC_3).setChainState(2).setKnockback(CLUB_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING)));
    public static final RegistryObject<LongVerticalSwing> CLUB_BASIC_3 = ACTIONS.register("club_basic_3", () ->
            new LongVerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.CLUB, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 3, 8}, new Action.Properties().setChainFrom(CLUB_BASIC_2).setChainTo(CLUB_BASIC_2).setChainState(2).setKnockback(CLUB_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING)));
    public static final RegistryObject<LongVerticalSwingCharged> CLUB_ALTERNATE_1 = ACTIONS.register("club_alternate_1", () ->
            new LongVerticalSwingCharged(DamageType.STRIKING.asArray(), HurtSphere.CLUB, 1, CombatUtil.STUN_VERY_LONG, new int[] {28, 6, 4, 8}, new Action.Properties().setChargeState(0).setKnockback(CLUB_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED)));
    public static final RegistryObject<CrawlingSwing> CLUB_CRAWLING = ACTIONS.register("club_crawling", () ->
            new CrawlingSwing(DamageType.STRIKING.asArray(), HurtSphere.CLUB, 1, CombatUtil.STUN_SHORT, new int[] {9, 6, 3, 8}, new Action.Properties().setSprinting().setCrawling().setKnockback(CLUB_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING)));

    public static final RegistryObject<Thrust> SPEAR_BASIC_1 = ACTIONS.register("spear_basic_1", () ->
            new Thrust(DamageType.PIERCING.asArray(), HurtSphere.SPEAR, 1, CombatUtil.STUN_SHORT, new int[] {10, 5, 3, 9}, new Action.Properties().setChainTo(ActionsNF.SPEAR_BASIC_2).setChainState(2).setKnockback(SPEAR_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.LONG_BLADE_SWING)));
    public static final RegistryObject<Thrust> SPEAR_BASIC_2 = ACTIONS.register("spear_basic_2", () ->
            new Thrust(DamageType.PIERCING.asArray(), HurtSphere.SPEAR, 1, CombatUtil.STUN_SHORT, new int[] {10, 5, 3, 9}, new Action.Properties().setChainFrom(SPEAR_BASIC_1).setChainTo(ActionsNF.SPEAR_BASIC_3).setChainState(2).setKnockback(SPEAR_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.LONG_BLADE_SWING)));
    public static final RegistryObject<Thrust> SPEAR_BASIC_3 = ACTIONS.register("spear_basic_3", () ->
            new Thrust(DamageType.PIERCING.asArray(), HurtSphere.SPEAR, 1, CombatUtil.STUN_SHORT, new int[] {10, 5, 3, 9}, new Action.Properties().setChainFrom(SPEAR_BASIC_2).setChainTo(SPEAR_BASIC_2).setChainState(2).setKnockback(SPEAR_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.LONG_BLADE_SWING)));
    public static final RegistryObject<ThrustCharged> SPEAR_ALTERNATE_1 = ACTIONS.register("spear_alternate_1", () ->
            new ThrustCharged(DamageType.PIERCING.asArray(), HurtSphere.SPEAR, 1, CombatUtil.STUN_LONG, new int[] {24, 5, 3, 9}, new Action.Properties().setChargeState(0).setKnockback(SPEAR_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.LONG_BLADE_SWING).setExtraSound(SoundsNF.LONG_BLADE_SWING_CHARGED)));
    public static final RegistryObject<CrawlingThrust> SPEAR_CRAWLING = ACTIONS.register("spear_crawling", () ->
            new CrawlingThrust(DamageType.PIERCING.asArray(), HurtSphere.SPEAR, 1, CombatUtil.STUN_SHORT, new int[] {10, 5, 3, 9}, new Action.Properties().setSprinting().setCrawling().setKnockback(SPEAR_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.LONG_BLADE_SWING)));

    public static final RegistryObject<SwingRight> KNIFE_BASIC_1 = ACTIONS.register("knife_basic_1", () ->
            new SwingRight(DamageType.SLASHING.asArray(), HurtSphere.KNIFE, 2, CombatUtil.STUN_SHORT, new int[] {6, 8, 3, 8}, new Action.Properties().setChainTo(ActionsNF.KNIFE_BASIC_2).setChainState(2).setSound(SoundsNF.SHORT_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_KNIFE).setKnockback(KNIFE_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<SwingLeft> KNIFE_BASIC_2 = ACTIONS.register("knife_basic_2", () ->
            new SwingLeft(DamageType.SLASHING.asArray(), HurtSphere.KNIFE, 2, CombatUtil.STUN_SHORT, new int[] {6, 8, 3, 7}, new Action.Properties().setChainFrom(KNIFE_BASIC_1).setChainTo(ActionsNF.KNIFE_BASIC_3).setChainState(2).setSound(SoundsNF.SHORT_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_KNIFE).setKnockback(KNIFE_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<SwingRight> KNIFE_BASIC_3 = ACTIONS.register("knife_basic_3_conditional", () ->
            new SwingRight(DamageType.SLASHING.asArray(), HurtSphere.KNIFE, 2, CombatUtil.STUN_SHORT, new int[] {6, 8, 3, 8}, new Action.Properties().setChainTo(ActionsNF.KNIFE_BASIC_2).setChainFrom(KNIFE_BASIC_2).setChainState(2).setSound(SoundsNF.SHORT_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_KNIFE).setKnockback(KNIFE_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<StabCharged> KNIFE_ALTERNATE_1 = ACTIONS.register("knife_alternate_1", () ->
            new StabCharged(DamageType.PIERCING.asArray(), HurtSphere.KNIFE, 1, CombatUtil.STUN_LONG, new int[] {7, 13, 4, 4, 7}, new Action.Properties().setChargeState(1).setSound(SoundsNF.SHORT_BLADE_SWING).setExtraSound(SoundsNF.SHORT_BLADE_SWING_CHARGED).setKnockback(KNIFE_KNOCKBACK).setImpact(Impact.MEDIUM), bleeding(0.4F)));
    public static final RegistryObject<CrawlingSwing> KNIFE_CRAWLING = ACTIONS.register("knife_crawling", () ->
            new CrawlingSwing(DamageType.SLASHING.asArray(), HurtSphere.KNIFE, 1, CombatUtil.STUN_SHORT, new int[] {6, 5, 3, 7}, new Action.Properties().setSprinting().setCrawling().setSound(SoundsNF.SHORT_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_KNIFE).setKnockback(KNIFE_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<KnifeCarveAction> KNIFE_CARVE = ACTIONS.register("knife_carve", () ->
            new KnifeCarveAction(new int[] {6, 43, 7}, new Action.Properties().setChargeState(1).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED)));

    public static final RegistryObject<Stab> CHISEL_BASIC_1 = ACTIONS.register("chisel_basic_1", () ->
            new Stab(DamageType.PIERCING.asArray(), HurtSphere.CHISEL, 1, CombatUtil.STUN_SHORT, new int[] {9, 4, 4, 8}, new Action.Properties().setChainTo(ActionsNF.CHISEL_BASIC_2).setChainState(2).setSound(SoundsNF.SHORT_BLADE_SWING).setKnockback(CHISEL_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<Stab> CHISEL_BASIC_2 = ACTIONS.register("chisel_basic_2", () ->
            new Stab(DamageType.PIERCING.asArray(), HurtSphere.CHISEL, 1, CombatUtil.STUN_SHORT, new int[] {8, 4, 4, 7}, new Action.Properties().setChainFrom(CHISEL_BASIC_1).setChainTo(ActionsNF.CHISEL_BASIC_3).setChainState(2).setSound(SoundsNF.SHORT_BLADE_SWING).setKnockback(CHISEL_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<Stab> CHISEL_BASIC_3 = ACTIONS.register("chisel_basic_3", () ->
            new Stab(DamageType.PIERCING.asArray(), HurtSphere.CHISEL, 1, CombatUtil.STUN_SHORT, new int[] {8, 4, 4, 7}, new Action.Properties().setChainFrom(CHISEL_BASIC_2).setChainTo(ActionsNF.CHISEL_BASIC_2).setChainState(2).setSound(SoundsNF.SHORT_BLADE_SWING).setKnockback(CHISEL_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<StabCharged> CHISEL_ALTERNATE_1 = ACTIONS.register("chisel_alternate_1", () ->
            new StabCharged(DamageType.PIERCING.asArray(), HurtSphere.CHISEL, 1, CombatUtil.STUN_MEDIUM, new int[] {8, 13, 4, 4, 7}, new Action.Properties().setChargeState(1).setSound(SoundsNF.SHORT_BLADE_SWING).setExtraSound(SoundsNF.SHORT_BLADE_SWING_CHARGED).setKnockback(CHISEL_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<CrawlingThrust> CHISEL_CRAWLING = ACTIONS.register("chisel_crawling", () ->
            new CrawlingThrust(DamageType.PIERCING.asArray(), HurtSphere.CHISEL, 1, CombatUtil.STUN_SHORT, new int[] {8, 5, 3, 8}, new Action.Properties().setSprinting().setCrawling().setSound(SoundsNF.SHORT_BLADE_SWING).setKnockback(CHISEL_KNOCKBACK).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<ChiselCarveAction> CHISEL_CARVE = ACTIONS.register("chisel_carve", () ->
            new ChiselCarveAction(new int[] {8, 37, 9}, new Action.Properties().setChargeState(1).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED)));

    public static final RegistryObject<HammerStrike> HAMMER_BASIC_1 = ACTIONS.register("hammer_basic_1", () ->
            new HammerStrike(DamageType.STRIKING.asArray(), HurtSphere.HAMMER, 1, CombatUtil.STUN_MEDIUM, new int[] {8, 6, 4, 7}, new Action.Properties().setChainTo(ActionsNF.HAMMER_BASIC_2).setChainState(2).setKnockback(HAMMER_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING)));
    public static final RegistryObject<HammerStrike> HAMMER_BASIC_2 = ACTIONS.register("hammer_basic_2", () ->
            new HammerStrike(DamageType.STRIKING.asArray(), HurtSphere.HAMMER, 1, CombatUtil.STUN_MEDIUM, new int[] {8, 6, 4, 7}, new Action.Properties().setChainFrom(HAMMER_BASIC_1).setChainTo(ActionsNF.HAMMER_BASIC_3).setChainState(2).setKnockback(HAMMER_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING)));
    public static final RegistryObject<HammerStrike> HAMMER_BASIC_3 = ACTIONS.register("hammer_basic_3", () ->
            new HammerStrike(DamageType.STRIKING.asArray(), HurtSphere.HAMMER, 1, CombatUtil.STUN_MEDIUM, new int[] {8, 6, 4, 7}, new Action.Properties().setChainFrom(HAMMER_BASIC_2).setChainTo(HAMMER_BASIC_2).setChainState(2).setKnockback(HAMMER_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING)));
    public static final RegistryObject<HammerTechnique> HAMMER_ALTERNATE_1 = ACTIONS.register("hammer_alternate_1", () ->
            new HammerTechnique(AnvilAction.DRAW, AnvilAction.DRAW_LINE, DamageType.STRIKING.asArray(), HurtSphere.HAMMER, 1, CombatUtil.STUN_MEDIUM, new int[] {24, 5, 4, 9}, new Action.Properties().setChargeState(0).setKnockback(HAMMER_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED)));
    public static final RegistryObject<CrawlingSwing> HAMMER_CRAWLING = ACTIONS.register("hammer_crawling", () ->
            new CrawlingSwing(DamageType.STRIKING.asArray(), HurtSphere.HAMMER, 1, CombatUtil.STUN_MEDIUM, new int[] {8, 6, 4, 7}, new Action.Properties().setSprinting().setCrawling().setKnockback(HAMMER_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING).setSound(SoundsNF.SWING)));
    public static final RegistryObject<HammerBreak> HAMMER_BREAK = ACTIONS.register("hammer_break", () ->
            new HammerBreak(new int[] {7, 37, 7}, new Action.Properties().setChargeState(1)));

    public static final RegistryObject<WideSwingRight> AXE_BASIC_1 = ACTIONS.register("axe_basic_1", () ->
            new WideSwingRight(STRIKING_SLASHING, HurtSphere.AXE, 2, CombatUtil.STUN_LONG, new int[] {7, 12, 2, 8}, new Action.Properties().setChainTo(ActionsNF.AXE_BASIC_2).setChainState(2).setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setConditionalChainTo(ActionsNF.AXE_BASIC_2_CONDITIONAL).setConditionalChainFunction(HITSTOP).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(BlockTags.MINEABLE_WITH_AXE)));
    public static final RegistryObject<WideSwingLeft> AXE_BASIC_2 = ACTIONS.register("axe_basic_2", () ->
            new WideSwingLeft(STRIKING_SLASHING, HurtSphere.AXE, 2, CombatUtil.STUN_LONG, new int[] {7, 12, 2, 8}, new Action.Properties().setChainFrom(AXE_BASIC_1).setChainTo(ActionsNF.AXE_BASIC_3).setChainState(2).setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(BlockTags.MINEABLE_WITH_AXE)));
    public static final RegistryObject<WideSwingRight> AXE_BASIC_2_CONDITIONAL = ACTIONS.register("axe_basic_2_conditional", () ->
            new WideSwingRight(STRIKING_SLASHING, HurtSphere.AXE, 2, CombatUtil.STUN_LONG, new int[] {7, 12, 2, 8}, new Action.Properties().setChainFrom(AXE_BASIC_1).setChainTo(ActionsNF.AXE_BASIC_2).setChainState(2).setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setConditionalChainTo(ActionsNF.AXE_BASIC_3_CONDITIONAL).setConditionalChainFunction(HITSTOP).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(BlockTags.MINEABLE_WITH_AXE)));
    public static final RegistryObject<WideSwingRight> AXE_BASIC_3 = ACTIONS.register("axe_basic_3", () ->
            new WideSwingRight(STRIKING_SLASHING, HurtSphere.AXE, 2, CombatUtil.STUN_LONG, new int[] {7, 12, 2, 8}, new Action.Properties().setChainFrom(AXE_BASIC_2).setChainTo(AXE_BASIC_2).setChainState(2).setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setConditionalChainTo(ActionsNF.AXE_BASIC_2_CONDITIONAL).setConditionalChainFunction(HITSTOP).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(BlockTags.MINEABLE_WITH_AXE)));
    public static final RegistryObject<WideSwingRight> AXE_BASIC_3_CONDITIONAL = ACTIONS.register("axe_basic_3_conditional", () ->
            new WideSwingRight(STRIKING_SLASHING, HurtSphere.AXE, 2, CombatUtil.STUN_LONG, new int[] {7, 12, 2, 8}, new Action.Properties().setChainFrom(AXE_BASIC_2_CONDITIONAL).setChainTo(AXE_BASIC_2).setChainState(2).setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setConditionalChainTo(ActionsNF.AXE_BASIC_2_CONDITIONAL).setConditionalChainFunction(HITSTOP).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(BlockTags.MINEABLE_WITH_AXE)));
    public static final RegistryObject<VerticalSwingCharged> AXE_ALTERNATE_1 = ACTIONS.register("axe_alternate_1", () ->
            new VerticalSwingCharged(STRIKING_SLASHING, HurtSphere.AXE, 1, CombatUtil.STUN_VERY_LONG, new int[] {24, 6, 4, 7}, new Action.Properties().setChargeState(0).setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setExtraSound(SoundsNF.HEAVY_BLADE_SWING_CHARGED).setHarvestable(BlockTags.MINEABLE_WITH_AXE)));
    public static final RegistryObject<CrawlingSwing> AXE_CRAWLING = ACTIONS.register("axe_crawling", () ->
            new CrawlingSwing(STRIKING_SLASHING, HurtSphere.AXE, 2, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 3, 7}, new Action.Properties().setSprinting().setCrawling().setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(BlockTags.MINEABLE_WITH_AXE)));
    public static final RegistryObject<AxeCarveAction> AXE_CARVE = ACTIONS.register("axe_carve", () ->
            new AxeCarveAction(new int[] {7, 43, 8}, new Action.Properties().setChargeState(1).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED)));

    public static final RegistryObject<VerticalSwing> PICKAXE_BASIC_1 = ACTIONS.register("pickaxe_basic_1", () ->
            new VerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.PICKAXE, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 4, 7}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(2).setKnockback(PICKAXE_KNOCKBACK).setImpact(Impact.MEDIUM).setChainTo(ActionsNF.PICKAXE_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<VerticalSwing> PICKAXE_BASIC_2 = ACTIONS.register("pickaxe_basic_2", () ->
            new VerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.PICKAXE, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 4, 7}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(2).setKnockback(PICKAXE_KNOCKBACK).setImpact(Impact.MEDIUM).setChainTo(ActionsNF.PICKAXE_BASIC_3).setChainFrom(ActionsNF.PICKAXE_BASIC_1).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<VerticalSwing> PICKAXE_BASIC_3 = ACTIONS.register("pickaxe_basic_3", () ->
            new VerticalSwing(DamageType.STRIKING.asArray(), HurtSphere.PICKAXE, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 4, 7}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(2).setKnockback(PICKAXE_KNOCKBACK).setImpact(Impact.MEDIUM).setChainTo(ActionsNF.PICKAXE_BASIC_2).setChainFrom(ActionsNF.PICKAXE_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<VerticalSwingCharged> PICKAXE_ALTERNATE_1 = ACTIONS.register("pickaxe_alternate_1", () ->
            new VerticalSwingCharged(DamageType.STRIKING.asArray(), HurtSphere.PICKAXE, 1, CombatUtil.STUN_VERY_LONG, new int[] {24, 6, 4, 7}, new Action.Properties().setChargeState(0).setKnockback(PICKAXE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<CrawlingSwing> PICKAXE_CRAWLING = ACTIONS.register("pickaxe_crawling", () ->
            new CrawlingSwing(DamageType.STRIKING.asArray(), HurtSphere.PICKAXE, 1, CombatUtil.STUN_MEDIUM, new int[] {9, 6, 4, 7}, new Action.Properties().setSprinting().setCrawling().setKnockback(PICKAXE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));

    public static final RegistryObject<Dig> SHOVEL_BASIC_1 = ACTIONS.register("shovel_basic_1", () ->
            new Dig(new int[] {9, 4, 5, 3, 6}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(3).setChainTo(ActionsNF.SHOVEL_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_SHOVEL)));
    public static final RegistryObject<Dig> SHOVEL_BASIC_2 = ACTIONS.register("shovel_basic_2", () ->
            new Dig(new int[] {9, 4, 5, 3, 6}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(3).setChainTo(ActionsNF.SHOVEL_BASIC_3).setChainFrom(ActionsNF.SHOVEL_BASIC_1).setHarvestable(BlockTags.MINEABLE_WITH_SHOVEL)));
    public static final RegistryObject<Dig> SHOVEL_BASIC_3 = ACTIONS.register("shovel_basic_3", () ->
            new Dig(new int[] {9, 4, 5, 3, 6}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(3).setChainTo(ActionsNF.SHOVEL_BASIC_2).setChainFrom(ActionsNF.SHOVEL_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_SHOVEL)));
    public static final RegistryObject<DigCharged> SHOVEL_ALTERNATE_1 = ACTIONS.register("shovel_alternate_1", () ->
            new DigCharged(new int[] {24, 4, 5, 3, 6}, new Action.Properties().setChargeState(0).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED).setHarvestable(BlockTags.MINEABLE_WITH_SHOVEL)));
    public static final RegistryObject<CrawlingDig> SHOVEL_CRAWLING = ACTIONS.register("shovel_crawling", () ->
            new CrawlingDig(new int[] {9, 4, 5, 3, 6}, new Action.Properties().setSprinting().setCrawling().setSound(SoundsNF.SWING).setHarvestable(BlockTags.MINEABLE_WITH_SHOVEL)));

    public static final RegistryObject<WideSwingRight> SICKLE_BASIC_1 = ACTIONS.register("sickle_basic_1", () ->
            new WideSwingRight(PIERCING_SLASHING, HurtSphere.SICKLE, 2, CombatUtil.STUN_MEDIUM, new int[] {7, 11, 2, 9}, new Action.Properties().setSound(SoundsNF.BLADE_SWING).setChainState(2).setChainTo(ActionsNF.SICKLE_BASIC_2).setKnockback(SICKLE_KNOCKBACK).setImpact(Impact.MEDIUM).setHarvestable(TagsNF.MINEABLE_WITH_SICKLE), bleeding(0.35F)));
    public static final RegistryObject<WideSwingLeft> SICKLE_BASIC_2 = ACTIONS.register("sickle_basic_2", () ->
            new WideSwingLeft(PIERCING_SLASHING, HurtSphere.SICKLE, 2, CombatUtil.STUN_MEDIUM, new int[] {7, 11, 2, 9}, new Action.Properties().setSound(SoundsNF.BLADE_SWING).setChainState(2).setChainTo(ActionsNF.SICKLE_BASIC_3).setChainFrom(ActionsNF.SICKLE_BASIC_1).setKnockback(SICKLE_KNOCKBACK).setImpact(Impact.MEDIUM).setHarvestable(TagsNF.MINEABLE_WITH_SICKLE), bleeding(0.35F)));
    public static final RegistryObject<WideSwingRight> SICKLE_BASIC_3 = ACTIONS.register("sickle_basic_3", () ->
            new WideSwingRight(PIERCING_SLASHING, HurtSphere.SICKLE, 2, CombatUtil.STUN_MEDIUM, new int[] {7, 11, 2, 9}, new Action.Properties().setSound(SoundsNF.BLADE_SWING).setChainState(2).setChainTo(ActionsNF.SICKLE_BASIC_2).setChainFrom(ActionsNF.SICKLE_BASIC_2).setKnockback(SICKLE_KNOCKBACK).setImpact(Impact.MEDIUM).setHarvestable(TagsNF.MINEABLE_WITH_SICKLE), bleeding(0.35F)));
    public static final RegistryObject<DiagonalSwingCharged> SICKLE_ALTERNATE_1 = ACTIONS.register("sickle_alternate_1", () ->
            new DiagonalSwingCharged(DamageType.PIERCING.asArray(), HurtSphere.SICKLE, 2, CombatUtil.STUN_VERY_LONG, new int[] {24, 7, 4, 7}, new Action.Properties().setChargeState(0).setKnockback(SICKLE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING).setExtraSound(SoundsNF.BLADE_SWING_CHARGED).setHarvestable(TagsNF.MINEABLE_WITH_SICKLE), bleeding(0.35F)));
    public static final RegistryObject<CrawlingSwing> SICKLE_CRAWLING = ACTIONS.register("sickle_crawling", () ->
            new CrawlingSwing(PIERCING_SLASHING, HurtSphere.SICKLE, 1, CombatUtil.STUN_MEDIUM, new int[] {10, 6, 4, 8}, new Action.Properties().setSprinting().setCrawling().setKnockback(SICKLE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_SICKLE), bleeding(0.35F)));

    public static final RegistryObject<VerticalSwing> ADZE_BASIC_1 = ACTIONS.register("adze_basic_1", () ->
            new VerticalSwing(DamageType.SLASHING.asArray(), HurtSphere.ADZE, 1, CombatUtil.STUN_SHORT, new int[] {9, 6, 4, 7}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(2).setChainTo(ActionsNF.ADZE_BASIC_2).setKnockback(ADZE_KNOCKBACK).setImpact(Impact.MEDIUM).setHarvestable(TagsNF.MINEABLE_WITH_ADZE)));
    public static final RegistryObject<VerticalSwing> ADZE_BASIC_2 = ACTIONS.register("adze_basic_2", () ->
            new VerticalSwing(DamageType.SLASHING.asArray(), HurtSphere.ADZE, 1, CombatUtil.STUN_SHORT, new int[] {9, 6, 4, 7}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(2).setChainTo(ActionsNF.ADZE_BASIC_3).setChainFrom(ActionsNF.ADZE_BASIC_1).setKnockback(ADZE_KNOCKBACK).setImpact(Impact.MEDIUM).setHarvestable(TagsNF.MINEABLE_WITH_ADZE)));
    public static final RegistryObject<VerticalSwing> ADZE_BASIC_3 = ACTIONS.register("adze_basic_3", () ->
            new VerticalSwing(DamageType.SLASHING.asArray(), HurtSphere.ADZE, 1, CombatUtil.STUN_SHORT, new int[] {9, 6, 4, 7}, new Action.Properties().setSound(SoundsNF.SWING).setChainState(2).setChainTo(ActionsNF.ADZE_BASIC_2).setChainFrom(ActionsNF.ADZE_BASIC_2).setKnockback(ADZE_KNOCKBACK).setImpact(Impact.MEDIUM).setHarvestable(TagsNF.MINEABLE_WITH_ADZE)));
    public static final RegistryObject<VerticalSwingCharged> ADZE_ALTERNATE_1 = ACTIONS.register("adze_alternate_1", () ->
            new VerticalSwingCharged(DamageType.SLASHING.asArray(), HurtSphere.ADZE, 1, CombatUtil.STUN_LONG, new int[] {24, 6, 4, 7}, new Action.Properties().setChargeState(0).setKnockback(ADZE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED).setHarvestable(TagsNF.MINEABLE_WITH_ADZE)));
    public static final RegistryObject<CrawlingSwing> ADZE_CRAWLING = ACTIONS.register("adze_crawling", () ->
            new CrawlingSwing(DamageType.SLASHING.asArray(), HurtSphere.ADZE, 1, CombatUtil.STUN_SHORT, new int[] {9, 6, 4, 7}, new Action.Properties().setSprinting().setCrawling().setSound(SoundsNF.SWING).setKnockback(ADZE_KNOCKBACK).setImpact(Impact.MEDIUM).setHarvestable(TagsNF.MINEABLE_WITH_ADZE)));
    public static final RegistryObject<AdzeCarveAction> ADZE_CARVE = ACTIONS.register("adze_carve", () ->
            new AdzeCarveAction(new int[] {6, 43, 7}, new Action.Properties().setChargeState(1).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED)));

    public static final RegistryObject<ChiselAndHammerBasic> CHISEL_AND_HAMMER_BASIC_1 = ACTIONS.register("chisel_and_hammer_basic_1", () ->
            new ChiselAndHammerBasic(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_CHISEL_HIT).setChainState(3).setChainTo(ActionsNF.CHISEL_AND_HAMMER_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerBasic> CHISEL_AND_HAMMER_BASIC_2 = ACTIONS.register("chisel_and_hammer_basic_2", () ->
            new ChiselAndHammerBasic(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_CHISEL_HIT).setChainState(3).setChainTo(ActionsNF.CHISEL_AND_HAMMER_BASIC_3).setChainFrom(ActionsNF.CHISEL_AND_HAMMER_BASIC_1).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerBasic> CHISEL_AND_HAMMER_BASIC_3 = ACTIONS.register("chisel_and_hammer_basic_3", () ->
            new ChiselAndHammerBasic(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_CHISEL_HIT).setChainState(3).setChainTo(ActionsNF.CHISEL_AND_HAMMER_BASIC_2).setChainFrom(ActionsNF.CHISEL_AND_HAMMER_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerAlternate> CHISEL_AND_HAMMER_ALTERNATE = ACTIONS.register("chisel_and_hammer_alternate", () ->
            new ChiselAndHammerAlternate(new int[] {9, 15, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_CHISEL_HIT).setExtraSound(SoundsNF.HAMMER_CHISEL_HIT).setChargeState(1).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerCrawling> CHISEL_AND_HAMMER_CRAWLING = ACTIONS.register("chisel_and_hammer_crawling", () ->
            new ChiselAndHammerCrawling(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_CHISEL_HIT).setSprinting().setCrawling().setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));

    public static final RegistryObject<ChiselAndHammerBasic> FLINT_CHISEL_AND_HAMMER_BASIC_1 = ACTIONS.register("flint_chisel_and_hammer_basic_1", () ->
            new ChiselAndHammerBasic(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_FLINT_CHISEL_HIT).setChainState(3).setChainTo(ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerBasic> FLINT_CHISEL_AND_HAMMER_BASIC_2 = ACTIONS.register("flint_chisel_and_hammer_basic_2", () ->
            new ChiselAndHammerBasic(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_FLINT_CHISEL_HIT).setChainState(3).setChainTo(ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_3).setChainFrom(ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_1).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerBasic> FLINT_CHISEL_AND_HAMMER_BASIC_3 = ACTIONS.register("flint_chisel_and_hammer_basic_3", () ->
            new ChiselAndHammerBasic(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_FLINT_CHISEL_HIT).setChainState(3).setChainTo(ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_2).setChainFrom(ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_2).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerAlternate> FLINT_CHISEL_AND_HAMMER_ALTERNATE = ACTIONS.register("flint_chisel_and_hammer_alternate", () ->
            new ChiselAndHammerAlternate(new int[] {9, 15, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_FLINT_CHISEL_HIT).setExtraSound(SoundsNF.HAMMER_FLINT_CHISEL_HIT).setChargeState(1).setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistryObject<ChiselAndHammerCrawling> FLINT_CHISEL_AND_HAMMER_CRAWLING = ACTIONS.register("flint_chisel_and_hammer_crawling", () ->
            new ChiselAndHammerCrawling(new int[] {9, 3, 3, 4, 9}, new Action.Properties().setSound(SoundsNF.HAMMER_FLINT_CHISEL_HIT).setSprinting().setCrawling().setHarvestable(BlockTags.MINEABLE_WITH_PICKAXE)));

    public static final RegistryObject<VerticalSwing> MAUL_BASIC_1 = ACTIONS.register("maul_basic_1", () ->
            new VerticalSwing(STRIKING_SLASHING, HurtSphere.MAUL, 2, CombatUtil.STUN_VERY_LONG, new int[] {10, 6, 3, 10}, new Action.Properties().setChainTo(ActionsNF.MAUL_BASIC_2).setChainState(2).setKnockback(MAUL_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_MAUL)));
    public static final RegistryObject<VerticalSwing> MAUL_BASIC_2 = ACTIONS.register("maul_basic_2", () ->
            new VerticalSwing(STRIKING_SLASHING, HurtSphere.MAUL, 2, CombatUtil.STUN_VERY_LONG, new int[] {10, 6, 3, 10}, new Action.Properties().setChainFrom(MAUL_BASIC_1).setChainTo(ActionsNF.MAUL_BASIC_3).setChainState(2).setKnockback(MAUL_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_MAUL)));
    public static final RegistryObject<VerticalSwing> MAUL_BASIC_3 = ACTIONS.register("maul_basic_3", () ->
            new VerticalSwing(STRIKING_SLASHING, HurtSphere.MAUL, 2, CombatUtil.STUN_VERY_LONG, new int[] {10, 6, 3, 10}, new Action.Properties().setChainFrom(MAUL_BASIC_2).setChainTo(MAUL_BASIC_2).setChainState(2).setKnockback(MAUL_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_MAUL)));
    public static final RegistryObject<VerticalSwingCharged> MAUL_ALTERNATE_1 = ACTIONS.register("maul_alternate_1", () ->
            new VerticalSwingCharged(STRIKING_SLASHING, HurtSphere.MAUL, 2, CombatUtil.STUN_MAX, new int[] {32, 6, 4, 10}, new Action.Properties().setChargeState(0).setKnockback(MAUL_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setExtraSound(SoundsNF.HEAVY_BLADE_SWING_CHARGED).setHarvestable(TagsNF.MINEABLE_WITH_MAUL)));
    public static final RegistryObject<CrawlingSwing> MAUL_CRAWLING = ACTIONS.register("maul_crawling", () ->
            new CrawlingSwing(STRIKING_SLASHING, HurtSphere.MAUL, 2, CombatUtil.STUN_LONG, new int[] {10, 6, 3, 10}, new Action.Properties().setSprinting().setCrawling().setKnockback(MAUL_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setHarvestable(TagsNF.MINEABLE_WITH_MAUL)));

    //Techniques
    public static final RegistryObject<WeaponGuardAction> SWORD_GUARD = ACTIONS.register("sword_guard", () ->
            new WeaponGuardAction(new Action.Properties().setChargeState(0).setChainState(0).setChainTo(ActionsNF.SWORD_RIPOSTE).setSound(SoundsNF.WEAPON_BLOCK), 5, 5));
    public static final RegistryObject<Riposte> SWORD_RIPOSTE = ACTIONS.register("sword_riposte", () ->
            new Riposte(DamageType.SLASHING.asArray(), HurtSphere.SWORD, 2, CombatUtil.STUN_LONG, new int[] {5, 6, 3, 6}, new Action.Properties().setChainFrom(SWORD_GUARD).setKnockback(SWORD_KNOCKBACK).setImpact(Impact.HIGH).setSound(SoundsNF.BLADE_SWING_CHARGED)));
    public static final RegistryObject<WeaponGuardAction> SABRE_GUARD = ACTIONS.register("sabre_guard", () ->
            new WeaponGuardAction(new Action.Properties().setChargeState(0).setChainState(0).setChainTo(ActionsNF.SABRE_RIPOSTE).setSound(SoundsNF.WEAPON_BLOCK), 5, 5));
    public static final RegistryObject<Riposte> SABRE_RIPOSTE = ACTIONS.register("sabre_riposte", () ->
            new Riposte(DamageType.SLASHING.asArray(), HurtSphere.SABRE, 2, CombatUtil.STUN_LONG, new int[] {5, 6, 3, 6}, new Action.Properties().setChainFrom(SABRE_GUARD).setKnockback(SABRE_KNOCKBACK).setImpact(Impact.HIGH).setSound(SoundsNF.BLADE_SWING_CHARGED), bleeding(0.4F)));
    public static final RegistryObject<WeaponGuardAction> MACE_GUARD = ACTIONS.register("mace_guard", () ->
            new WeaponGuardAction(new Action.Properties().setChargeState(0).setChainState(0).setChainTo(ActionsNF.MACE_RIPOSTE).setSound(SoundsNF.WEAPON_BLOCK), 5, 5));
    public static final RegistryObject<Riposte> MACE_RIPOSTE = ACTIONS.register("mace_riposte", () ->
            new Riposte(DamageType.STRIKING.asArray(), HurtSphere.MACE, 1, CombatUtil.STUN_VERY_LONG, new int[] {6, 6, 3, 7}, new Action.Properties().setChainFrom(MACE_GUARD).setKnockback(MACE_KNOCKBACK).setImpact(Impact.HIGH).setSound(SoundsNF.SWING_CHARGED), bleeding(0.3F)));
    public static final RegistryObject<WeaponGuardAction> SICKLE_GUARD = ACTIONS.register("sickle_guard", () ->
            new WeaponGuardAction(new Action.Properties().setChargeState(0).setChainState(0).setChainTo(ActionsNF.SICKLE_RIPOSTE).setSound(SoundsNF.WEAPON_BLOCK), 5, 5));
    public static final RegistryObject<Riposte> SICKLE_RIPOSTE = ACTIONS.register("sickle_riposte", () ->
            new Riposte(PIERCING_SLASHING, HurtSphere.SICKLE, 2, CombatUtil.STUN_VERY_LONG, new int[] {6, 6, 3, 7}, new Action.Properties().setChainFrom(SICKLE_GUARD).setKnockback(SICKLE_KNOCKBACK).setImpact(Impact.HIGH).setSound(SoundsNF.BLADE_SWING_CHARGED), bleeding(0.35F)));

    public static final RegistryObject<ThrowSpearTechnique> SPEAR_THROW = ACTIONS.register("spear_throw", () ->
            new ThrowSpearTechnique(DamageType.PIERCING.asArray(), CombatUtil.STUN_MEDIUM, new int[] {8, 18, 5, 2, 6}, new Action.Properties().setChargeState(1).setKnockback(SPEAR_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.LONG_BLADE_SWING).setExtraSound(SoundsNF.LONG_BLADE_SWING_CHARGED),
                    2F, 0, false));
    public static final RegistryObject<ThrowKnifeTechnique> KNIFE_THROW = ACTIONS.register("knife_throw", () ->
            new ThrowKnifeTechnique(DamageType.PIERCING.asArray(), CombatUtil.STUN_SHORT, new int[] {8, 14, 5, 2, 5}, new Action.Properties().setChargeState(1).setKnockback(KNIFE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.SHORT_BLADE_SWING).setExtraSound(SoundsNF.SHORT_BLADE_SWING_CHARGED),
                    1.75F, 0, true));
    public static final RegistryObject<ThrowAxeTechnique> AXE_THROW = ACTIONS.register("axe_throw", () ->
            new ThrowAxeTechnique(DamageType.SLASHING.asArray(), CombatUtil.STUN_MEDIUM, new int[] {28, 4, 2, 6}, new Action.Properties().setChargeState(0).setKnockback(AXE_KNOCKBACK).setImpact(Impact.MEDIUM).setSound(SoundsNF.HEAVY_BLADE_SWING).setExtraSound(SoundsNF.HEAVY_BLADE_SWING_CHARGED),
                    1.6F, 80, true));

    public static final RegistryObject<HammerTechnique> HAMMER_UPSET = ACTIONS.register("hammer_upset", () ->
            new HammerTechnique(AnvilAction.UPSET, AnvilAction.UPSET_LINE, DamageType.STRIKING.asArray(), HurtSphere.HAMMER, 2, CombatUtil.STUN_MEDIUM, new int[] {24, 5, 4, 9}, new Action.Properties().setChargeState(0).setSound(SoundsNF.SWING).setKnockback(HAMMER_KNOCKBACK).setImpact(Impact.MEDIUM).setExtraSound(SoundsNF.SWING_CHARGED)));

    //Items
    public static final RegistryObject<FlintKnap> FLINT_KNAP = ACTIONS.register("flint_knap", () ->
            new FlintKnap(new int[] {7, 57, 7}, new Action.Properties().setChargeState(1)));
    public static final RegistryObject<BoneKnap> BONE_KNAP = ACTIONS.register("bone_knap", () ->
            new BoneKnap(new int[] {7, 57, 7}, new Action.Properties().setChargeState(1)));
    public static final RegistryObject<ShieldGuardAction> SHIELD_GUARD = ACTIONS.register("shield_guard", () ->
            new ShieldGuardAction(new Action.Properties().setChargeState(0).setChainState(0).setChainTo(ActionsNF.SHIELD_RIPOSTE).setSound(() -> SoundEvents.SHIELD_BLOCK), 5, 5));
    public static final RegistryObject<ShieldRiposte> SHIELD_RIPOSTE = ACTIONS.register("shield_riposte", () ->
            new ShieldRiposte(5F, DamageType.ABSOLUTE.asArray(), HurtSphere.SHIELD, 3, CombatUtil.STUN_VERY_LONG, new int[] {5, 4, 3, 6},
                    new Action.Properties().setChainFrom(SHIELD_GUARD).setKnockback(0.8F).setImpact(Impact.HIGH).setSound(SoundsNF.SWING_CHARGED)));
    public static final RegistryObject<SlingThrow> SLING_THROW = ACTIONS.register("sling_throw", () ->
            new SlingThrow(new Action.Properties().setChargeState(1).setSound(SoundsNF.SWING).setExtraSound(SoundsNF.SWING_CHARGED),
                    -0.25F, 3.1F, 8, 10 + 9 + 8 + 7, 4, 3, 7));
    public static final RegistryObject<BowShoot> BOW_SHOOT = ACTIONS.register("bow_shoot", () ->
            new BowShoot(new Action.Properties().setChargeState(1).setSound(SoundsNF.BOW_SHOOT).setExtraSound(SoundsNF.BOW_SHOOT_CHARGED),
                    -0.6F, 3.0F, 5, 20, 2, 5));
    public static final RegistryObject<BowShoot> TWISTED_BOW_SHOOT = ACTIONS.register("twisted_bow_shoot", () ->
            new BowShoot(new Action.Properties().setChargeState(1).setSound(SoundsNF.BOW_SHOOT).setExtraSound(SoundsNF.BOW_SHOOT_CHARGED),
                    -0.6F, 3.25F, 5, 24, 2, 5));
    public static final RegistryObject<WrapAction> FIBER_BANDAGE_USE = ACTIONS.register("fiber_bandage_use", () ->
            new WrapAction(new Action.Properties().setChargeState(0).setExtraSound(SoundsNF.BANDAGE_HEAL), 12, 5) {
                @Override
                public void onChargeRelease(LivingEntity user) {
                    super.onChargeRelease(user);
                    if(ActionTracker.get(user).isFullyCharged()) {
                        MobEffectInstance bleeding = user.getEffect(EffectsNF.BLEEDING.get());
                        if(bleeding != null) {
                            int duration = Math.max(0, bleeding.getDuration() - 30 * 20);
                            user.removeEffect(EffectsNF.BLEEDING.get());
                            if(duration > 0) user.addEffect(new MobEffectInstance(EffectsNF.BLEEDING.get(), duration));
                        }
                        Player player = (Player) user;
                        if(!player.getAbilities().instabuild) user.getItemInHand(PlayerData.get(player).getActiveHand()).shrink(1);
                    }
                }

                @Override
                public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
                    List<Component> tooltips = new ObjectArrayList<>();
                    tooltips.add(new TranslatableComponent("action.wrap").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
                    tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.fiber_bandage_use.info").withStyle(ChatFormatting.DARK_GREEN)));
                    return tooltips;
                }
            });
    public static final RegistryObject<WrapAction> BANDAGE_USE = ACTIONS.register("bandage_use", () ->
            new WrapAction(new Action.Properties().setChargeState(0).setExtraSound(SoundsNF.BANDAGE_HEAL), 12, 5) {
                @Override
                public void onChargeRelease(LivingEntity user) {
                    super.onChargeRelease(user);
                    if(ActionTracker.get(user).isFullyCharged()) {
                        if(user.hasEffect(EffectsNF.BLEEDING.get())) user.removeEffect(EffectsNF.BLEEDING.get());
                        user.heal(10F);
                        Player player = (Player) user;
                        if(!player.getAbilities().instabuild) user.getItemInHand(PlayerData.get(player).getActiveHand()).shrink(1);
                    }
                }

                @Override
                public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
                    List<Component> tooltips = new ObjectArrayList<>();
                    tooltips.add(new TranslatableComponent("action.wrap").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
                    tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.bandage_use.info_0").withStyle(ChatFormatting.DARK_GREEN)));
                    tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.bandage_use.info_1").withStyle(ChatFormatting.DARK_GREEN)));
                    return tooltips;
                }
            });
    public static final RegistryObject<WrapAction> MEDICINAL_BANDAGE_USE = ACTIONS.register("medicinal_bandage_use", () ->
            new WrapAction(new Action.Properties().setChargeState(0).setExtraSound(SoundsNF.BANDAGE_HEAL), 12, 5) {
                @Override
                public void onChargeRelease(LivingEntity user) {
                    super.onChargeRelease(user);
                    if(ActionTracker.get(user).isFullyCharged()) {
                        if(user.hasEffect(EffectsNF.BLEEDING.get())) user.removeEffect(EffectsNF.BLEEDING.get());
                        user.heal(10F);
                        user.addEffect(new MobEffectInstance(EffectsNF.BANDAGED.get(), 180 * 20));
                        Player player = (Player) user;
                        if(!player.getAbilities().instabuild) user.getItemInHand(PlayerData.get(player).getActiveHand()).shrink(1);
                    }
                }

                @Override
                public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
                    List<Component> tooltips = new ObjectArrayList<>();
                    tooltips.add(new TranslatableComponent("action.wrap").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
                    tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.bandage_use.info_0").withStyle(ChatFormatting.DARK_GREEN)));
                    tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.bandage_use.info_1").withStyle(ChatFormatting.DARK_GREEN)));
                    tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.medicinal_bandage_use.info").withStyle(ChatFormatting.DARK_GREEN)));
                    return tooltips;
                }
            });
    public static final RegistryObject<FirestarterShoot> FIRESTARTER_SHOOT = ACTIONS.register("firestarter_shoot", () -> new FirestarterShoot(
            new Action.Properties().setSound(SoundsNF.FIRESTARTER_STRIKE).setExtraSound(SoundsNF.FIRESTARTER_IGNITE), -0.25F, 6, 2, 4, 2, 6));
    public static final RegistryObject<FirestarterReload> FIRESTARTER_RELOAD = ACTIONS.register("firestarter_reload", () -> new FirestarterReload(
            new Action.Properties().setSound(SoundsNF.FIRESTARTER_RELOAD), -0.25F, 8, 16, 8));

    //NPCs
    public static final RegistryObject<DeerGraze> DEER_GRAZE = ACTIONS.register("deer_graze", () ->
            new DeerGraze(new Action.Properties().setChargeState(1), 8, 1, 10));

    public static final RegistryObject<WolfGrowl> WOLF_GROWL = ACTIONS.register("wolf_growl", () ->
            new WolfGrowl(new Action.Properties().setChargeState(1).setSound(SoundsNF.WOLF_GROWL), 5, 1, 5));
    public static final RegistryObject<WolfBite> WOLF_BITE = ACTIONS.register("wolf_bite", () ->
            new WolfBite(26F, DamageType.PIERCING.asArray(), HurtSphere.WOLF_BITE, 1, CombatUtil.STUN_MEDIUM,
                    new int[] {9, 6, 1, 8}, new Action.Properties().setImpact(Impact.MEDIUM).setSound(SoundsNF.WOLF_ATTACK).setChainTo(ActionsNF.WOLF_BITE_CHAIN).setChainState(2), bleeding(0.3F)));
    public static final RegistryObject<WolfBiteChain> WOLF_BITE_CHAIN = ACTIONS.register("wolf_bite_chain", () ->
            new WolfBiteChain(26F, DamageType.PIERCING.asArray(), HurtSphere.WOLF_BITE, 1, CombatUtil.STUN_MEDIUM,
                    new int[] {3, 6, 8}, new Action.Properties().setImpact(Impact.MEDIUM).setSound(SoundsNF.WOLF_ATTACK).setChainFrom(WOLF_BITE), bleeding(0.3F)));

    public static final RegistryObject<Action> DRAKEFOWL_BREED = ACTIONS.register("drakefowl_breed", () -> new Action(new Action.Properties().setSpecial(), 20));
    public static final RegistryObject<DrakefowlCollapse> DRAKEFOWL_COLLAPSE = ACTIONS.register("drakefowl_collapse", () ->
            new DrakefowlCollapse(new Action.Properties().setChargeState(1), 9, 1, 12));
    public static final RegistryObject<DrakefowlClaw> DRAKEFOWL_CLAW = ACTIONS.register("drakefowl_claw", () ->
            new DrakefowlClaw(15F, DamageType.SLASHING.asArray(), HurtSphere.DRAKEFOWL_CLAW, 1, CombatUtil.STUN_SHORT,
                    new int[] {8, 4, 4}, new Action.Properties().setImpact(Impact.LOW).setSpecial()));
    public static final RegistryObject<DrakefowlSpit> DRAKEFOWL_SPIT = ACTIONS.register("drakefowl_spit", () ->
            new DrakefowlSpit(new Action.Properties().setSound(SoundsNF.DRAKEFOWL_SPIT), 12, 5, 8));

    public static final RegistryObject<MerborBreed> MERBOR_BREED = ACTIONS.register("merbor_breed", () -> new MerborBreed(new Action.Properties(), 12, 8));
    public static final RegistryObject<MerborCollapse> MERBOR_COLLAPSE = ACTIONS.register("merbor_collapse", () ->
            new MerborCollapse(new Action.Properties().setChargeState(1), 12, 1, 14));
    public static final RegistryObject<MerborGore> MERBOR_GORE = ACTIONS.register("merbor_gore", () ->
            new MerborGore(28F, DamageType.PIERCING.asArray(), HurtSphere.MERBOR_TUSK, 2, CombatUtil.STUN_LONG,
                    new int[] {9, 5, 8}, new Action.Properties().setImpact(Impact.HIGH).setKnockback(0.7F).setSound(SoundsNF.MERBOR_ATTACK), bleeding(1F)));

    public static final RegistryObject<HuskRightSwipe> HUSK_RIGHT_SWIPE_1 = ACTIONS.register("husk_right_swipe_1", () ->
            new HuskRightSwipe(10, DamageType.STRIKING.asArray(), HurtSphere.HUSK_ARM, 2, CombatUtil.STUN_SHORT, new int[] {7, 1, 6, 2, 7},
                    new Action.Properties().setChainTo(ActionsNF.HUSK_RIGHT_SWIPE_2).setChainState(3).setKnockback(0.25F).setImpact(Impact.MEDIUM).setSound(SoundsNF.HUSK_SWING)));
    public static final RegistryObject<HuskLeftSwipe> HUSK_RIGHT_SWIPE_2 = ACTIONS.register("husk_right_swipe_2", () ->
            new HuskLeftSwipe(10, DamageType.STRIKING.asArray(), HurtSphere.HUSK_ARM, 2, CombatUtil.STUN_SHORT, new int[] {6, 1, 6, 2, 7},
                    new Action.Properties().setChainFrom(HUSK_RIGHT_SWIPE_1).setKnockback(0.25F).setImpact(Impact.MEDIUM).setSound(SoundsNF.HUSK_SWING)));
    public static final RegistryObject<HuskLeftSwipe> HUSK_LEFT_SWIPE_1 = ACTIONS.register("husk_left_swipe_1", () ->
            new HuskLeftSwipe(10, DamageType.STRIKING.asArray(), HurtSphere.HUSK_ARM, 2, CombatUtil.STUN_SHORT, new int[] {7, 1, 6, 2, 7},
                    new Action.Properties().setChainTo(ActionsNF.HUSK_LEFT_SWIPE_2).setChainState(3).setKnockback(0.25F).setImpact(Impact.MEDIUM).setSound(SoundsNF.HUSK_SWING)));
    public static final RegistryObject<HuskRightSwipe> HUSK_LEFT_SWIPE_2 = ACTIONS.register("husk_left_swipe_2", () ->
            new HuskRightSwipe(10, DamageType.STRIKING.asArray(), HurtSphere.HUSK_ARM, 2, CombatUtil.STUN_SHORT, new int[] {6, 1, 6, 2, 7},
                    new Action.Properties().setChainFrom(HUSK_LEFT_SWIPE_1).setKnockback(0.25F).setImpact(Impact.MEDIUM).setSound(SoundsNF.HUSK_SWING)));
    public static final RegistryObject<HuskOverhead> HUSK_OVERHEAD = ACTIONS.register("husk_overhead", () ->
            new HuskOverhead(15, DamageType.STRIKING.asArray(), HurtSphere.HUSK_ARM, 2, CombatUtil.STUN_MEDIUM, new int[] {8, 2, 7, 8},
                    new Action.Properties().setKnockback(0.4F).setImpact(Impact.HIGH).setSound(SoundsNF.HUSK_SWING)));
    public static final RegistryObject<HuskOverhead> HUSK_MINE = ACTIONS.register("husk_mine", () ->
            new HuskOverhead(15, DamageType.STRIKING.asArray(), HurtSphere.HUSK_ARM, 2, CombatUtil.STUN_MEDIUM, new int[] {8, 2, 7, 8},
                    new Action.Properties().setKnockback(0.4F).setImpact(Impact.HIGH).setSound(SoundsNF.HUSK_SWING)));

    public static final RegistryObject<SkeletonThrust> SKELETON_THRUST = ACTIONS.register("skeleton_thrust", () ->
            new SkeletonThrust(6, DamageType.STRIKING.asArray(), HurtSphere.SKELETON_ARM, 1, CombatUtil.STUN_SHORT, new int[] {7, 5, 2, 7},
                    new Action.Properties().setKnockback(0.15F).setImpact(Impact.MEDIUM).setSound(SoundsNF.SKELETON_THRUST)));
    public static final RegistryObject<SkeletonShoot> SKELETON_SHOOT = ACTIONS.register("skeleton_shoot", () ->
            new SkeletonShoot(new Action.Properties().setChargeState(2).setSound(SoundsNF.SKELETON_BOW_SHOOT), 9, 20, 40, 8));

    public static final RegistryObject<DregBuff> DREG_BUFF = ACTIONS.register("dreg_buff", () ->
            new DregBuff(new Action.Properties().setChargeState(1), 10, 10, 8));
    public static final RegistryObject<DregResurrect> DREG_RESURRECT = ACTIONS.register("dreg_resurrect", () ->
            new DregResurrect(new Action.Properties(), 10, 20, 8));
    public static final RegistryObject<DregBuild> DREG_BUILD = ACTIONS.register("dreg_build", () ->
            new DregBuild(new Action.Properties(), 10, 6, 10));
    public static final RegistryObject<DregCower> DREG_COWER = ACTIONS.register("dreg_cower", () ->
            new DregCower(new Action.Properties().setChargeState(1), 10, 10, 8));

    public static final RegistryObject<CockatriceBite> COCKATRICE_BITE = ACTIONS.register("cockatrice_bite", () ->
            new CockatriceBite(22F, DamageType.PIERCING.asArray(), HurtSphere.COCKATRICE_BITE, 1, CombatUtil.STUN_MEDIUM,
                    new int[] {10, 6, 9}, new Action.Properties().setImpact(Impact.MEDIUM).setSound(SoundsNF.COCKATRICE_BITE)));
    public static final RegistryObject<CockatriceClaw> COCKATRICE_CLAW = ACTIONS.register("cockatrice_claw", () ->
            new CockatriceClaw(26F, DamageType.SLASHING.asArray(), HurtSphere.COCKATRICE_CLAW, 1, CombatUtil.STUN_LONG,
                    new int[] {9, 4, 6}, new Action.Properties().setKnockback(0.3F).setImpact(Impact.MEDIUM)));
    public static final RegistryObject<CockatriceSpit> COCKATRICE_SPIT = ACTIONS.register("cockatrice_spit", () ->
            new CockatriceSpit(new Action.Properties().setSound(SoundsNF.COCKATRICE_SPIT), 14, 6, 9));

    public static final RegistryObject<SpiderBite> SPIDER_BITE_STRONG = ACTIONS.register("spider_bite_strong", () ->
            new SpiderBite(22F, DamageType.PIERCING.asArray(), HurtSphere.SPIDER_BITE, 1, CombatUtil.STUN_LONG,
                    new int[] {9, 5, 8}, new Action.Properties().setImpact(Impact.MEDIUM).setSound(SoundsNF.SPIDER_BITE)));
    public static final RegistryObject<SpiderBite> SPIDER_BITE_PARALYZING = ACTIONS.register("spider_bite_paralyzing", () ->
            new SpiderBite(18F, DamageType.PIERCING.asArray(), HurtSphere.SPIDER_BITE, 1, CombatUtil.STUN_MEDIUM,
                    new int[] {9, 5, 8}, new Action.Properties().setImpact(Impact.LOW).setSound(SoundsNF.SPIDER_BITE), new AttackEffect(EffectsNF.PARALYSIS, 18 * 20, 0, 1)));
    public static final RegistryObject<SpiderBite> SPIDER_BITE_POISONOUS = ACTIONS.register("spider_bite_poisonous", () ->
            new SpiderBite(18F, DamageType.PIERCING.asArray(), HurtSphere.SPIDER_BITE, 1, CombatUtil.STUN_MEDIUM,
                    new int[] {9, 5, 8}, new Action.Properties().setImpact(Impact.LOW).setSound(SoundsNF.SPIDER_BITE), new AttackEffect(EffectsNF.POISON, 10 * 20, 0, 1)));

    public static final RegistryObject<RockwormRetreat> ROCKWORM_RETREAT = ACTIONS.register("rockworm_retreat", () ->
            new RockwormRetreat(new Action.Properties().setSound(SoundsNF.ROCKWORM_BURROW), 6));
    public static final RegistryObject<RockwormEmerge> ROCKWORM_EMERGE = ACTIONS.register("rockworm_emerge", () ->
            new RockwormEmerge(new Action.Properties().setSound(SoundsNF.ROCKWORM_EMERGE), 6));
    public static final RegistryObject<RockwormBite> ROCKWORM_BITE = ACTIONS.register("rockworm_bite", () ->
            new RockwormBite(35F, DamageType.PIERCING.asArray(), HurtSphere.ROCKWORM_BITE, 2, 6,
                    new int[] {5, 7}, new Action.Properties().setKnockback(-1F).setImpact(Impact.HIGH).setSound(SoundsNF.ROCKWORM_BITE)));

    public static final RegistryObject<PitDevilGrowl> PIT_DEVIL_GROWL = ACTIONS.register("pit_devil_growl", () ->
            new PitDevilGrowl(new Action.Properties().setChargeState(1).setSound(SoundsNF.PIT_DEVIL_GROWL), 4, 1, 4));
    public static final RegistryObject<PitDevilBite> PIT_DEVIL_BITE = ACTIONS.register("pit_devil_bite", () ->
            new PitDevilBite(35F, PIERCING_STRIKING, HurtSphere.PIT_DEVIL_BITE, 2, CombatUtil.STUN_MEDIUM,
                    new int[] {9, 6, 8}, new Action.Properties().setImpact(Impact.MEDIUM).setSound(SoundsNF.PIT_DEVIL_BITE), bleeding(0.5F)));

    public static final RegistryObject<EctoplasmClubLarge> ECTOPLASM_CLUB_LARGE = ACTIONS.register("ectoplasm_club_large", () ->
            new EctoplasmClubLarge(40F, DamageType.STRIKING.asArray(), HurtSphere.ECTOPLASM_CLUB, 2, CombatUtil.STUN_LONG,
                    new int[] {16, 7, 16}, new Action.Properties().setImpact(Impact.HIGH).setSound(SoundsNF.ECTOPLASM_ATTACK)));
    public static final RegistryObject<EctoplasmClubMedium> ECTOPLASM_CLUB_MEDIUM = ACTIONS.register("ectoplasm_club_medium", () ->
            new EctoplasmClubMedium(25F, DamageType.STRIKING.asArray(), HurtSphere.ECTOPLASM_CLUB, 1, CombatUtil.STUN_MEDIUM,
                    new int[] {14, 7, 14}, new Action.Properties().setImpact(Impact.MEDIUM).setSound(SoundsNF.ECTOPLASM_ATTACK)));
    public static final RegistryObject<EctoplasmExplode> ECTOPLASM_EXPLODE_LARGE = ACTIONS.register("ectoplasm_explode_large", () ->
            new EctoplasmExplode(new Action.Properties().setImpact(Impact.MAXIMUM).setSound(SoundsNF.ESSENCE_EXPLODE),
                    50D/16D, 250F / (1.5F * 5F), 1.1F, CombatUtil.STUN_VERY_LONG, 22, 2));
    public static final RegistryObject<EctoplasmExplode> ECTOPLASM_EXPLODE_MEDIUM = ACTIONS.register("ectoplasm_explode_medium", () ->
            new EctoplasmExplode(new Action.Properties().setImpact(Impact.HIGH).setSound(SoundsNF.ESSENCE_EXPLODE),
                    35D/16D, 150F / (1.5F * 5F), 0.8F, CombatUtil.STUN_LONG, 20, 2));

    public static void register() {
        ACTIONS.register(Nightfall.MOD_EVENT_BUS);
    }

    public static void init() {
        for(RegistryObject<Action> action : ACTIONS.getEntries()) action.get().init();
    }

    public static boolean isEmpty(ResourceLocation id) {
        if(id == null) return false;
        return id.equals(EMPTY.getId());
    }

    public static Action get(ResourceLocation id) {
        return RegistriesNF.getActions().getValue(id);
    }

    public static boolean isTagged(Action action, TagKey<Action> tag) {
        return RegistriesNF.getActions().getHolder(action).get().is(tag);
    }

    public static AttackEffect bleeding(int duration, float chance) {
        return new AttackEffect(EffectsNF.BLEEDING, duration, 0, chance);
    }

    public static AttackEffect bleeding(float chance) {
        return bleeding(30 * 20, chance);
    }

    public static AttackEffect poison(int duration, float chance) {
        return new AttackEffect(EffectsNF.POISON, duration, 0, chance);
    }
}
