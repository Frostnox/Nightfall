package frostnox.nightfall.block.block.pile;

import net.minecraft.world.item.Item;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class FireablePileBlock extends PileBlock {
    public final Supplier<? extends PileBlock> firedBlock;

    public FireablePileBlock(Supplier<? extends Item> drop, Supplier<? extends PileBlock> firedBlock, VoxelShape shapeZ, Properties properties) {
        super(drop, shapeZ, properties);
        this.firedBlock = firedBlock;
    }
}
