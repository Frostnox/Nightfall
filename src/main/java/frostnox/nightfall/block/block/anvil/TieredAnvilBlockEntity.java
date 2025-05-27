package frostnox.nightfall.block.block.anvil;

import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.IMicroGrid;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.MenuContainerBlockEntity;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.TieredAnvilRecipe;
import frostnox.nightfall.item.ItemStackHandlerNF;
import frostnox.nightfall.item.item.MeleeWeaponItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.world.AnvilSlagToClient;
import frostnox.nightfall.network.message.world.GridUseToClient;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.math.noise.FractalSimplexNoiseFast;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TieredAnvilBlockEntity extends MenuContainerBlockEntity implements MenuProvider, IMicroGrid {
    public static final int GRID_X = 14;
    public static final int GRID_Y = 6;
    public static final int GRID_Z = 8;
    private static final int HEAT_TIME = 20 * 90;
    private final ItemStackHandlerNF result;
    public final boolean[][][] grid = new boolean[GRID_X][GRID_Y][GRID_Z];
    public final boolean[][][] slag = new boolean[GRID_X][GRID_Y][GRID_Z];
    private ResourceLocation recipeLocation;
    private TieredHeat cachedHeat = TieredHeat.NONE;
    private int heatTicks, tickCount;
    private boolean finished = false;
    public boolean inProgress = false;
    public final Color[][][] gridColors = new Color[GRID_X][GRID_Y][GRID_Z];

    public TieredAnvilBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.ANVIL.get(), pos, state);
    }

    protected TieredAnvilBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        result = new ItemStackHandlerNF();
    }

    @Override
    public ItemStackHandlerNF getInventory() {
        return result;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent(Nightfall.MODID + ".anvil");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new TieredAnvilContainer(pContainerId, pInventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.getAllKeys().contains("recipe")) setRecipeID(ResourceLocation.parse(tag.getString("recipe")));
        if(tag.contains("heat_ticks")) heatTicks = tag.getInt("heat_ticks");
        if(tag.contains("finished")) finished = tag.getBoolean("finished");
        if(tag.contains("in_progress")) inProgress = tag.getBoolean("in_progress");
        ListTag activeCubes = tag.getList("active_cubes", ListTag.TAG_STRING);
        ListTag activeSlag = tag.getList("active_slag", ListTag.TAG_STRING);
        for(int x = 0; x < getGridXSize(); x++) {
            for(int y = 0; y < getGridYSize(); y++) {
                for(int z = 0; z < getGridZSize(); z++) {
                    grid[x][y][z] = activeCubes.contains(StringTag.valueOf(IMicroGrid.idFromPos(x, y, z)));
                    slag[x][y][z] = activeSlag.contains(StringTag.valueOf(IMicroGrid.idFromPos(x, y, z)));
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(recipeLocation != null) tag.putString("recipe", recipeLocation.toString());
        tag.putInt("heat_ticks", heatTicks);
        tag.putBoolean("finished", finished);
        tag.putBoolean("in_progress", inProgress);
        ListTag activeCubes = new ListTag(), activeSlag = new ListTag();
        for(int x = 0; x < getGridXSize(); x++) {
            for(int y = 0; y < getGridYSize(); y++) {
                for(int z = 0; z < getGridZSize(); z++) {
                    if(grid[x][y][z]) activeCubes.add(StringTag.valueOf(IMicroGrid.idFromPos(x, y, z)));
                    if(slag[x][y][z]) activeSlag.add(StringTag.valueOf(IMicroGrid.idFromPos(x, y, z)));
                }
            }
        }
        tag.put("active_cubes", activeCubes);
        tag.put("active_slag", activeSlag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TieredAnvilBlockEntity entity) {
        entity.tickCount++;
        entity.heatTicks -= level.isRainingAt(pos.above()) ? 2 : 1;
        if(entity.heatTicks < HEAT_TIME / 2 && entity.tickCount % 20 == 0) {
            if(entity.hasHeatSource(entity.recipeLocation)) entity.heatTicks = HEAT_TIME;
            else if(entity.heatTicks <= 0) entity.destroyGrid();
        }
        if(entity.heatTicks > 0 && entity.tickCount % 12 == 0) entity.tryToFinishItem();
        entity.setChanged();
    }

    @Override
    public boolean canUseGrid(Action action) {
        return action.is(TagsNF.SMITHING_ACTION);
    }

    @Override
    public boolean isValidActionType(int type) {
        return type > -1 && type < AnvilAction.values().length;
    }

    @Override
    public boolean useGrid(int ordinal, Vec3i hitPos, Player player, ItemStack item) {
        if(level != null && level.isClientSide() || !inProgress) return false; //Only allow server to change grid
        int x = hitPos.getX();
        int y = hitPos.getY();
        int z = hitPos.getZ();
        //Check for incorrect position
        if(!gridHas(x, y, z)) return false;
        Optional<?> recipe = level.getRecipeManager().byKey(recipeLocation);
        if(recipe.isEmpty() || !(recipe.get() instanceof TieredAnvilRecipe anvilRecipe)) return false;
        boolean badTool = !(item.getItem() instanceof MeleeWeaponItem tool) || tool.material.getTier() < anvilRecipe.getTier() - 1;
        Vec3 gridPos = getWorldPos(worldPosition, (x + 0.5F) / 16, (y + 0.5F) / 16, (z + 0.5F) / 16);
        if(getCachedHeat() != TieredHeat.NONE) {
            ((ServerLevel) level).sendParticles(getCachedHeat().getSparkParticle().get(), gridPos.x(), gridPos.y(), gridPos.z(),
                    (badTool ? 2 : 11) + level.random.nextInt(5), 0, 0, 0, 0.12F);
        }
        level.playSound(null, gridPos.x, gridPos.y, gridPos.z, SoundsNF.ANVIL_STRIKE.get(), SoundSource.BLOCKS, 1F, 1F);
        if(!player.getAbilities().instabuild) item.hurtAndBreak(badTool ? 2 : 1, player, (p) -> p.broadcastBreakEvent(PlayerData.get(p).getActiveHand()));
        if(badTool) return false;
        CompoundTag changedCubes = new CompoundTag(), changedSlag = new CompoundTag(); //Update only the cubes that changed
        int xMove = 0, zMove = 0;
        float userYaw = ((player.getYRot() >= 0F ? player.getYRot() : (360F + player.getYRot())) + getRotationDegrees()) % 360F;
        if(userYaw >= 135F && userYaw < 225F) zMove = 1;
        else if(userYaw < 45F || userYaw > 315F) zMove = -1;
        else if(userYaw >= 45F && userYaw < 135F) xMove = 1;
        else xMove = -1;
        AnvilAction action = AnvilAction.values()[ordinal];
        switch(action) {
            case PUNCH -> punchCube(x, y, z, changedCubes, changedSlag);
            case PUNCH_LINE -> {
                punchCube(x, y, z, changedCubes, changedSlag);
                if(gridHas(x+zMove, y, z+xMove)) punchCube(x+zMove, y, z+xMove, changedCubes, changedSlag);
                if(gridHas(x-zMove, y, z-xMove)) punchCube(x-zMove, y, z-xMove, changedCubes, changedSlag);
            }
            case PUNCH_SQUARE -> {
                for(int gridX = x - 1; gridX <= x + 1; gridX++) {
                    for(int gridZ = z - 1; gridZ <= z + 1; gridZ++) {
                        if(gridHas(gridX, y, gridZ)) punchCube(gridX, y, gridZ, changedCubes, changedSlag);
                    }
                }
            }
            case BEND -> {
                breakSlag(x, y, z, changedSlag);
                if(!gridHas(x, y + 1, z)) {
                    if(xMove != 0) {
                        if(!gridHas(x - xMove, y, z) || !gridHas(x + xMove, y, z)) {
                            if(!gridHas(x + xMove, y, z)) xMove *= -1;
                            int newX = x + xMove;
                            while(newX >= 0 && newX < getGridXSize()) {
                                if(!grid[newX][y][z]) {
                                    moveCube(x, y, z, newX, y, z, changedCubes);
                                    break;
                                }
                                newX += xMove;
                            }
                        }
                    }
                    else {
                        if(!gridHas(x, y, z - zMove) || !gridHas(x, y, z + zMove)) {
                            if(!gridHas(x, y, z + zMove)) zMove *= -1;
                            int newZ = z + zMove;
                            while(newZ >= 0 && newZ < getGridZSize()) {
                                if(!grid[x][y][newZ]) {
                                    moveCube(x, y, z, x, y, newZ, changedCubes);
                                    break;
                                }
                                newZ += zMove;
                            }
                        }
                    }
                }
            }
            case DRAW -> {
                drawCube(x, y, z, xMove, zMove, changedCubes, changedSlag);
                if(gridHas(x+zMove, y, z+xMove)) drawCube(x+zMove, y, z+xMove, xMove, zMove, changedCubes, changedSlag);
                if(gridHas(x-zMove, y, z-xMove)) drawCube(x-zMove, y, z-xMove, xMove, zMove, changedCubes, changedSlag);
            }
            case DRAW_LINE -> {
                drawCube(x, y, z, xMove, zMove, changedCubes, changedSlag);
                if(zMove != 0) {
                    for(int i = 1; gridContainsLocation(x + zMove * i, y, z); i++) {
                        if(gridHas(x+zMove*i, y, z)) drawCube(x+zMove*i, y, z, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                    for(int i = 1; gridContainsLocation(x - zMove * i, y, z); i++) {
                        if(gridHas(x-zMove*i, y, z)) drawCube(x-zMove*i, y, z, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                }
                else {
                    for(int i = 1; gridContainsLocation(x, y, z + xMove * i); i++) {
                        if(gridHas(x, y, z+xMove*i)) drawCube(x, y, z+xMove*i, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                    for(int i = 1; gridContainsLocation(x, y, z - xMove * i); i++) {
                        if(gridHas(x, y, z-xMove*i)) drawCube(x, y, z-xMove*i, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                }
            }
            case UPSET -> {
                xMove *= -1;
                zMove *= -1;
                upsetCube(x, y, z, xMove, zMove, changedCubes, changedSlag);
                if(gridHas(x+zMove, y, z+xMove)) upsetCube(x+zMove, y, z+xMove, xMove, zMove, changedCubes, changedSlag);
                if(gridHas(x-zMove, y, z-xMove)) upsetCube(x-zMove, y, z-xMove, xMove, zMove, changedCubes, changedSlag);
            }
            case UPSET_LINE -> {
                xMove *= -1;
                zMove *= -1;
                upsetCube(x, y, z, xMove, zMove, changedCubes, changedSlag);
                if(zMove != 0) {
                    for(int i = 1; gridContainsLocation(x + zMove * i, y, z); i++) {
                        if(gridHas(x+zMove*i, y, z)) upsetCube(x+zMove*i, y, z, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                    for(int i = 1; gridContainsLocation(x - zMove * i, y, z); i++) {
                        if(gridHas(x-zMove*i, y, z)) upsetCube(x-zMove*i, y, z, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                }
                else {
                    for(int i = 1; gridContainsLocation(x, y, z + xMove * i); i++) {
                        if(gridHas(x, y, z+xMove*i)) upsetCube(x, y, z+xMove*i, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                    for(int i = 1; gridContainsLocation(x, y, z - xMove * i); i++) {
                        if(gridHas(x, y, z-xMove*i)) upsetCube(x, y, z-xMove*i, xMove, zMove, changedCubes, changedSlag);
                        else break;
                    }
                }
            }
            default -> {
                Nightfall.LOGGER.error("Missing anvil grid interaction type '" + action +"'.");
                return false;
            }
        }
        if(!changedCubes.getAllKeys().isEmpty()) {
            if(IMicroGrid.compare(grid, anvilRecipe.getFinishShape()) == -1) {
                destroyGrid(); //Not enough metal left to complete finish shape
                return true;
            }
            else {
                //Gravitate floating sections
                ObjectOpenHashSet<Vec3i> visited = new ObjectOpenHashSet<>(GRID_X * GRID_Y * GRID_Z / 2);
                ObjectArrayList<Vec3i> positions = new ObjectArrayList<>(32);
                List<IntObjectPair<List<Vec3i>>> components = new ObjectArrayList<>(6); //Connected sub-grids of grid
                //Build components based off changed cubes
                for(String id : changedCubes.getAllKeys()) {
                    if(changedCubes.getBoolean(id)) continue;
                    Vec3i startPos = IMicroGrid.posFromId(id);
                    for(Direction startDir : Direction.values()) {
                        int startNeighborX = startPos.getX() + startDir.getStepX();
                        int startNeighborY = startPos.getY() + startDir.getStepY();
                        int startNeighborZ = startPos.getZ() + startDir.getStepZ();
                        if(gridHas(startNeighborX, startNeighborY, startNeighborZ)) {
                            positions.push(new Vec3i(startNeighborX, startNeighborY, startNeighborZ));
                            int minY = GRID_Y - 1;
                            List<Vec3i> component = new ObjectArrayList<>(32);
                            while(!positions.isEmpty()) {
                                Vec3i pos = positions.pop();
                                if(visited.contains(pos)) continue;
                                visited.add(pos);
                                component.add(pos);
                                if(pos.getY() < minY) minY = pos.getY();
                                for(Direction dir : Direction.values()) {
                                    int neighborX = pos.getX() + dir.getStepX();
                                    int neighborY = pos.getY() + dir.getStepY();
                                    int neighborZ = pos.getZ() + dir.getStepZ();
                                    if(gridHas(neighborX, neighborY, neighborZ)) positions.push(new Vec3i(neighborX, neighborY, neighborZ));
                                }
                            }
                            if(!component.isEmpty() && minY > 0) components.add(new IntObjectImmutablePair<>(minY, component));
                        }
                    }
                }
                components.sort(Comparator.comparingInt(IntObjectPair::firstInt)); //Make sure lower sections fall first
                //Gravitate cubes
                Set<Vec3i> fallingCubes = new ObjectOpenHashSet<>(GRID_X * GRID_Y * GRID_Z / 2);
                for(IntObjectPair<List<Vec3i>> pair : components) {
                    List<Vec3i> component = pair.second();
                    component.sort(Comparator.comparingInt(Vec3i::getY)); //Mark sure lower cubes fall first
                    Vec3i lowPos = component.get(0);
                    int minY = lowPos.getY();
                    while(minY > 0 && !grid[lowPos.getX()][minY - 1][lowPos.getZ()]) minY--;
                    if(minY != lowPos.getY()) {
                        int yChange = lowPos.getY() - minY;
                        for(Vec3i pos : component) {
                            //Don't remove if a cube fell into this position
                            if(!fallingCubes.contains(pos)) {
                                grid[pos.getX()][pos.getY()][pos.getZ()] = false;
                                changedCubes.putBoolean(IMicroGrid.idFromPos(pos.getX(), pos.getY(), pos.getZ()), false);
                            }
                            breakSlag(pos.getX(), pos.getY(), pos.getZ(), changedSlag);
                            int fallY = pos.getY() - yChange;
                            grid[pos.getX()][fallY][pos.getZ()] = true;
                            changedCubes.putBoolean(IMicroGrid.idFromPos(pos.getX(), fallY, pos.getZ()), true);
                            fallingCubes.add(new Vec3i(pos.getX(), fallY, pos.getZ()));
                        }
                    }
                }
            }
        }
        boolean updateCubes = !changedCubes.getAllKeys().isEmpty(), updateSlag = !changedSlag.getAllKeys().isEmpty();
        if(updateCubes || updateSlag) {
            if(IMicroGrid.equals(grid, anvilRecipe.getFinishShape())) {
                finished = true;
                if(!tryToFinishItem()) {
                    if(updateCubes) NetworkHandler.toAllTrackingChunk(level.getChunkAt(getBlockPos()), new GridUseToClient(getBlockPos(), changedCubes));
                    if(updateSlag) NetworkHandler.toAllTrackingChunk(level.getChunkAt(getBlockPos()), new AnvilSlagToClient(getBlockPos(), changedSlag));
                }
            }
            else {
                finished = false;
                if(updateCubes) NetworkHandler.toAllTrackingChunk(level.getChunkAt(getBlockPos()), new GridUseToClient(getBlockPos(), changedCubes));
                if(updateSlag) NetworkHandler.toAllTrackingChunk(level.getChunkAt(getBlockPos()), new AnvilSlagToClient(getBlockPos(), changedSlag));
            }
        }
        setChanged();
        return true;
    }

    public void destroyGrid() {
        double x = worldPosition.getX() + 0.5, y = worldPosition.getY() + 1, z = worldPosition.getZ() + 0.5;
        if(getCachedHeat() != TieredHeat.NONE) {
            ((ServerLevel) level).sendParticles(getCachedHeat().getSparkParticle().get(), x, y, z,
                    20 + level.random.nextInt(5), 4/32D, 1D/16D, 4/32D, 0.003F);
        }
        level.playSound(null, x, y, z, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1F, 1F);
        clearGrid();
        IMicroGrid.clearGrid(slag);
        inProgress = false;
        level.setBlock(worldPosition, getBlockState().setValue(TieredAnvilBlock.HAS_METAL, false), 2);
    }

    private void breakSlag(int x, int y, int z, CompoundTag changedSlag) {
        if(gridContainsLocation(x, y, z) && slag[x][y][z]) {
            slag[x][y][z] = false;
            changedSlag.putBoolean(IMicroGrid.idFromPos(x, y ,z), false);
        }
    }

    private TieredHeat getCachedHeat() {
        if(cachedHeat == TieredHeat.NONE) {
            Optional<?> recipe = level.getRecipeManager().byKey(recipeLocation);
            if(recipe.isPresent() && recipe.get() instanceof TieredAnvilRecipe anvilRecipe) {
                cachedHeat = TieredHeat.fromTier(anvilRecipe.getTier());
            }
        }
        return cachedHeat;
    }

    private boolean tryToFinishItem() {
        if(finished && IMicroGrid.isEmpty(slag)) {
            Optional<?> recipe = level.getRecipeManager().byKey(recipeLocation);
            if(recipe.isPresent() && recipe.get() instanceof TieredAnvilRecipe anvilRecipe && (hasWaterSource() || anvilRecipe.getTier() == 0)) {
                double itemX = worldPosition.getX() + 0.5, itemY = worldPosition.getY() + 1, itemZ = worldPosition.getZ() + 0.5;
                if(anvilRecipe.getTier() > 0) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, itemX, itemY, itemZ,
                            20 + level.random.nextInt(5), 4 / 32D, 1D / 16D, 4 / 32D, 0.003F);
                    level.playSound(null, itemX, itemY, itemZ, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);
                }
                result.setStackInSlot(0, anvilRecipe.getResultItem().copy());
                clearGrid();
                inProgress = false;
                heatTicks = 0;
                finished = false;
                level.setBlock(worldPosition, getBlockState().setValue(TieredAnvilBlock.HAS_METAL, false), 2);
                return true;
            }
        }
        return false;
    }

    private void punchCube(int x, int y, int z, CompoundTag changedCubes, CompoundTag changedSlag) {
        breakSlag(x, y, z, changedSlag);
        grid[x][y][z] = false;
        changedCubes.putBoolean(IMicroGrid.idFromPos(x, y, z), false);
    }

    private void drawCube(int x, int y, int z, int xMove, int zMove, CompoundTag changedCubes, CompoundTag changedSlag) {
        breakSlag(x, y, z, changedSlag);
        if(y > 0 && !gridHas(x, y+1, z) && (!gridHas(x + xMove, y, z + zMove) || !gridHas(x - xMove, y, z - zMove))) {
            if(!grid[x][y-1][z]) { //Try to move down first
                moveCube(x, y, z, x, y-1, z, changedCubes);
            }
            else if(xMove != 0) {
                int newX = x + xMove;
                while(newX >= 0 && newX < getGridXSize() && !grid[newX][y][z]) {
                    if(!grid[newX][y-1][z]) {
                        moveCube(x, y, z, newX, y-1, z, changedCubes);
                        return;
                    }
                    newX += xMove;
                }
            }
            else if(zMove != 0) {
                int newZ = z + zMove;
                while(newZ >= 0 && newZ < getGridZSize() && !grid[x][y][newZ]) {
                    if(!grid[x][y-1][newZ]) {
                        moveCube(x, y, z, x, y-1, newZ, changedCubes);
                        return;
                    }
                    newZ += zMove;
                }
            }
        }
    }

    private void upsetCube(int x, int y, int z, int xMove, int zMove, CompoundTag changedCubes, CompoundTag changedSlag) {
        breakSlag(x, y, z, changedSlag);
        if(y < getGridYSize() - 1 && !grid[x][y + 1][z] && (!gridHas(x + xMove, y, z + zMove) || !gridHas(x - xMove, y, z - zMove))) {
            if(hasAdjacencyExcluding(x, y + 1, z, x, y, z)) { //Try to move up first
                moveCube(x, y, z, x, y + 1, z, changedCubes);
            }
            else if(xMove != 0) {
                int newX = x + xMove;
                int newY = !grid[newX][y][z] ? y : (y + 1);
                while(newX >= 0 && newX < getGridXSize()) {
                    if(grid[newX][newY][z] || (!grid[newX][newY][z] && !gridHas(newX, newY - 1, z))) break;
                    else newX += xMove;
                }
                if(!gridHas(newX - xMove, newY, z)) moveCube(x, y, z, newX - xMove, newY, z, changedCubes);
            }
            else if(zMove != 0) {
                int newZ = z + zMove;
                int newY = !grid[x][y][newZ] ? y : (y + 1);
                while(newZ >= 0 && newZ < getGridZSize()) {
                    if(grid[x][newY][newZ] || (!grid[x][newY][newZ] && !gridHas(x, newY - 1, newZ))) break;
                    else newZ += zMove;
                }
                if(!gridHas(x, newY, newZ - zMove)) moveCube(x, y, z, x, newY, newZ - zMove, changedCubes);
            }
        }
    }

    private void moveCube(int x, int y, int z, int xTo, int yTo, int zTo, CompoundTag changedCubes) {
        grid[x][y][z] = false;
        changedCubes.putBoolean(IMicroGrid.idFromPos(x, y, z), false);
        grid[xTo][yTo][zTo] = true;
        changedCubes.putBoolean(IMicroGrid.idFromPos(xTo, yTo, zTo), true);
    }

    public boolean hasWaterSource() {
        for(BlockPos searchPos : BlockPos.betweenClosed(worldPosition.offset(-2, -1, -2), worldPosition.offset(2, 1, 2))) {
            BlockState state = level.getBlockState(searchPos);
            if(state.getFluidState().is(TagsNF.FRESHWATER)) {
                return true;
            }
        }
        return false;
    }

    public TieredHeat getBestHeatSource() {
        if(level == null) return TieredHeat.NONE;
        TieredHeat bestHeat = TieredHeat.NONE;
        for(BlockPos searchPos : BlockPos.betweenClosed(worldPosition.offset(-2, -1, -2), worldPosition.offset(2, 1, 2))) {
            BlockState state = level.getBlockState(searchPos);
            if(state.getBlock() instanceof IHeatSource heatSource) {
                TieredHeat heat = heatSource.getHeat(level, searchPos, state);
                if(heat.getTier() > bestHeat.getTier()) bestHeat = heat;
            }
        }
        return bestHeat;
    }

    public boolean hasHeatSource(ResourceLocation recipeId) {
        if(level == null) return false;
        Optional<?> recipe = level.getRecipeManager().byKey(recipeId);
        if(recipe.isPresent() && recipe.get() instanceof TieredAnvilRecipe anvilRecipe) {
            int minTier = anvilRecipe.getTier();
            for(BlockPos searchPos : BlockPos.betweenClosed(worldPosition.offset(-2, -1, -2), worldPosition.offset(2, 1, 2))) {
                BlockState state = level.getBlockState(searchPos);
                if(state.getBlock() instanceof IHeatSource heatSource && heatSource.getHeat(level, searchPos, state).getTier() >= minTier) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canStartSmithing(Player player, ResourceLocation recipeId) {
        if(level == null || inProgress) return false;
        Optional<?> recipe = level.getRecipeManager().byKey(recipeId);
        if(recipe.isPresent() && recipe.get() instanceof TieredAnvilRecipe anvilRecipe) {
            if(anvilRecipe.getTier() <= ((TieredAnvilBlock) getBlockState().getBlock()).tier && anvilRecipe.isUnlocked(player)) {
                return anvilRecipe.getTier() == 0 || hasHeatSource(recipeId);
            }
        }
        return false;
    }

    public boolean startSmithing(ResourceLocation recipeId) {
        if(level == null || level.isClientSide() || inProgress) return false;
        setRecipeID(recipeId);
        inProgress = true;
        heatTicks = HEAT_TIME;
        Optional<?> recipe = level.getRecipeManager().byKey(recipeLocation);
        IMicroGrid.clearGrid(slag);
        if(recipe.isPresent() && recipe.get() instanceof TieredAnvilRecipe anvilRecipe) {
            double pX = worldPosition.getX() + 0.5, pY = worldPosition.getY() + 1 + 2D/16D, pZ = worldPosition.getZ() + 0.5;
            if(getCachedHeat() != TieredHeat.NONE) {
                ((ServerLevel) level).sendParticles(getCachedHeat().getFlameParticle().get(), pX, pY, pZ,
                        14 + level.random.nextInt(5), 4/32D, 1D/16D, 4/32D, 0.008F);
                level.playSound(null, pX, pY, pZ, SoundsNF.FIRE_WHOOSH.get(), SoundSource.BLOCKS, 1F, 1F);
            }
            int[][][] startShape = anvilRecipe.getStartShape();
            List<Vec3i> randPositions = new ObjectArrayList<>(32);
            for(int x = 0; x < GRID_X; x++) {
                for(int y = 0; y < GRID_Y; y++) {
                    for(int z = 0; z < GRID_Z; z++) {
                        switch(startShape[x][y][z]) {
                            case 0 -> grid[x][y][z] = false;
                            case 1 -> grid[x][y][z] = true;
                            default -> randPositions.add(new Vec3i(x, y, z));
                        }
                    }
                }
            }
            if(!randPositions.isEmpty()) {
                int randAmount = anvilRecipe.getRandMin() + level.random.nextInt(anvilRecipe.getRandMax() - anvilRecipe.getRandMin());
                while(randAmount >= 0 && !randPositions.isEmpty()) {
                    Vec3i pos = randPositions.remove(level.random.nextInt(randPositions.size()));
                    grid[pos.getX()][pos.getY()][pos.getZ()] = true;
                    randAmount--;
                }
            }
            float slagChance = anvilRecipe.getSlagChance();
            if(slagChance > 0F) {
                for(int slagX = 0; slagX < GRID_X; slagX++) {
                    for(int slagY = 0; slagY < GRID_Y; slagY++) {
                        for(int slagZ = 0; slagZ < GRID_Z; slagZ++) {
                            if(grid[slagX][slagY][slagZ] && canSlagForm(slagX, slagY, slagZ) && level.random.nextFloat() < slagChance) {
                                slag[slagX][slagY][slagZ] = true;
                            }
                        }
                    }
                }
            }
        }
        else clearGrid();
        level.setBlock(worldPosition, getBlockState().setValue(TieredAnvilBlock.HAS_METAL, true), 2);
        return true;
    }

    private boolean canSlagForm(int x, int y, int z) {
        return !gridHas(x + 1, y, z) || !gridHas(x - 1, y, z) || !gridHas(x, y + 1, z) || !gridHas(x, y, z + 1) || !gridHas(x, y, z - 1);
    }

    public ItemStack getResult() {
        return result.getStackInSlot(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && !inProgress;
    }

    @Override
    public boolean[][][] getGrid() {
        return grid;
    }

    @Override
    public int getGridXSize() {
        return GRID_X;
    }

    @Override
    public int getGridYSize() {
        return GRID_Y;
    }

    @Override
    public int getGridZSize() {
        return GRID_Z;
    }

    @Override
    public Vector3f getWorldGridOffset() {
        return new Vector3f(1F/16F, 1, 4F/16F);
    }

    @Override
    public float getRotationDegrees() {
        Direction direction = getBlockState().getValue(TieredAnvilBlock.FACING);
        return switch (direction) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        };
    }

    @Override
    public ResourceLocation getRecipeID() {
        return recipeLocation;
    }

    @Override
    public void setRecipeID(ResourceLocation id) {
        if(level != null) {
            Optional<? extends Recipe<?>> recipe = level.getRecipeManager().byKey(id);
            if(recipe.isPresent() && recipe.get() instanceof TieredAnvilRecipe anvilRecipe) {
                cachedHeat = TieredHeat.fromTier(anvilRecipe.getTier());
                if(level.isClientSide && !id.equals(recipeLocation)) {
                    Color metalColor = cachedHeat == TieredHeat.NONE ? LevelUtil.getMetalColor(anvilRecipe.getResultItem()) : cachedHeat.color;
                    FractalSimplexNoiseFast noise = new FractalSimplexNoiseFast(level.random.nextLong(), 0.107F, 2, 0.5F, 2.0F);
                    for(int x = 0; x < getGridXSize(); x++) {
                        for(int y = 0; y < getGridYSize(); y++) {
                            for(int z = 0; z < getGridZSize(); z++) {
                                int gb = Math.round(noise.noise3D(x, y, z) * 40);
                                gridColors[x][y][z] = new Color(Mth.clamp(metalColor.getRed() + Math.round(noise.noise3D(x, y, z) * 40), 0, 255),
                                        Mth.clamp(metalColor.getGreen() + gb, 0, 255),
                                        Mth.clamp(metalColor.getBlue() + gb, 0, 255));
                            }
                        }
                    }
                }
            }
        }
        recipeLocation = id;
    }
}
