package frostnox.nightfall.block;

import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IAdjustableNodeType {
    NodeType adjustNodeType(NodeType type, BlockState state, LivingEntity entity);
}
