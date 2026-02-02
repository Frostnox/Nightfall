package frostnox.nightfall.block.block.pot;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.block.block.MenuContainerBlockEntity;
import frostnox.nightfall.world.inventory.ItemStackHandlerNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.world.inventory.StorageContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotBlockEntity extends MenuContainerBlockEntity implements IHoldable {
    public static final int ROWS = 4, COLUMNS = 4;
    public final ItemStackHandlerNF inventory;

    public PotBlockEntity(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.POT.get(), pos, pBlockState);
    }

    protected PotBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        inventory = new ItemStackHandlerNF(ROWS * COLUMNS);
    }

    @Override
    public void startOpen(Player player) {
        level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                SoundsNF.CERAMIC_OPEN_LARGE.get(), SoundSource.BLOCKS, 1F, 0.94F + level.random.nextFloat() * 0.12F);
        level.gameEvent(player, GameEvent.CONTAINER_OPEN, worldPosition);
    }

    @Override
    public ItemStackHandlerNF getInventory() {
        return inventory;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent(Nightfall.MODID + ".pot");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pId, Inventory playerInv) {
        return StorageContainer.createPotContainer(pId, playerInv, this);
    }

    @Override
    public CompoundTag writeDataAndClear() {
        CompoundTag data = saveWithId();
        data.putInt("state", Block.getId(getBlockState().getBlock().defaultBlockState()));
        inventory.clear();
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        NonNullList<ItemStack> drops = getContainerDrops();
        drops.add(new ItemStack(getBlockState().getBlock().asItem()));
        if(level != null) Containers.dropContents(level, pos, drops);
    }
}
