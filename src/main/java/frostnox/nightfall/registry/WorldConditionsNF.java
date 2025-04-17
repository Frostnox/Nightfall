package frostnox.nightfall.registry;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.world.condition.WorldCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class WorldConditionsNF {
    public static final ResourceLocation SATISFIED_CONDITION_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "gui/icon/satisfied_condition");
    public static final ResourceLocation UNSATISFIED_CONDITION_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "gui/icon/unsatisfied_condition");

    public static final DeferredRegister<WorldCondition> CONDITIONS = DeferredRegister.create(RegistriesNF.WORLD_CONDITIONS_KEY, Nightfall.MODID);

    public static final RegistryObject<WorldCondition> HEAT_SOURCE = CONDITIONS.register("heat_source", () -> new WorldCondition() {
        @Override
        public boolean test(Player player) {
            BlockPos pos = new BlockPos(player.getEyePosition());
            for(BlockPos searchPos : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3))) {
                BlockState state = player.level.getBlockState(searchPos);
                if(state.getBlock() instanceof IHeatSource heatSource && heatSource.getHeat(player.level, searchPos, state) != TieredHeat.NONE) {
                    return true;
                }
            }
            return false;
        }
    });
    public static final RegistryObject<WorldCondition> WATER_SOURCE = CONDITIONS.register("water_source", () -> new WorldCondition() {
        @Override
        public boolean test(Player player) {
            BlockPos pos = new BlockPos(player.getEyePosition());
            for(BlockPos searchPos : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3))) {
                if(player.level.getFluidState(searchPos).is(FluidTags.WATER)) {
                    return true;
                }
            }
            return false;
        }
    });

    public static void register() {
        CONDITIONS.register(Nightfall.MOD_EVENT_BUS);
    }

    public static WorldCondition get(ResourceLocation id) {
        return RegistriesNF.getWorldConditions().getValue(id);
    }

    public static boolean contains(ResourceLocation id) {
        return RegistriesNF.getWorldConditions().containsKey(id);
    }
}
