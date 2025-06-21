package frostnox.nightfall.block.block.barrel;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.block.block.MenuContainerBlockEntity;
import frostnox.nightfall.data.recipe.BarrelRecipe;
import frostnox.nightfall.world.inventory.ItemStackHandlerNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.world.inventory.StorageContainer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class BarrelBlockEntityNF extends MenuContainerBlockEntity implements IHoldable {
    public static final int ROWS = 4, COLUMNS = 3, MAX_RECIPES = COLUMNS;
    public final ItemStackHandlerNF inventory;
    public final List<ResourceLocation> activeRecipes = new ObjectArrayList<>(new ResourceLocation[MAX_RECIPES]);
    public final IntList soakTicks = new IntArrayList(new int[MAX_RECIPES]);
    public final IntList soakDurations = new IntArrayList(IntStream.generate(() -> 1).limit(MAX_RECIPES).toArray());
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return pIndex < MAX_RECIPES ? soakTicks.getInt(pIndex) : soakDurations.getInt(pIndex - MAX_RECIPES);
        }

        @Override
        public void set(int pIndex, int pValue) {
            if(pIndex < MAX_RECIPES) soakTicks.set(pIndex, pValue);
            else soakDurations.set(pIndex - MAX_RECIPES, pValue);
        }

        @Override
        public int getCount() {
            return MAX_RECIPES * 2;
        }
    };
    private final ContainerOpenersCounter opener = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(state, SoundEvents.BARREL_OPEN);
            level.setBlockAndUpdate(pos, state.setValue(BarrelBlockNF.OPEN, true));
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(state, SoundEvents.BARREL_CLOSE);
            level.setBlockAndUpdate(pos, state.setValue(BarrelBlockNF.OPEN, false));
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int eventId, int eventParam) {

        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if(player.containerMenu instanceof StorageContainer storageContainer) return storageContainer.entity == BarrelBlockEntityNF.this;
            else return false;
        }
    };

    public BarrelBlockEntityNF(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.BARREL.get(), pos, pBlockState);
    }

    protected BarrelBlockEntityNF(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        inventory = new ItemStackHandlerNF(ROWS * COLUMNS) {
            @Override
            protected void onContentsChanged(int slot) {
                if(level == null || level.isClientSide()) return;
                setChanged();
                int column = slot % COLUMNS;
                NonNullList<ItemStack> items = NonNullList.withSize(ROWS, ItemStack.EMPTY);
                int inputSize = 0;
                for(int i = 0; i < ROWS; i++) {
                    ItemStack item = inventory.getStackInSlot(column + i * 3);
                    items.set(i, item);
                    if(inputSize == 0 && !item.isEmpty()) inputSize = item.getCount();
                }
                Optional<BarrelRecipe> newRecipe = level.getRecipeManager().getRecipeFor(BarrelRecipe.TYPE,
                        new RecipeWrapper(new ItemStackHandlerNF(items)), level);
                activeRecipes.set(column, newRecipe.isPresent() ? newRecipe.get().getId() : null);
                soakTicks.set(column, 0);
                soakDurations.set(column, newRecipe.isPresent() ?
                        (newRecipe.get().hasFixedSoakTime() ? newRecipe.get().getSoakTime() : (newRecipe.get().getSoakTime() * inputSize)) : 1);
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarrelBlockEntityNF entity) {
        boolean changed = false;
        for(int i = 0; i < MAX_RECIPES; i++) {
            ResourceLocation recipeLocation = entity.activeRecipes.get(i);
            if(recipeLocation != null) {
                Optional<? extends Recipe<?>> recipe = level.getRecipeManager().byKey(recipeLocation);
                if(recipe.isPresent()) {
                    int soakTicks = entity.soakTicks.getInt(i) + 1;
                    entity.soakTicks.set(i, soakTicks);
                    changed = true;
                    if(soakTicks >= entity.soakDurations.getInt(i)) {
                        NonNullList<ItemStack> items = NonNullList.withSize(ROWS, ItemStack.EMPTY);
                        for(int j = 0; j < ROWS; j++) {
                            int slot = i + j * 3;
                            items.set(j, entity.inventory.getStackInSlot(slot));
                            entity.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                        }
                        ItemStack item = ((BarrelRecipe) recipe.get()).assemble(new RecipeWrapper(new ItemStackHandlerNF(items)));
                        entity.inventory.setStackInSlot(i, item);
                    }
                }
            }
        }
        if(changed) entity.setChanged();
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
        return new TranslatableComponent(Nightfall.MODID + ".barrel");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pId, Inventory playerInv) {
        return StorageContainer.createBarrelContainer(pId, playerInv, this);
    }

    @Override
    public void startOpen(Player pPlayer) {
        if(!remove && !pPlayer.isSpectator()) opener.incrementOpeners(pPlayer, getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public void stopOpen(Player pPlayer) {
        if(!remove && !pPlayer.isSpectator()) opener.decrementOpeners(pPlayer, getLevel(), getBlockPos(), getBlockState());
    }

    public void recheckOpen() {
        if(!remove) opener.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
    }

    private void playSound(BlockState state, SoundEvent pSound) {
        Vec3i normal = state.getValue(BarrelBlockNF.FACING).getNormal();
        level.playSound(null, worldPosition.getX() + 0.5D + normal.getX() / 2.0D,
                worldPosition.getY() + 0.5D + normal.getY() / 2.0D, worldPosition.getZ() + 0.5D + normal.getZ() / 2.0D,
                pSound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        DataUtil.loadSlottedData(tag.getList("recipes", ListTag.TAG_COMPOUND), activeRecipes, (t) -> ResourceLocation.parse(t.getString("recipe")));
        DataUtil.loadSlottedData(tag.getList("soakTicks", ListTag.TAG_COMPOUND), soakTicks, (t) -> t.getInt("ticks"));
        DataUtil.loadSlottedData(tag.getList("soakDurations", ListTag.TAG_COMPOUND), soakDurations, (t) -> t.getInt("duration"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("recipes", DataUtil.writeSlottedData(activeRecipes, (t, r) -> t.putString("recipe", r.toString())));
        tag.put("soakTicks", DataUtil.writeSlottedData(soakTicks, (t, i) -> t.putInt("ticks", i), (i) -> i == 0));
        tag.put("soakDurations", DataUtil.writeSlottedData(soakDurations, (t, i) -> t.putInt("duration", i), (i) -> i == 1));
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
