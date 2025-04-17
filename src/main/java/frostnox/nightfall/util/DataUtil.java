package frostnox.nightfall.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataUtil {
    public static final Direction[] DIRECTIONS_EXCEPT_UP = new Direction[] {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static final Direction[] DIRECTIONS_EXCEPT_DOWN = new Direction[] {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static final Direction[] DIRECTIONS_EXCEPT_NORTH = new Direction[] {Direction.DOWN, Direction.UP, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static final Direction[] DIRECTIONS_EXCEPT_SOUTH = new Direction[] {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.WEST, Direction.EAST};
    public static final Direction[] DIRECTIONS_EXCEPT_WEST = new Direction[] {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST};
    public static final Direction[] DIRECTIONS_EXCEPT_EAST = new Direction[] {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST};
    /**
     * Guaranteed to be unique when x, y, and z are within limited ranges
     */
    public static int hashPos(int x, int y, int z) {
        return ((x & 1023) << 22) | ((z & 1023) << 12) | (y & 4095);
    }

    public static int hashPos(BlockPos pos) {
        return hashPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos fromPosHash(int hash) {
        return new BlockPos(hash >>> 22, hash & 4095, (hash >>> 12) & 1023);
    }

    public static float[] toArray(List<Float> floatList) {
        float[] array = new float[floatList.size()];
        for(int i = 0; i < floatList.size(); i++) array[i] = floatList.get(i);
        return array;
    }

    public static int packChunkPos(int worldX, int worldY, int worldZ) {
        return ((worldX & 15) << 16) | ((worldZ & 15) << 12) | (worldY & 4095);
    }

    public static int packChunkPos(BlockPos pos) {
        return packChunkPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos unpackChunkPos(int minX, int minZ, int packedPos) {
        return new BlockPos(minX + ((packedPos >>> 16) & 15), packedPos & 4095, minZ + ((packedPos >>> 12) & 15));
    }

    public static Direction[] getDirectionsExcept(Direction direction) {
        return switch(direction) {
            case UP -> DIRECTIONS_EXCEPT_UP;
            case DOWN -> DIRECTIONS_EXCEPT_DOWN;
            case NORTH -> DIRECTIONS_EXCEPT_NORTH;
            case SOUTH -> DIRECTIONS_EXCEPT_SOUTH;
            case WEST -> DIRECTIONS_EXCEPT_WEST;
            case EAST -> DIRECTIONS_EXCEPT_EAST;
        };
    }

    public static CompoundTag writeFluids(List<FluidStack> list, CompoundTag tag) {
        ListTag listTag = new ListTag();
        for(int i = 0; i < list.size(); i++) {
            FluidStack fluid = list.get(i);
            if(!fluid.isEmpty()) {
                CompoundTag fluidTag = new CompoundTag();
                fluid.writeToNBT(fluidTag);
                listTag.add(fluidTag);
            }
        }
        tag.put("Fluids", listTag);
        return tag;
    }

    public static void loadFluids(List<FluidStack> list, CompoundTag tag) {
        ListTag listTag = tag.getList("Fluids", ListTag.TAG_COMPOUND);
        for(int i = 0; i < listTag.size(); i++) {
            CompoundTag fluidTag = listTag.getCompound(i);
            list.add(FluidStack.loadFluidStackFromNBT(fluidTag));
        }
    }

    public static <T> ListTag writeSlottedData(List<T> data, BiConsumer<CompoundTag, T> writer, Predicate<T> filter) {
        ListTag listTag = new ListTag();
        for(int i = 0; i < data.size(); i++) {
            T object = data.get(i);
            if(object != null && !filter.test(object)) {
                CompoundTag tag = new CompoundTag();
                tag.putByte("Slot", (byte) i);
                writer.accept(tag, object);
                listTag.add(tag);
            }
        }
        return listTag;
    }

    public static <T> ListTag writeSlottedData(List<T> data, BiConsumer<CompoundTag, T> writer) {
        return writeSlottedData(data, writer, (t) -> false);
    }

    public static <T> void loadSlottedData(ListTag listTag, List<T> data, Function<CompoundTag, T> reader) {
        for(int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            int slot = tag.getByte("Slot") & 255;
            if(slot < data.size()) data.set(slot, reader.apply(tag));
        }
    }

    public static void writeSlottedFluids(CompoundTag tag, NonNullList<FluidStack> fluids) {
        ListTag listtag = new ListTag();
        for(int i = 0; i < fluids.size(); i++) {
            FluidStack fluidStack = fluids.get(i);
            if(!fluidStack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte)i);
                fluidStack.writeToNBT(compoundtag);
                listtag.add(compoundtag);
            }
        }
        tag.put("Fluids", listtag);
    }

    public static void loadSlottedFluids(CompoundTag tag, NonNullList<FluidStack> fluids) {
        ListTag listtag = tag.getList("Fluids", 10);
        for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if(j < fluids.size()) fluids.set(j, FluidStack.loadFluidStackFromNBT(compoundtag));
        }
    }

    public static FluidStack fluidStackFromJson(JsonObject json) {
        String name = GsonHelper.getAsString(json, "fluid");
        ResourceLocation location = ResourceLocation.parse(name);
        if(!ForgeRegistries.FLUIDS.containsKey(location)) throw new JsonSyntaxException("Unknown fluid '" + name + "'");
        return new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(location)), GsonHelper.getAsInt(json, "count", 1));
    }

    public static JsonObject itemStackToJson(ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
        if(item.getCount() > 1) json.addProperty("count", item.getCount());
        return json;
    }

    public static JsonObject fluidStackToJson(FluidStack fluid) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString());
        if(fluid.getAmount() > 1) json.addProperty("count", fluid.getAmount());
        return json;
    }

    public static <T extends Enum<T>, V> Map<T, V> mapEnum(Class<T> enumClass, Predicate<T> filter, Function<T, V> valueMapper) {
        EnumMap<T, V> enumMap = new EnumMap<>(enumClass);
        for(T enumValue : enumClass.getEnumConstants()) {
            if(filter.test(enumValue)) continue;
            V mappedValue = valueMapper.apply(enumValue);
            enumMap.put(enumValue, mappedValue);
        }
        return Collections.unmodifiableMap(enumMap);
    }

    public static <T extends Enum<T>, V> Map<T, V> mapEnum(Class<T> enumClass, Function<T, V> valueMapper) {
        return mapEnum(enumClass, (clazz) -> false, valueMapper);
    }

    public static <T, V> Map<T, V> map(Iterable<T> values, Predicate<T> filter, Function<T, V> valueMapper) {
        Map<T, V> map = new HashMap<>();
        for(T value : values) {
            if(filter.test(value)) continue;
            V mappedValue = valueMapper.apply(value);
            map.put(value, mappedValue);
        }
        return Collections.unmodifiableMap(map);
    }

    public static <T, V> Map<T, V> map(Iterable<T> values, Function<T, V> valueMapper) {
        return map(values, (clazz) -> false, valueMapper);
    }

    public static <T> List<T> concat(Collection<T>... collections) {
        int size = 0;
        for(Collection<T> collection : collections) size += collection.size();
        List<T> list = new ObjectArrayList<>(size);
        for(Collection<T> collection : collections) list.addAll(collection);
        return list;
    }

    public static String getName(EquipmentSlot slot) {
        return switch(slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> "";
        };
    }

    public static String getLocalName(EquipmentSlot slot) {
        return WordUtils.capitalize(getName(slot));
    }

    public static String getLocalName(String name) {
        return WordUtils.capitalize(name.replace("_", " "));
    }

    public static double getSimulatedWeatherPercentageAboveIntensity(int simulations, double intensity) {
        Random random = new Random();
        float lastWeatherIntensity, weatherIntensity = 0;
        int weatherDuration;
        double totalDuration = 0, targetDuration = 0;
        for(int i = 0; i < simulations; i++) {
            lastWeatherIntensity = weatherIntensity;
            weatherIntensity = (float) MathUtil.gammaSample(1.2, random) / 4F;
            Season season = Season.values()[random.nextInt(Season.values().length)];
            if(random.nextFloat() < (season == Season.FALL ? 0.5F : 0.3F)) {
                weatherIntensity = -Math.min(weatherIntensity, 1F);
            }
            else {
                if(season == Season.SUMMER) weatherIntensity *= 0.8F;
                else if(season == Season.SPRING) weatherIntensity *= 1.2F;
                weatherIntensity = Math.min(weatherIntensity, 1F);
            }
            weatherDuration = 20 * 60 * 8 + random.nextInt(20 * 60 * 12);
            totalDuration += weatherDuration;
            if((intensity >= lastWeatherIntensity && intensity <= weatherIntensity) || (intensity >= weatherIntensity && intensity <= lastWeatherIntensity)) {
                double timeAtIntensity = (intensity - lastWeatherIntensity) / (weatherIntensity - lastWeatherIntensity) * weatherDuration;
                targetDuration += totalDuration - timeAtIntensity;
            }
        }
        return (targetDuration / totalDuration) * 100;
    }
}