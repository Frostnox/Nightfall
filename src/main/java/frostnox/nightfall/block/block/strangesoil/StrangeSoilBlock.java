package frostnox.nightfall.block.block.strangesoil;

import frostnox.nightfall.block.block.UnstableBlock;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StrangeSoilBlock extends UnstableBlock implements EntityBlock {
    public final Lazy<BlockState> normalSoil;

    public StrangeSoilBlock(Supplier<SoundEvent> slideSound, Supplier<? extends Block> normalSoil, Properties properties) {
        super(slideSound, properties);
        this.normalSoil = Lazy.of(() -> normalSoil.get().defaultBlockState());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) {
        List<ItemStack> blockDrops = super.getDrops(state, context);
        if(context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player &&
                context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof StrangeSoilBlockEntity entity && entity.lootTableLoc != null) {
            MinecraftServer server = entity.getLevel().getServer();
            if(server != null) {
                List<ItemStack> lootDrops = server.getLootTables().get(entity.lootTableLoc).getRandomItems(context.create(LootContextParamSets.CHEST));
                return Stream.concat(blockDrops.stream(), lootDrops.stream()).toList();
            }
        }
        return blockDrops;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.STRANGE_SOIL.get().create(pos, state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int pId, int pParam) {
        super.triggerEvent(state, level, pos, pId, pParam);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(pId, pParam);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return normalSoil.get().getCloneItemStack(target, level, pos, player);
    }
}
