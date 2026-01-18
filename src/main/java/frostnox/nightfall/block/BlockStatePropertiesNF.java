package frostnox.nightfall.block;

import frostnox.nightfall.block.block.SidingBlock;
import frostnox.nightfall.block.block.ChairBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockStatePropertiesNF {
    public static final BooleanProperty SMELTED = BooleanProperty.create("smelted");
    public static final BooleanProperty RANDOM_TICKING = BooleanProperty.create("random_ticking");
    public static final BooleanProperty TICKING = BooleanProperty.create("ticking");
    public static final BooleanProperty CHANGED = BooleanProperty.create("changed");
    public static final BooleanProperty CHARRED = BooleanProperty.create("charred");
    public static final BooleanProperty ALTERNATE = BooleanProperty.create("alternate");
    public static final BooleanProperty HEATED = BooleanProperty.create("heated");
    public static final BooleanProperty HAS_FOOD = BooleanProperty.create("has_food");
    public static final BooleanProperty HAS_METAL = BooleanProperty.create("has_metal");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty SUPPORT = BooleanProperty.create("support");
    public static final IntegerProperty WATER_LEVEL = IntegerProperty.create("water_level", 0, 7); //0 = empty. 6 = falling. 7 = source.
    public static final IntegerProperty HEAT_FULL = IntegerProperty.create("heat", 0, 5); //0 = none. 1-5 correspond to TieredHeat values.
    public static final IntegerProperty HEAT = IntegerProperty.create("heat", 1, 5);
    public static final IntegerProperty INGOTS = IntegerProperty.create("ingots", 1, 12);
    public static final IntegerProperty CHUNKS = IntegerProperty.create("chunks", 1, 4);
    public static final IntegerProperty LOGS = IntegerProperty.create("logs", 1, 9);
    public static final IntegerProperty FIREWOOD = IntegerProperty.create("firewood", 0, 4);
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 0, 4);
    public static final IntegerProperty COUNT_4 = IntegerProperty.create("count", 1, 4);
    public static final IntegerProperty COUNT_8 = IntegerProperty.create("count", 1, 8);
    public static final IntegerProperty STAGE_4 = IntegerProperty.create("stage", 1, 4);
    public static final IntegerProperty STAGE_8 = IntegerProperty.create("stage", 1, 8);
    public static final DirectionProperty FACING_NOT_DOWN = DirectionProperty.create("facing", (dir) -> dir != Direction.DOWN);
    public static final DirectionProperty FACING_NOT_UP = DirectionProperty.create("facing", (dir) -> dir != Direction.UP);
    public static final EnumProperty<IWaterloggedBlock.WaterlogType> WATERLOG_TYPE = EnumProperty.create("waterlog_type", IWaterloggedBlock.WaterlogType.class);
    public static final EnumProperty<SidingBlock.Type> SIDING_TYPE = EnumProperty.create("siding_type", SidingBlock.Type.class);
    public static final EnumProperty<SidingBlock.Shape> SIDING_SHAPE = EnumProperty.create("siding_shape", SidingBlock.Shape.class);
    public static final EnumProperty<Humidity> HUMIDITY = EnumProperty.create("humidity", Humidity.class);
    public static final EnumProperty<ChairBlock.Type> CHAIR_TYPE = EnumProperty.create("chair_type", ChairBlock.Type.class);
}
