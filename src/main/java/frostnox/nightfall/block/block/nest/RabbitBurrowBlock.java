package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RabbitBurrowBlock extends OverlayBurrowBlock {
    public RabbitBurrowBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        super.randomTick(state, level, pos, random);
        if(level.getBlockEntity(pos) instanceof NestBlockEntity nest) {
            if(LevelUtil.isDayTimeWithin(level, LevelUtil.MORNING_TIME, LevelUtil.SUNSET_TIME)) nest.removeAllEntities(false);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.RABBIT_BURROW.get().create(pos, state);
    }
}
