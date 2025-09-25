package frostnox.nightfall.block.block.eggnest;

import frostnox.nightfall.entity.entity.animal.DrakefowlBabyEntity;
import frostnox.nightfall.registry.forge.EntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class DrakefowlEggNestBlock extends EggNestBlock {
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 3.0D, 12.0D);

    public DrakefowlEggNestBlock(Supplier<? extends Item> eggItem, int hatchDuration, float minTemp, float maxTemp, Properties properties) {
        super(eggItem, hatchDuration, minTemp, maxTemp, properties);
    }

    @Override
    protected LivingEntity hatchBaby(ServerLevel level, BlockPos pos, int data) {
        DrakefowlBabyEntity chick =  EntitiesNF.DRAKEFOWL_CHICK.get().create(level);
        chick.moveTo(pos, level.random.nextFloat() * 360, 0);
        chick.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.BREEDING, null, null);
        level.addFreshEntity(chick);
        return chick;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }
}
