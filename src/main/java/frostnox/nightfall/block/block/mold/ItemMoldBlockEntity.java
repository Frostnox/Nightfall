package frostnox.nightfall.block.block.mold;

import frostnox.nightfall.block.IDropsItems;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.fluid.MetalFluid;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class ItemMoldBlockEntity extends BlockEntity implements IHoldable, IDropsItems {
    protected FluidStack inputFluid = FluidStack.EMPTY;
    protected float temperature;

    public ItemMoldBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.ITEM_MOLD.get(), pos, state);
    }

    protected ItemMoldBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inputFluid = FluidStack.loadFluidStackFromNBT(tag);
        temperature = tag.getFloat("temperature");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(!inputFluid.isEmpty()) inputFluid.writeToNBT(tag);
        tag.putFloat("temperature", temperature);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ItemMoldBlockEntity entity) {
        if(entity.temperature > 0F) entity.temperature -= 0.5F;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemMoldBlockEntity entity) {
        serverTick(level, pos, state, entity, 1);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemMoldBlockEntity entity, int ticks) {
        if(entity.temperature > 0F) {
            TieredHeat oldHeat = TieredHeat.fromTemp(entity.temperature);
            entity.temperature = Math.max(0, entity.temperature - 0.5F * ticks);
            if(ticks > 1 || oldHeat != TieredHeat.fromTemp(entity.temperature)) level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    public FluidStack getInputFluid() {
        return inputFluid;
    }

    public boolean isEmpty() {
        return inputFluid.isEmpty();
    }

    public boolean isFull() {
        return inputFluid.getAmount() >= getMaxUnits();
    }

    public boolean isCool() {
        return temperature < TieredHeat.RED.getBaseTemp();
    }

    public float getTemperature() {
        return temperature;
    }

    public int getMaxUnits() {
        return ((ItemMoldBlock) getBlockState().getBlock()).maxUnits;
    }

    public ItemStack getCastItem() {
        if(isCool() && isFull() && inputFluid.getFluid() instanceof MetalFluid metalFluid) {
            return new ItemStack(metalFluid.metal.getMatchingItem(((ItemMoldBlock) getBlockState().getBlock()).matchingItemTag));
        }
        else return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getContainerDrops() {
        NonNullList<ItemStack> list = NonNullList.create();
        list.add(getCastItem());
        return list;
    }

    /**
     * Transfers at most 10 units from the provided fluid to the mold
     * @return true if fluid was added to the mold, false if not
     */
    public boolean addFluid(FluidStack fluid, float temperature) {
        boolean empty = inputFluid.isEmpty();
        if(empty || inputFluid.getFluid().isSame(fluid.getFluid())) {
            if(inputFluid.getAmount() < getMaxUnits() && !fluid.isEmpty()) {
                if(this.temperature < temperature) this.temperature = temperature;
                int amount = Math.min(fluid.getAmount(), Math.min(getMaxUnits() - inputFluid.getAmount(), 10));
                if(empty) inputFluid = new FluidStack(fluid.getFluid(), amount);
                else inputFluid.grow(Math.min(fluid.getAmount(), amount));
                fluid.shrink(amount);
                setChanged();
                if(empty) level.setBlock(getBlockPos(), getBlockState().setValue(ItemMoldBlock.TICKING, true), 3);
                else level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
                return true;
            }
        }
        return false;
    }

    public void clearFluid() {
        temperature = 0F;
        if(!inputFluid.isEmpty()) {
            inputFluid = FluidStack.EMPTY;
            level.setBlock(getBlockPos(), getBlockState().setValue(ItemMoldBlock.TICKING, false), 3);
        }
    }

    public void setInputFluid(FluidStack fluid) {
        inputFluid = fluid;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public CompoundTag writeDataAndClear() {
        CompoundTag data = this.saveWithId();
        data.putInt("state", Block.getId(this.getBlockState().setValue(ItemMoldBlock.FACING, Direction.NORTH)));
        inputFluid = FluidStack.EMPTY;
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        NonNullList<ItemStack> drops = getContainerDrops();
        drops.add(new ItemStack(this.getBlockState().getBlock().asItem()));
        if(level != null) Containers.dropContents(level, pos, drops);
    }

    @Override
    public boolean useBlockEntityItemRenderer() {
        return true;
    }

    @Override
    public double getFirstPersonYOffset() {
        return 0.4D;
    }
}
