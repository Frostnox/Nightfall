package frostnox.nightfall.block.block.anvil;

import frostnox.nightfall.data.recipe.TieredAnvilRecipe;
import frostnox.nightfall.item.ItemStackHandlerNF;
import frostnox.nightfall.registry.forge.ContainersNF;
import frostnox.nightfall.world.inventory.PartialInventoryContainer;
import frostnox.nightfall.world.inventory.SingleSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class TieredAnvilContainer extends PartialInventoryContainer {
    public final TieredAnvilBlockEntity entity;
    private final ContainerLevelAccess access;
    public List<TieredAnvilRecipe> recipes = Lists.newArrayList();
    private final ItemStackHandlerNF inventory;
    private final DataSlot selectedRecipe = DataSlot.standalone();

    public TieredAnvilContainer(int windowID, Inventory playerInv, TieredAnvilBlockEntity entity) {
        super(ContainersNF.TIERED_ANVIL.get(), playerInv, windowID, true);
        this.entity = entity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());
        this.inventory = new ItemStackHandlerNF(3) {
            @Override
            protected void onContentsChanged(int slot) {
                if(entity.getLevel() == null || !entity.getLevel().isClientSide) return;
                setupRecipes(playerInv.player);
            }
        };
        this.addSlot(new SingleSlot(inventory, 0, 17, 17));
        this.addSlot(new SingleSlot(inventory, 1, 17, 17 + 18));
        this.addSlot(new SingleSlot(inventory, 2, 17, 17 + 18 + 18));
        this.addDataSlot(selectedRecipe);
        selectedRecipe.set(-1);
    }

    public boolean hasValidInput() {
        return !inventory.isEmpty() && !recipes.isEmpty();
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipe.get();
    }

    public void setupRecipes(Player player) {
        selectedRecipe.set(-1);
        if(!inventory.isEmpty()) {
            recipes = entity.getLevel().getRecipeManager().getRecipesFor(TieredAnvilRecipe.TYPE, new RecipeWrapper(inventory), entity.getLevel()).stream()
                    .filter((recipe) -> recipe.getTier() <= ((TieredAnvilBlock) entity.getBlockState().getBlock()).tier && recipe.isUnlocked(player)).toList();
        }
        else recipes.clear();
    }

    public void consumeInputs() {
        inventory.clear();
    }

    public static TieredAnvilContainer createClientContainer(int windowID, Inventory playerInv, FriendlyByteBuf extraData) {
        BlockEntity entity = playerInv.player.level.getBlockEntity(extraData.readBlockPos());
        if(entity instanceof TieredAnvilBlockEntity anvilEntity) return new TieredAnvilContainer(windowID, playerInv, anvilEntity);
        else throw new IllegalStateException("Anvil block entity does not exist at " + extraData.readBlockPos());
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        if(index >= 0 && index < this.recipes.size() && index != selectedRecipe.get()) {
            this.selectedRecipe.set(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return entity.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((p_40313_, p_40314_) -> {
            this.clearContainer(player, new RecipeWrapper(this.inventory));
        });
    }
}
