package frostnox.nightfall.block.block.campfire;

import frostnox.nightfall.data.recipe.CampfireRecipe;
import frostnox.nightfall.data.recipe.SingleRecipe;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.Optional;
import java.util.Random;

public class CampfireBlockEntityNF extends BlockEntity implements Clearable {
    public static final int CAPACITY = 3, COOK_TIME = 60 * 20, FIREWOOD_BURN_TICKS = 120 * 20;
    public final NonNullList<ItemStack> items = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    private final int[] cookTicks = new int[CAPACITY];
    private int burnTicks;

    public CampfireBlockEntityNF(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.CAMPFIRE.get(), pos, pBlockState);
    }

    protected CampfireBlockEntityNF(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public static void cookTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntityNF campfire) {
        boolean changed = false;
        for(int i = 0; i < campfire.items.size(); i++) {
            ItemStack item = campfire.items.get(i);
            if(!item.isEmpty() && !item.is(ItemsNF.BURNT_FOOD.get())) {
                changed = true;
                campfire.cookTicks[i]++;
                RecipeWrapper container = new RecipeWrapper(new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, item)));
                Optional<CampfireRecipe> campfireRecipe = level.getRecipeManager().getRecipeFor(CampfireRecipe.TYPE, container, level);
                if(campfire.cookTicks[i] >= campfireRecipe.map(SingleRecipe::getCookTime).orElse(COOK_TIME)) {
                    campfire.cookTicks[i] = 0;
                    ItemStack cookedItem = campfireRecipe.map((recipe) -> recipe.assemble(container)).orElse(new ItemStack(ItemsNF.BURNT_FOOD.get()));
                    campfire.items.set(i, cookedItem);
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.playSound(null, pos, SoundsNF.SIZZLE.get(), SoundSource.BLOCKS,
                            1F, 0.97F + level.random.nextFloat() * 0.06F);
                }
            }
        }
        campfire.burnTicks++;
        if(campfire.burnTicks >= FIREWOOD_BURN_TICKS) {
            campfire.burnTicks = 0;
            int firewood = state.getValue(CampfireBlockNF.FIREWOOD);
            if(firewood == 1) level.setBlockAndUpdate(pos, state.setValue(CampfireBlockNF.FIREWOOD, 0).setValue(CampfireBlockNF.LIT, false));
            else level.setBlockAndUpdate(pos, state.setValue(CampfireBlockNF.FIREWOOD, firewood - 1));
            ((ServerLevel) level).sendParticles(ParticleTypesNF.FLAME_RED.get(), pos.getX() + 0.5, pos.getY() + 3.5D/16D,
                    pos.getZ() + 0.5, 12, 0.25, 3.5D/32D, 0.25, 0.014);
            level.playSound(null, pos, SoundsNF.FIRE_WHOOSH.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat() * 0.1F);
        }
        if(changed) setChanged(level, pos, state);
    }

    public static void cooldownTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntityNF campfire) {
        boolean changed = false;
        for(int i = 0; i < campfire.items.size(); i++) {
            if(campfire.cookTicks[i] > 0) {
                changed = true;
                campfire.cookTicks[i] = Mth.clamp(campfire.cookTicks[i] - 2, 0, COOK_TIME);
            }
            if(campfire.burnTicks > 0) {
                changed = true;
                campfire.burnTicks--;
            }
        }
        if(changed) setChanged(level, pos, state);
    }

    public static void particleTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntityNF campfire) {
        Random random = level.random;
        if(random.nextFloat() < 0.11F) {
            for(int i = 0; i < random.nextInt(2) + 2; i++) {
                CampfireBlock.makeParticles(level, pos, false, false);
            }
        }
        Direction.Axis axis = state.getValue(CampfireBlockNF.AXIS);
        for(int i = 0; i < campfire.items.size(); i++) {
            if(!campfire.items.get(i).isEmpty() && random.nextFloat() < 0.2F) {
                double x = pos.getX() + 0.5D;
                double z = pos.getZ() + 0.5D;
                if(axis == Direction.Axis.Z) {
                    if(i % 2 == 1) x += 0.25D * ((i + 1) / 2);
                    else if(i > 0) x -= 0.25D * (i / 2);
                }
                else {
                    if(i % 2 == 1) z += 0.25D * ((i + 1) / 2);
                    else if(i > 0) z -= 0.25D * (i / 2);
                }
                double y = pos.getY() + 1D;
                for(int j = 0; j < 2; j++) {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 5.0E-4D, 0.0D);
                }
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        if(tag.contains("cookTicks", 11)) {
            int[] cookTicks = tag.getIntArray("cookTicks");
            System.arraycopy(cookTicks, 0, this.cookTicks, 0, cookTicks.length);
        }
        burnTicks = tag.getInt("burnTicks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items, true);
        tag.putIntArray("cookTicks", cookTicks);
        tag.putInt("burnTicks", burnTicks);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, true);
        return tag;
    }

    public boolean placeFood(ItemStack food) {
        for(int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if(item.isEmpty()) {
                cookTicks[i] = 0;
                items.set(i, food.split(1));
                setChanged();
                if(!getBlockState().getValue(CampfireBlockNF.HAS_FOOD)) {
                    level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(CampfireBlockNF.HAS_FOOD, true));
                }
                else level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                return true;
            }
        }
        return false;
    }

    public ItemStack takeFood() {
        ItemStack bestItem = ItemStack.EMPTY;
        int index = -1;
        for(int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if(!item.isEmpty()) {
                bestItem = item;
                index = i;
                //Exit early if cooked
                if(level.getRecipeManager().getRecipeFor(CampfireRecipe.TYPE,
                        new RecipeWrapper(new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, item))), level).isEmpty()) break;
            }
        }
        if(!bestItem.isEmpty()) {
            cookTicks[index] = 0;
            items.set(index, ItemStack.EMPTY);
            setChanged();
            if(items.stream().allMatch(ItemStack::isEmpty)) {
                level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(CampfireBlockNF.HAS_FOOD, false));
            }
            else level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            return bestItem;
        }
        else return ItemStack.EMPTY;
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
