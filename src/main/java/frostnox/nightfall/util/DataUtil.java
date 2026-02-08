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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.*;

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

    public static Fluid fluidFromJson(JsonObject json) {
        String name = GsonHelper.getAsString(json, "fluid");
        ResourceLocation location = ResourceLocation.parse(name);
        if(!ForgeRegistries.FLUIDS.containsKey(location)) throw new JsonSyntaxException("Unknown fluid '" + name + "'");
        return Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(location));
    }

    public static JsonObject fluidToJson(Fluid fluid) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
        return json;
    }

    public static FluidStack fluidStackFromJson(JsonObject json) {
        String name = GsonHelper.getAsString(json, "fluid");
        ResourceLocation location = ResourceLocation.parse(name);
        if(!ForgeRegistries.FLUIDS.containsKey(location)) throw new JsonSyntaxException("Unknown fluid '" + name + "'");
        return new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(location)), GsonHelper.getAsInt(json, "count", 1));
    }

    public static JsonObject fluidStackToJson(FluidStack fluid) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString());
        if(fluid.getAmount() > 1) json.addProperty("count", fluid.getAmount());
        return json;
    }

    public static JsonObject itemStackToJson(ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
        if(item.getCount() > 1) json.addProperty("count", item.getCount());
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

    /**
     * AI generated title casing code
     * <p>
     * Rules implemented (Chicago-style, pragmatic):
     *  - Capitalize first and last words.
     *  - Capitalize "major" words; keep minor words lowercase unless first/last or after punctuation like ':'.
     *  - Preserve words with internal capitals (iPhone) and all-uppercase acronyms (NASA).
     *  - Hyphenated words: title-cap each hyphen part (with minor-word logic applied per-part).
     *  - Words after sentence/subtitle punctuation (. ! ? :) are capitalized.
     */

    private static final Set<String> MINOR_WORDS = new HashSet<>(Arrays.asList(
            // Common "minor" words: articles, coordinating conjunctions, short prepositions, and a few others.
            "a", "an", "and", "as", "at", "but", "by", "for", "if", "in", "nor",
            "of", "on", "or", "per", "the", "to", "vs", "via", "with", "from", "over", "into", "onto"
    ));

    // Characters that, if immediately preceding a word, force the word to be capitalized
    private static final Pattern FORCE_CAP_AFTER = Pattern.compile("[:!?.\\u2014\\u2013]$"); // :, !, ?, ., em-dash, en-dash

    // Word detection (letters or digits plus internal apostrophes/hyphens/slashes)
    private static final Pattern LEADING_PUNCT = Pattern.compile("^[^\\p{L}\\p{N}]+"); // leading non-letter/digit
    private static final Pattern TRAILING_PUNCT = Pattern.compile("[^\\p{L}\\p{N}]+$"); // trailing non-letter/digit

    /**
     * Convert input text to title case using default locale.
     */
    public static String toTitleCase(String input) {
        return toTitleCase(input, Locale.getDefault());
    }

    /**
     * Convert input text to title case using specified locale.
     */
    public static String toTitleCase(String input, Locale locale) {
        if (input == null || input.isEmpty()) return input;

        // Normalize spaces (preserve multi-spaces? we collapse to single space for simplicity)
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) return input;

        StringBuilder out = new StringBuilder(input.length() + 16);
        boolean prevForcesCapital = false;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            boolean isFirst = (i == 0);
            boolean isLast = (i == tokens.length - 1);

            // Check if previous token ended with punctuation that forces capitalization
            if (i > 0) {
                String prev = tokens[i - 1];
                prevForcesCapital = FORCE_CAP_AFTER.matcher(prev).find();
            } else {
                prevForcesCapital = false;
            }

            String processed = processToken(token, isFirst, isLast, prevForcesCapital, locale);
            out.append(processed);
            if (i < tokens.length - 1) out.append(' ');
        }

        return out.toString();
    }

    // Process a single whitespace-separated token, preserving leading/trailing punctuation.
    private static String processToken(String token, boolean isFirst, boolean isLast,
                                       boolean prevForcesCapital, Locale locale) {
        // Extract leading and trailing punctuation (if any)
        Matcher leadM = LEADING_PUNCT.matcher(token);
        int leadEnd = 0;
        if (leadM.find()) leadEnd = leadM.end();

        Matcher trailM = TRAILING_PUNCT.matcher(token);
        int trailStart = token.length();
        if (trailM.find()) trailStart = trailM.start();

        String leading = token.substring(0, leadEnd);
        String trailing = token.substring(trailStart);
        String core = token.substring(leadEnd, trailStart);

        if (core.isEmpty()) {
            // Token is pure punctuation like "..." or "("
            return token;
        }

        // Handle slash-separated (e.g., "and/or") by processing parts
        if (core.contains("/")) {
            String[] parts = core.split("/", -1);
            for (int p = 0; p < parts.length; p++) {
                parts[p] = processWordPart(parts[p], isFirst, isLast, prevForcesCapital, locale);
            }
            core = String.join("/", parts);
            return leading + core + trailing;
        }

        // Handle hyphenated words: title-case each part (common editorial approach).
        if (core.contains("-")) {
            String[] parts = core.split("(?<=-)|(?=-)"); // keep hyphens as separate tokens
            for (int p = 0; p < parts.length; p++) {
                String part = parts[p];
                if (part.equals("-")) continue;
                // For hyphen parts, treat positions as: if overall token is first/last, mark accordingly only for edges
                boolean partIsEdge = (p == 0) || (p == parts.length - 1);
                parts[p] = processWordPart(part, isFirst && partIsEdge, isLast && partIsEdge, prevForcesCapital, locale);
            }
            core = String.join("", parts);
            return leading + core + trailing;
        }

        // Normal single word
        core = processWordPart(core, isFirst, isLast, prevForcesCapital, locale);
        return leading + core + trailing;
    }

    // Process one word "part" (no hyphens/slashes) and apply capitalization rules.
    private static String processWordPart(String word, boolean isFirst, boolean isLast,
                                          boolean prevForcesCapital, Locale locale) {
        if (word.isEmpty()) return word;

        // If word contains any internal uppercase letters (not just first char), treat as "preserve" (iPhone, eBay)
        if (hasInternalUppercase(word)) return word;

        // If word is ALL CAPS and length > 1, preserve (acronyms)
        if (isAllCaps(word)) return word;

        String lower = word.toLowerCase(locale);

        // Decide if it's a minor word (lowercase in titles)
        boolean isMinor = MINOR_WORDS.contains(lower);

        // Force capitalization if it's first, last, or previous token forced capitalization.
        if (isFirst || isLast || prevForcesCapital) {
            return capitalizeFirstLetterPreserveApostrophes(lower, locale);
        }

        if (isMinor) {
            return lower;
        } else {
            return capitalizeFirstLetterPreserveApostrophes(lower, locale);
        }
    }

    // Capitalize first alphabetic character, preserve later characters (but lowercased input provided)
    private static String capitalizeFirstLetterPreserveApostrophes(String lower, Locale locale) {
        int len = lower.length();
        for (int i = 0; i < len; i++) {
            char c = lower.charAt(i);
            if (Character.isLetter(c)) {
                String head = lower.substring(0, i);
                String first = String.valueOf(lower.charAt(i)).toUpperCase(locale);
                String tail = lower.substring(i + 1);
                // Special: for contractions like "o'clock" -> "O'Clock" (tail already lowercased)
                return head + first + tail;
            }
        }
        // No letter found (digits-only?), just return as-is
        return lower;
    }

    private static boolean hasInternalUppercase(String s) {
        // internal uppercase meaning any uppercase after index 0
        for (int i = 1; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean isAllCaps(String s) {
        int letters = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                letters++;
                if (!Character.isUpperCase(c)) return false;
            }
        }
        return letters > 0;
    }
}