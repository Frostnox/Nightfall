package frostnox.nightfall.block.block.crucible;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.MenuContainerBlockEntity;
import frostnox.nightfall.block.block.mold.ItemMoldBlockEntity;
import frostnox.nightfall.block.fluid.MetalFluid;
import frostnox.nightfall.data.recipe.CrucibleRecipe;
import frostnox.nightfall.item.ItemStackHandlerNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.DataUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CrucibleBlockEntity extends MenuContainerBlockEntity implements MenuProvider, IHoldable {
    public static final int ITEM_CAPACITY = 4;
    protected final ItemStackHandlerNF inventory;
    public final List<FluidStack> fluids = new ObjectArrayList<>(ITEM_CAPACITY);
    protected final List<ResourceLocation> recipeLocations = new ObjectArrayList<>(new ResourceLocation[ITEM_CAPACITY]);
    public final IntList cookTicks = new IntArrayList(new int[ITEM_CAPACITY]);
    public final IntList cookDurations = new IntArrayList(IntStream.generate(() -> 1).limit(ITEM_CAPACITY).toArray());
    protected float temperature, targetTemperature;
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return pIndex < ITEM_CAPACITY ? cookTicks.getInt(pIndex) : cookDurations.getInt(pIndex - ITEM_CAPACITY);
        }

        @Override
        public void set(int pIndex, int pValue) {
            if(pIndex < ITEM_CAPACITY) cookTicks.set(pIndex, pValue);
            else cookDurations.set(pIndex - ITEM_CAPACITY, pValue);
        }

        @Override
        public int getCount() {
            return ITEM_CAPACITY * 2;
        }
    };

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesNF.CRUCIBLE.get(), pos, state);
        this.inventory = new ItemStackHandlerNF(ITEM_CAPACITY) {
            @Override
            protected void onContentsChanged(int slot) {
                if(level == null || level.isClientSide()) return;
                setChanged();
                ItemStack item = inventory.getStackInSlot(slot);
                Optional<CrucibleRecipe> newRecipe = level.getRecipeManager().getRecipeFor(CrucibleRecipe.TYPE,
                        new RecipeWrapper(new ItemStackHandlerNF(item)), level);
                recipeLocations.set(slot, newRecipe.isPresent() ? newRecipe.get().getId() : null);
                cookTicks.set(slot, 0);
                cookDurations.set(slot, newRecipe.isPresent() ? (newRecipe.get().getCookTime() * item.getCount()) : 1);
            }
        };
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent(Nightfall.MODID + ".crucible");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new CrucibleContainer(pContainerId, pInventory, this);
    }

    @Override
    public void startOpen(Player pPlayer) {
        level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.625, worldPosition.getZ() + 0.5,
                SoundsNF.CERAMIC_OPEN_SMALL.get(), SoundSource.BLOCKS, 1F, 0.94F + level.random.nextFloat() * 0.12F);
        level.gameEvent(pPlayer, GameEvent.CONTAINER_OPEN, worldPosition);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        DataUtil.loadSlottedData(tag.getList("recipes", ListTag.TAG_COMPOUND), recipeLocations, (t) -> ResourceLocation.parse(t.getString("recipe")));
        fluids.clear();
        DataUtil.loadFluids(fluids, tag);
        DataUtil.loadSlottedData(tag.getList("cookTicks", ListTag.TAG_COMPOUND), cookTicks, (t) -> t.getInt("ticks"));
        DataUtil.loadSlottedData(tag.getList("cookDurations", ListTag.TAG_COMPOUND), cookDurations, (t) -> t.getInt("duration"));
        temperature = tag.getFloat("temperature");
        targetTemperature = tag.getFloat("targetTemperature");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());
        tag.put("recipes", DataUtil.writeSlottedData(recipeLocations, (t, r) -> t.putString("recipe", r.toString())));
        DataUtil.writeFluids(fluids, tag);
        tag.put("cookTicks", DataUtil.writeSlottedData(cookTicks, (t, i) -> t.putInt("ticks", i), (i) -> i == 0));
        tag.put("cookDurations", DataUtil.writeSlottedData(cookDurations, (t, i) -> t.putInt("duration", i), (i) -> i == 1));
        tag.putFloat("temperature", temperature);
        tag.putFloat("targetTemperature", targetTemperature);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        if(!fluids.isEmpty()) DataUtil.writeFluids(fluids, tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if(pkt.getTag() != null) handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        fluids.clear();
        DataUtil.loadFluids(fluids, tag);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity entity) {
        boolean changed = false;
        float targetTemp = entity.targetTemperature;
        if(level.isRainingAt(pos.above())) targetTemp -= 200;
        if(entity.temperature < targetTemp) {
            entity.temperature = Math.min(entity.temperature + 2, targetTemp);
            TieredHeat heat = TieredHeat.fromTemp(entity.temperature);
            if(state.getValue(CrucibleBlock.HEAT) != heat.getTier()) {
                level.setBlock(pos, state.setValue(CrucibleBlock.HEAT, heat.getTier()), 2);
            }
            changed = true;
        }
        else if(entity.temperature > targetTemp) {
            entity.temperature = Math.max(entity.temperature - 1, targetTemp);
            TieredHeat heat = TieredHeat.fromTemp(entity.temperature);
            if(state.getValue(CrucibleBlock.HEAT) != heat.getTier()) {
                level.setBlock(pos, state.setValue(CrucibleBlock.HEAT, heat.getTier()), 2);
            }
            changed = true;
        }
        for(int i = 0; i < ITEM_CAPACITY; i++) {
            ResourceLocation recipeLocation = entity.recipeLocations.get(i);
            if(recipeLocation != null) {
                CrucibleRecipe recipe = (CrucibleRecipe) level.getRecipeManager().byKey(recipeLocation).orElseThrow();
                if(entity.temperature >= recipe.getTemperature()) {
                    int cookTicks = entity.cookTicks.getInt(i) + 1;
                    entity.cookTicks.set(i, cookTicks);
                    changed = true;
                    if(cookTicks >= entity.cookDurations.getInt(i)) {
                        RecipeWrapper inventory = new RecipeWrapper(new ItemStackHandlerNF(entity.inventory.getStackInSlot(i)));
                        FluidStack fluid = recipe.assembleFluid(inventory);
                        if(fluid.isEmpty() || entity.getFluidUnits() != entity.getFluidCapacity(entity.getBlockState())) {
                            entity.addFluid(fluid, Integer.MAX_VALUE);
                            entity.setItem(i, recipe.assemble(inventory));
                            entity.tryAlloying();
                            level.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 4 | 16);
                        }
                    }
                }
                else {
                    int cookTicks = entity.cookTicks.getInt(i);
                    if(cookTicks > 0) {
                        entity.cookTicks.set(i, cookTicks - 1);
                        changed = true;
                    }
                }
            }
        }
        if(changed) entity.setChanged();
    }

    public int getFluidUnits() {
        int amount = 0;
        for(FluidStack fluid : fluids) amount += fluid.getAmount();
        return amount;
    }

    public boolean addFluid(FluidStack newFluid, int maxAmount) {
        int units = getFluidUnits();
        int amount = Math.min(maxAmount, Math.min(getFluidCapacity(getBlockState()) - units, newFluid.getAmount()));
        if(amount > 0) {
            for(FluidStack fluid : fluids) {
                if(fluid.isFluidEqual(newFluid)) {
                    fluid.grow(amount);
                    newFluid.shrink(amount);
                    return true;
                }
            }
            fluids.add(new FluidStack(newFluid.getFluid(), newFluid.getAmount()));
            newFluid.setAmount(0);
            setChanged();
            return true;
        }
        return false;
    }

    public void removeEmptyFluids() {
        fluids.removeIf(FluidStack::isEmpty);
        setChanged();
    }

    public void tryAlloying() {
        for(Fluid fluid : ForgeRegistries.FLUIDS) {
            if(fluid instanceof MetalFluid metalFluid && metalFluid.metal.canCreateFromFluids(fluids, temperature)) {
                int units = getFluidUnits();
                fluids.clear();
                fluids.add(new FluidStack(fluid, units));
                setChanged();
            }
        }
    }

    public FluidStack getRecentMetalFluid() {
        for(int i = fluids.size() - 1; i >= 0; i--) {
            FluidStack fluid = fluids.get(i);
            if(fluid.getFluid() instanceof MetalFluid) return fluid;
        }
        return FluidStack.EMPTY;
    }

    public ItemStackHandlerNF getInventory() {
        return inventory;
    }

    public int getFluidCapacity(BlockState state) {
        return ((CrucibleBlock) state.getBlock()).fluidCapacity;
    }

    public void emptyInventory() {
        for(int i = 0; i < ITEM_CAPACITY; i++) inventory.setStackInSlot(i, ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> getContainerDrops() {
        NonNullList<ItemStack> list = NonNullList.create();
        for(int i = 0; i < ITEM_CAPACITY; i++) list.add(inventory.getStackInSlot(i));
        return list;
    }

    @Override
    public CompoundTag writeDataAndClear() {
        targetTemperature = 0;
        CompoundTag data = this.saveWithId();
        data.putInt("state", Block.getId(getBlockState().setValue(CrucibleBlock.AXIS, Direction.Axis.Z)));
        emptyInventory();
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        NonNullList<ItemStack> drops = this.getContainerDrops();
        drops.add(new ItemStack(this.getBlockState().getBlock().asItem()));
        if(level != null) Containers.dropContents(level, pos, drops);
    }

    @Override
    public boolean heldUse(BlockPos usePos, Player player) {
        FluidStack fluid = getRecentMetalFluid();
        if(fluid.getFluid() instanceof MetalFluid metalFluid && temperature >= metalFluid.metal.getMeltTemp()) {
            BlockEntity blockEntity = player.level.getBlockEntity(usePos);
            boolean poured = false;
            if(blockEntity instanceof ItemMoldBlockEntity mold) {
                if(mold.addFluid(fluid, metalFluid.metal.getMeltTemp())) poured = true;
            }
            else if(blockEntity instanceof CrucibleBlockEntity crucible) {
                if(crucible.addFluid(fluid, 10)) {
                    poured = true;
                    player.level.sendBlockUpdated(crucible.getBlockPos(), crucible.getBlockState(), crucible.getBlockState(), 4 | 16);
                }
            }
            if(poured) {
                removeEmptyFluids();
                tryAlloying();
                player.level.playSound(null, player, SoundsNF.CRUCIBLE_POUR.get(), SoundSource.PLAYERS, 0.75F, 1F);
            }
            return true;
        }
        return false;
    }

    @Override
    public double getFirstPersonYOffset() {
        return 2D/16D;
    }

    @Override
    public double getThirdPersonYOffset() {
        return -3D/16D;
    }
}
