package frostnox.nightfall.block.block.cauldron;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.block.block.MenuContainerBlockEntity;
import frostnox.nightfall.data.recipe.CauldronRecipe;
import frostnox.nightfall.item.ItemStackHandlerNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.world.inventory.StorageContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CauldronBlockEntity extends MenuContainerBlockEntity implements IHoldable {
    public final ItemStackHandlerNF inventory;
    protected ResourceLocation recipeLocation;
    protected int cookTicks, cookTicksTotal = CauldronRecipe.COOK_TIME;
    public ItemStack meal = ItemStack.EMPTY;
    public boolean hot = false;
    private boolean needsUpdate = false;
    public int animTick, forceEntityRenderTick = -1;
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            if(pIndex == 0) return cookTicks;
            else return cookTicksTotal;
        }

        @Override
        public void set(int pIndex, int pValue) {
            if(pIndex == 0) cookTicks = pValue;
            else cookTicksTotal = pValue;
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.CAULDRON.get(), pos, state);
    }

    protected CauldronBlockEntity(BlockEntityType<?> pType, BlockPos pos, BlockState pBlockState) {
        super(pType, pos, pBlockState);
        inventory = new ItemStackHandlerNF(4) {
            @Override
            protected void onContentsChanged(int slot) {
                if(level == null || level.isClientSide()) return;
                setChanged();
                if(getBlockState().getValue(CauldronBlockNF.TASK) == Task.IDLE && canCookMeal())  {
                    level.setBlockAndUpdate(pos, getBlockState().setValue(CauldronBlockNF.TASK, Task.COOK));
                }
                Optional<CauldronRecipe> newRecipe = level.getRecipeManager().getRecipeFor(CauldronRecipe.TYPE, new RecipeWrapper(inventory), level);
                recipeLocation = newRecipe.isPresent() ? newRecipe.get().getId() : null;
                cookTicks = 0;
                cookTicksTotal = newRecipe.isPresent() ? newRecipe.get().getCookTime() : CauldronRecipe.COOK_TIME;
            }
        };
    }

    public int getCookTicks() {
        return cookTicks;
    }

    public int getCookTicksTotal() {
        return cookTicksTotal;
    }

    public boolean canCookMeal() {
        if(!hot) return false;
        int water = 0;
        for(int i = 0; i < inventory.getSlots(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if(item.isEmpty()) return false;
            if(item.is(ItemsNF.WATER.get()) || item.is(ItemsNF.SEAWATER.get())) {
                water++;
                if(water > 1) return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable ContainerData getData() {
        return data;
    }

    @Override
    public ItemStackHandlerNF getInventory() {
        return inventory;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent(Nightfall.MODID + ".cauldron");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return StorageContainer.createCauldronContainer(pContainerId, pInventory, this);
    }

    @Override
    public void startOpen(Player pPlayer) {
        level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                SoundsNF.CERAMIC_OPEN_LARGE.get(), SoundSource.BLOCKS, 1F, 0.94F + level.random.nextFloat() * 0.12F);
        level.gameEvent(pPlayer, GameEvent.CONTAINER_OPEN, worldPosition);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("recipe", Tag.TAG_STRING)) recipeLocation = ResourceLocation.parse(tag.getString("recipe"));
        cookTicks = tag.getInt("ticks");
        cookTicksTotal = tag.getInt("ticksTotal");
        meal = ItemStack.of(tag.getCompound("meal"));
        hot = tag.getBoolean("hot");
        needsUpdate = !meal.isEmpty() || hot;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(recipeLocation != null) tag.putString("recipe", recipeLocation.toString());
        tag.putInt("ticks", cookTicks);
        tag.putInt("ticksTotal", cookTicksTotal);
        tag.putBoolean("hot", hot);
        if(!meal.isEmpty()) tag.put("meal", meal.save(new CompoundTag()));
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        if(needsUpdate) {
            needsUpdate = false;
            return ClientboundBlockEntityDataPacket.create(this);
        }
        else return null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("hot", hot);
        if(!meal.isEmpty()) tag.put("meal", meal.save(new CompoundTag()));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        hot = tag.getBoolean("hot");
        meal = ItemStack.of(tag.getCompound("meal"));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if(pkt.getTag() != null) handleUpdateTag(pkt.getTag());
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CauldronBlockEntity entity) {
        if(entity.animTick == 0) entity.animTick = level.random.nextInt(4096);
        else entity.animTick++;
    }

    public static void cookTick(Level level, BlockPos pos, BlockState state, CauldronBlockEntity entity) {
        if(!entity.hasMeal() && entity.canCookMeal()) {
            entity.cookTicks++;
            entity.setChanged();
            if(entity.cookTicks >= entity.cookTicksTotal) {
                RecipeWrapper container = new RecipeWrapper(entity.inventory);
                ItemStack cookedMeal = level.getRecipeManager().getRecipeFor(CauldronRecipe.TYPE, container, level).map((recipe) ->
                        recipe.assembleItem(container, null)).orElse(new ItemStack(ItemsNF.SUSPICIOUS_STEW.get(), 4));
                entity.clearContent();
                entity.meal = cookedMeal;
                entity.needsUpdate = true;
                state = state.setValue(CauldronBlockNF.TASK, Task.DONE);
                level.setBlockAndUpdate(pos, state);
            }
            if(state.getValue(CauldronBlockNF.TASK) == Task.IDLE) level.setBlockAndUpdate(pos, state.setValue(CauldronBlockNF.TASK, Task.COOK));
        }
        else if(state.getValue(CauldronBlockNF.TASK) == Task.COOK) {
            level.setBlockAndUpdate(pos, state.setValue(CauldronBlockNF.TASK, Task.IDLE));
        }
    }

    public static void idleTick(Level level, BlockPos pos, BlockState state, CauldronBlockEntity entity) {
        if(entity.cookTicks > 0) {
            entity.cookTicks -= 2;
            entity.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && !hasMeal();
    }

    public boolean hasMeal() {
        return !meal.isEmpty();
    }

    public ItemStack takeMeal() {
        ItemStack serving = meal.split(1);
        needsUpdate = true;
        if(meal.isEmpty()) level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(CauldronBlockNF.TASK, Task.IDLE));
        else level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        return serving;
    }

    public void applyHeat(boolean heated) {
        if(hot != heated) {
            hot = heated;
            needsUpdate = true;
            setChanged();
            Task task = getBlockState().getValue(CauldronBlockNF.TASK);
            if(!heated && task == Task.COOK) {
                level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(CauldronBlockNF.TASK, Task.IDLE));
            }
            else if(task == Task.IDLE && canCookMeal()) {
                level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(CauldronBlockNF.TASK, Task.COOK));
            }
            else level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public CompoundTag writeDataAndClear() {
        hot = false;
        CompoundTag data = saveWithId();
        data.putInt("state", Block.getId(getBlockState().getBlock().defaultBlockState()
                .setValue(CauldronBlockNF.TASK, meal.isEmpty() ? Task.IDLE : Task.DONE)));
        inventory.clear();
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        NonNullList<ItemStack> drops = getContainerDrops();
        drops.add(new ItemStack(getBlockState().getBlock().asItem()));
        if(level != null) Containers.dropContents(level, pos, drops);
    }

    @Override
    public boolean useBlockEntityItemRenderer() {
        return true;
    }

    @Override
    public double getThirdPersonYOffset() {
        return -3D/16D;
    }
}
