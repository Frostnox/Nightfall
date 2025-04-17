package frostnox.nightfall.registry.forge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.fluid.LavaFluidNF;
import frostnox.nightfall.block.fluid.MetalFluid;
import frostnox.nightfall.block.fluid.WaterFluidNF;
import frostnox.nightfall.util.DataUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FluidsNF {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Nightfall.MODID);

    public static final RegistryObject<WaterFluidNF> WATER = FLUIDS.register("water", () -> new WaterFluidNF.Source(FluidsNF.WATER_PROPERTIES));
    public static final RegistryObject<WaterFluidNF> WATER_FLOWING = FLUIDS.register("water_flowing", () -> new WaterFluidNF.Flowing(FluidsNF.WATER_PROPERTIES));
    public static final ForgeFlowingFluid.Properties WATER_PROPERTIES = new ForgeFlowingFluid.Properties(() -> WATER.get(), () -> WATER_FLOWING.get(),
            FluidAttributes.Water.builder(still("water"), flow("water")).color(0xFF273752).overlay(overlay("water")).temperature(20).
                    sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)).slopeFindDistance(3).levelDecreasePerBlock(1).canMultiply().
            block(() -> BlocksNF.WATER.get()).explosionResistance(100).tickRate(4);

    public static final RegistryObject<WaterFluidNF> SEAWATER = FLUIDS.register("seawater", () -> new WaterFluidNF.Source(FluidsNF.SEAWATER_PROPERTIES));
    public static final RegistryObject<WaterFluidNF> SEAWATER_FLOWING = FLUIDS.register("seawater_flowing", () -> new WaterFluidNF.Flowing(FluidsNF.SEAWATER_PROPERTIES));
    public static final ForgeFlowingFluid.Properties SEAWATER_PROPERTIES = new ForgeFlowingFluid.Properties(() -> SEAWATER.get(), () -> SEAWATER_FLOWING.get(),
            FluidAttributes.builder(still("seawater"), flow("seawater")).overlay(overlay("seawater")).temperature(20).
                    sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)).slopeFindDistance(3).levelDecreasePerBlock(1).canMultiply().
            block(() -> BlocksNF.SEAWATER.get()).explosionResistance(100).tickRate(4);

    public static final RegistryObject<LavaFluidNF> LAVA = FLUIDS.register("lava", () -> new LavaFluidNF.Source(FluidsNF.LAVA_PROPERTIES));
    public static final RegistryObject<LavaFluidNF> LAVA_FLOWING = FLUIDS.register("lava_flowing", () -> new LavaFluidNF.Flowing(FluidsNF.LAVA_PROPERTIES));
    public static final ForgeFlowingFluid.Properties LAVA_PROPERTIES = new ForgeFlowingFluid.Properties(() -> LAVA.get(), () -> LAVA_FLOWING.get(),
            FluidAttributes.builder(still("lava"), flow("lava")).overlay(null).temperature(1000).
            sound(SoundEvents.BUCKET_FILL_LAVA, SoundEvents.BUCKET_EMPTY_LAVA)).slopeFindDistance(2).levelDecreasePerBlock(1).
            block(() -> BlocksNF.LAVA.get()).explosionResistance(100).tickRate(24);

    //Recipe fluids (do not exist as blocks, mainly used as FluidStacks)
    public static final ResourceLocation METAL_STILL = still("metal");
    public static final ResourceLocation METAL_FLOW = flow("metal");
    public static final ResourceLocation METAL_SOLID = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/metal");
    public static final Map<Metal, RegistryObject<MetalFluid>> METAL = DataUtil.mapEnum(Metal.class, (metal) ->
            FLUIDS.register(metal.getName(), () -> new MetalFluid.Source(FluidsNF.METAL_PROPERTIES.get(metal))));
    public static final Map<Metal, RegistryObject<MetalFluid>> METAL_FLOWING = DataUtil.mapEnum(Metal.class, (metal) ->
            FLUIDS.register(metal.getName() + "_flowing", () -> new MetalFluid.Flowing(FluidsNF.METAL_PROPERTIES.get(metal))));
    public static final Map<Metal, MetalFluid.MetalProperties> METAL_PROPERTIES = DataUtil.mapEnum(Metal.class, (metal) ->
            new MetalFluid.MetalProperties(FluidsNF.METAL.get(metal), FluidsNF.METAL_FLOWING.get(metal), metal,
                    FluidAttributes.builder(METAL_STILL, METAL_FLOW).temperature((int) metal.getMeltTemp()).color(metal.getColor().getRGB())));

    private static final Map<Pair<Item, Fluid>, Item> bucketFluids = new HashMap<>(7);
    private static final BiMap<Fluid, Item> itemFluids = HashBiMap.create(2);

    private static ResourceLocation overlay(String name) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + name + "_overlay");
    }

    private static ResourceLocation still(String name) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + name + "_still");
    }

    private static ResourceLocation flow(String name) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + name + "_flow");
    }

    public static void register() {
        FLUIDS.register(Nightfall.MOD_EVENT_BUS);
    }

    public static void init() {
        registerBucket(ItemsNF.WOODEN_BUCKET.get(), WATER.get(), ItemsNF.WOODEN_WATER_BUCKET.get());
        registerBucket(ItemsNF.WOODEN_BUCKET.get(), SEAWATER.get(), ItemsNF.WOODEN_SEAWATER_BUCKET.get());
        registerBucket(ItemsNF.BRONZE_BUCKET.get(), WATER.get(), ItemsNF.BRONZE_WATER_BUCKET.get());
        registerBucket(ItemsNF.BRONZE_BUCKET.get(), SEAWATER.get(), ItemsNF.BRONZE_SEAWATER_BUCKET.get());
        registerBucket(ItemsNF.ALKIMIUM_BUCKET.get(), WATER.get(), ItemsNF.ALKIMIUM_WATER_BUCKET.get());
        registerBucket(ItemsNF.ALKIMIUM_BUCKET.get(), SEAWATER.get(), ItemsNF.ALKIMIUM_SEAWATER_BUCKET.get());
        registerBucket(ItemsNF.ALKIMIUM_BUCKET.get(), LAVA.get(), ItemsNF.ALKIMIUM_LAVA_BUCKET.get());
        registerItem(WATER.get(), ItemsNF.WATER.get());
        registerItem(SEAWATER.get(), ItemsNF.SEAWATER.get());
    }

    public static void registerBucket(Item emptyBucket, Fluid fluid, Item filledBucket) {
        bucketFluids.put(new Pair<>(emptyBucket, fluid), filledBucket);
    }

    public static ItemStack getFilledBucket(Item emptyBucket, Fluid fluid) {
        Pair<Item, Fluid> key = new Pair<>(emptyBucket, fluid);
        if(!bucketFluids.containsKey(key)) return ItemStack.EMPTY;
        else return new ItemStack(bucketFluids.get(key));
    }

    public static void registerItem(Fluid fluid, Item item) {
        itemFluids.put(fluid, item);
    }

    public static @Nullable Item getAsItem(Fluid fluid) {
        return itemFluids.get(fluid);
    }

    public static Fluid getAsFluid(Item item) {
        return itemFluids.inverse().getOrDefault(item, Fluids.EMPTY);
    }
}
