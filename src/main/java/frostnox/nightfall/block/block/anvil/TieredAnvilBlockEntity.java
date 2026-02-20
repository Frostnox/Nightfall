package frostnox.nightfall.block.block.anvil;

import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.item.item.MeleeWeaponItem;
import frostnox.nightfall.item.item.TongsItem;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.awt.*;

public class TieredAnvilBlockEntity extends BlockEntity {
    public int[] work = new int[11];
    protected @Nullable Item item;
    protected AnvilSection section;
    protected float temperature;
    protected int stableTempTicks;
    protected Color color;
    protected AABB[] cachedBoxes = new AABB[3];
    protected boolean slagCenter, slagLeft, slagRight, flip;
    protected boolean dirtyCache = true;

    public TieredAnvilBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.ANVIL.get(), pos, state);
    }

    protected TieredAnvilBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ItemStack getWorkpiece() {
        return hasWorkpiece() ? new ItemStack(item) : ItemStack.EMPTY;
    }

    public boolean hasWorkpiece() {
        return item != null;
    }

    public float getTemperature() {
        return temperature;
    }

    public Color getColor() {
        return color;
    }

    public AnvilSection getRawSection() {
        return section;
    }

    public AnvilSection getSection() {
        if(section == AnvilSection.HORN && !((TieredAnvilBlock) getBlockState().getBlock()).hasHorn) return AnvilSection.EDGE;
        else return section;
    }

    public boolean hasFlip() {
        return flip;
    }

    public boolean hasSlagRight() {
        return slagRight;
    }

    public boolean hasSlagCenter() {
        return slagCenter;
    }

    public boolean hasSlagLeft() {
        return slagLeft;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("item")) {
            work = tag.getIntArray("work");
            item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(tag.getString("item")));
            section = AnvilSection.values()[tag.getInt("section")];
            temperature = tag.getFloat("temperature");
            stableTempTicks = tag.getInt("stableTempTicks");
            color = new Color(tag.getInt("color"));
            slagCenter = tag.getBoolean("slagCenter");
            slagLeft = tag.getBoolean("slagLeft");
            slagRight = tag.getBoolean("slagRight");
            flip = tag.getBoolean("flip");
        }
        else item = null;
        dirtyCache = true;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(hasWorkpiece()) {
            tag.putIntArray("work", work);
            tag.putString("item", ForgeRegistries.ITEMS.getKey(item).toString());
            tag.putInt("section", section.ordinal());
            tag.putFloat("temperature", temperature);
            tag.putInt("stableTempTicks", stableTempTicks);
            tag.putInt("color", color.getRGB());
            tag.putBoolean("slagCenter", slagCenter);
            tag.putBoolean("slagLeft", slagLeft);
            tag.putBoolean("slagRight", slagRight);
            tag.putBoolean("flip", flip);
        }
        else tag.putBoolean("dummy", false);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB box = getBlockState().getShape(level, getBlockPos()).bounds().move(getBlockPos());
        if(hasWorkpiece()) {
            Direction dir = getBlockState().getValue(TieredAnvilBlock.FACING);
            float x = Math.abs(dir.getStepX()) * 3.5F/16F, z = Math.abs(dir.getStepZ()) * 3.5F/16F;
            return new AABB(box.minX - x, box.minY, box.minZ - z, box.maxX + x, box.maxY + 7F/16F, box.maxZ + z);
        }
        return box;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TieredAnvilBlockEntity entity) {
        tick(level, pos, state, entity, 1);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TieredAnvilBlockEntity entity, int ticks) {
        boolean raining = level.isRainingAt(pos.above());
        if(entity.stableTempTicks > 0) {
            entity.stableTempTicks = Math.max(0, entity.stableTempTicks - (raining ? 2 * ticks : ticks));
            entity.setChanged();
        }
        else if(entity.temperature > 0) {
            entity.temperature = Math.max(0, entity.temperature - (raining ? ticks : ticks * 0.5F));
            entity.setChanged();
        }
    }

    public void actWorkpiece(AnvilAction action, int index, Player player, ItemStack tool) {
        if(level != null && level.isClientSide) return;
        switch(action) {
            case STRIKE, CUT -> {
                IMetal metal = Metal.fromString(item.toString());
                boolean badTool = !(tool.getItem() instanceof MeleeWeaponItem weapon) || weapon.material.getTier() < metal.getTier() - 1;
                TieredHeat heat = TieredHeat.fromTemp(temperature);
                AABB box = getWorkpieceBoxes()[index];
                Vec3 center = box.getCenter();
                if(heat != TieredHeat.NONE) {
                    ((ServerLevel) level).sendParticles(heat.getSparkParticle().get(), center.x, center.y, center.z,
                            (badTool ? 2 : 8) + level.random.nextInt(5), 0, 0, 0, 0.12F);
                }
                level.playSound(null, center.x, center.y, center.z, SoundsNF.ANVIL_STRIKE.get(), SoundSource.BLOCKS, 1F, 1F);
                if(!player.getAbilities().instabuild) tool.hurtAndBreak(badTool ? 2 : 1, player, (p) -> p.broadcastBreakEvent(PlayerData.get(p).getActiveHand()));
                if(!badTool && heat.getTier() >= metal.getWorkTier()) {
                    if(action == AnvilAction.STRIKE) {
                        if(index == 0 ? slagCenter : (index == 1 ? slagLeft : slagRight)) {
                            if(index == 0) slagCenter = false;
                            else if(index == 1) slagLeft = false;
                            else slagRight = false;
                            ((ServerLevel) level).sendParticles(ParticleTypesNF.SPARK_SLAG.get(), center.x, center.y, center.z,
                                    10 + level.random.nextInt(5), 0, 0, 0, 0.06F);
                        }
                        else {
                            int strength = ActionTracker.get(player).getCharge() > 0 ? 2 : 1;
                            AnvilSection section = getSection();
                            switch(section) {
                                case HORN -> {
                                    if(index == 0) {
                                        if(work[0] < 3) work[0] = Math.min(3, work[0] + strength);
                                    }
                                    else if(index == 1) {
                                        if(work[5] < 2) work[5] = Math.min(2, work[5] + strength);
                                    }
                                    else if(work[7] < 3) work[7] = Math.min(3, work[7] + strength);
                                }
                                case FLAT -> {
                                    if(index == 0) {
                                        if(work[0] < 3) work[0] = Math.min(3, work[0] + strength);
                                    }
                                    else if(index == 1) {
                                        if(work[3] < 3) work[3] = Math.min(3, work[3] + strength);
                                    }
                                    else if(work[7] < 3) work[7] = Math.min(3, work[7] + strength);
                                }
                                case EDGE -> {
                                    if(index == 0) {
                                        if(work[1] < 3) work[1] = Math.min(3, work[1] + strength);
                                    }
                                    else if(index == 1) {
                                        if(work[3] < 3) work[3] = Math.min(3, work[3] + strength);
                                    }
                                    else if(work[8] < 3) work[8] = Math.min(3, work[8] + strength);
                                }
                            }
                            if(section != AnvilSection.HORN) {
                                if(index == 0) {
                                    if(work[0] + work[1] > 3) {
                                        work[0] = 0;
                                        work[1] = 0;
                                        work[2] = 3;
                                    }
                                }
                                else if(index == 1) {
                                    if(work[2] + work[3] > 3) {
                                        work[3] = 0;
                                        work[4] = 0;
                                        work[5] = 0;
                                        work[6] = 3;
                                    }
                                }
                                else {
                                    if(work[5] + work[6] > 3) {
                                        work[7] = 0;
                                        work[8] = 0;
                                        work[9] = 0;
                                        work[10] = 3;
                                    }
                                }
                            }
                        }
                    }
                    else {
                        int strength = ActionTracker.get(player).getCharge() > 0 ? 3 : 1;
                        if(index == 0) {
                            if(work[2] < 3) work[2] = Math.min(3, work[2] + strength);
                            if(work[2] == 3) {
                                work[0] = 0;
                                work[1] = 0;
                            }
                        }
                        else if(index == 1) {
                            if(work[6] < 3) work[6] = Math.min(3, work[2] + strength);
                            if(work[6] == 3) {
                                work[3] = 0;
                                work[4] = 0;
                                work[5] = 0;
                            }
                        }
                        else {
                            if(work[10] < 3) work[10] = Math.min(3, work[10] + strength);
                            if(work[10] == 3) {
                                work[7] = 0;
                                work[8] = 0;
                                work[9] = 0;
                            }
                        }
                    }
                    dirtyCache = true;
                    boolean destroyed = true;
                    for(AABB b : getWorkpieceBoxes()) {
                        if(b.getYsize() > 0) {
                            destroyed = false;
                            break;
                        }
                    }
                    setChanged();
                    if(destroyed) {
                        item = null;
                        level.setBlock(worldPosition, getBlockState().setValue(TieredAnvilBlock.HAS_METAL, false), 2);
                    }
                    else level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                }
            }
            case FLIP -> {
                level.playSound(null, player, SoundsNF.TONGS_HANDLE.get(), SoundSource.PLAYERS, 1F, 0.975F + level.random.nextFloat() * 0.05F);
                TongsItem tongs = (TongsItem) tool.getItem();
                if(!tongs.hasWorkpiece(tool) && temperature <= TieredHeat.fromTier(tongs.getMaxHeatTier() + 1).getBaseTemp()) {
                    flip = !flip;
                    int leftSpread = work[3];
                    int leftDraw = work[4];
                    int leftPunch = work[5];
                    int leftCut = work[6];
                    work[3] = work[7];
                    work[4] = work[8];
                    work[5] = work[9];
                    work[6] = work[10];
                    work[7] = leftSpread;
                    work[8] = leftDraw;
                    work[9] = leftPunch;
                    work[10] = leftCut;
                    dirtyCache = true;
                    setChanged();
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                }
            }
            case TAKE -> {
                level.playSound(null, player, SoundsNF.TONGS_HANDLE.get(), SoundSource.PLAYERS, 1F, 0.975F + level.random.nextFloat() * 0.05F);
                TongsItem tongs = (TongsItem) tool.getItem();
                if(!tongs.hasWorkpiece(tool) && temperature <= TieredHeat.fromTier(tongs.getMaxHeatTier() + 1).getBaseTemp()) {
                    dirtyCache = true;
                    tool.getTag().putString("item", ForgeRegistries.ITEMS.getKey(item).toString());
                    tool.getTag().putInt("color", getColor().getRGB());
                    tool.getTag().putInt("stableTempTicks", stableTempTicks);
                    tool.getTag().putFloat("temperature", getTemperature());
                    tool.getTag().putIntArray("work", work);
                    if(slagCenter) tool.getTag().putBoolean("slagCenter", true);
                    if(slagLeft) tool.getTag().putBoolean("slagLeft", true);
                    if(slagRight) tool.getTag().putBoolean("slagRight", true);
                    if(flip) tool.getTag().putBoolean("flip", true);
                    item = null;
                    setChanged();
                    level.setBlock(worldPosition, getBlockState().setValue(TieredAnvilBlock.HAS_METAL, false), 2);
                }
            }
        }
    }

    public boolean putWorkpiece(ItemStack item, Vec3 location) {
        TongsItem tongs = (TongsItem) item.getItem();
        if(!tongs.hasWorkpiece(item)) return false;
        Direction facing = getBlockState().getValue(TieredAnvilBlock.FACING);
        double positioning = facing.getAxis() != Direction.Axis.Z ? location.x % 1D : location.z % 1D;
        if(positioning < 0D) positioning += 1D;
        if(facing == Direction.NORTH || facing == Direction.WEST) positioning = 1D - positioning;
        work = tongs.getWork(item);
        this.item = tongs.getWorkpiece(item);
        section = positioning < 5D/16D ? AnvilSection.HORN : (positioning < 11D/16D ? AnvilSection.FLAT : AnvilSection.EDGE);
        temperature = tongs.getTemperature(item);
        stableTempTicks = tongs.getStableTempTicks(item);
        color = new Color(tongs.getColor(item));
        slagCenter = item.getTag().getBoolean("slagCenter");
        slagLeft = item.getTag().getBoolean("slagLeft");
        slagRight = item.getTag().getBoolean("slagRight");
        flip = item.getTag().getBoolean("flip");
        tongs.removeWorkpiece(item);
        setChanged();
        level.setBlock(worldPosition, getBlockState().setValue(TieredAnvilBlock.HAS_METAL, true), 2);
        return true;
    }

    public void destroyWorkpiece() {
        if(hasWorkpiece()) {
            double x = worldPosition.getX() + 0.5, y = worldPosition.getY() + 1, z = worldPosition.getZ() + 0.5;
            TieredHeat heat = TieredHeat.fromTemp(temperature);
            if(heat != TieredHeat.NONE) {
                ((ServerLevel) level).sendParticles(heat.getSparkParticle().get(), x, y, z,
                        20 + level.random.nextInt(5), 4/32D, 1D/16D, 4/32D, 0.003F);
            }
            level.playSound(null, x, y, z, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1F, 1F);
            item = null;
            level.setBlock(worldPosition, getBlockState().setValue(TieredAnvilBlock.HAS_METAL, false), 2);
            setChanged();
        }
    }

    public float getRotationDegrees() {
        Direction direction = getBlockState().getValue(TieredAnvilBlock.FACING);
        return switch (direction) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        };
    }

    public AABB[] getWorkpieceBoxes() {
        if(hasWorkpiece() && dirtyCache) {
            float baseWidth = 2F/16F, baseHeight = 2F/16F, baseDepth = 3F/16F;
            dirtyCache = false;
            float rot = -getRotationDegrees();
            Vector3f offset = MathUtil.rotatePointByYaw(new Vector3f(-(0.5F - section.center), 0, 0), rot);
            Vec3 pos = new Vec3(worldPosition.getX() + 0.5 + offset.x(), worldPosition.getY() + 1, worldPosition.getZ() + 0.5 + offset.z());

            float spread = work[0] * 0.5F;
            float draw = work[1] * (work[1] < 0 ? 0.25F : 0.5F);
            float cut = work[2] == 0 ? 1 : (work[2] == 1 ? 0.5F : 0.25F);
            float height = work[2] > 2 ? 0 : Math.max(0, baseHeight * (1F - spread/2 - draw/2));

            float leftDraw = work[4] * (work[4] < 0 ? 0.25F : 0.5F);
            float leftPunch = work[5] * -0.5F/16F;
            float leftSpread = work[3] * 0.5F;
            float leftCut = work[6] == 0 ? 1 : (work[6] == 1 ? 0.5F : 0.25F);
            float leftHeight = work[6] > 2 ? 0 : Math.max(0, baseHeight * (1F - leftSpread/2 - leftDraw/2));

            float rightDraw = work[8] * (work[8] < 0 ? 0.25F : 0.5F);
            float rightPunch = work[9] * -0.5F/16F;
            float rightSpread = work[7] * 0.5F;
            float rightCut = work[10] == 0 ? 1 : (work[10] == 1 ? 0.5F : 0.25F);
            float rightHeight = work[10] > 2 ? 0 : Math.max(0, baseHeight * (1F - rightSpread/2 - rightDraw/2));

            float yOff = -Math.min(Math.min(height > 0 ? 0 : 1, leftHeight > 0 ? (leftPunch + (getSection() == AnvilSection.HORN ? 1F/16F : 0)) : 1), rightHeight > 0 ? rightPunch : 1);
            //Center
            float centerWidth = baseWidth * (1F + draw);
            float depth = baseDepth * (1F + spread - draw/2) * cut;
            Vector3f rotPos = MathUtil.rotatePointByYaw(new Vector3f(-centerWidth/2, 0 + yOff, -depth/2), rot);
            Vector3f rotSize = MathUtil.rotatePointByYaw(new Vector3f(centerWidth, height, depth), rot);
            Vec3 boxPos = pos.add(rotPos.x(), rotPos.y(), rotPos.z());
            cachedBoxes[0] = new AABB(boxPos, boxPos.add(rotSize.x(), rotSize.y(), rotSize.z()));
            float spacing = height > 0 ? centerWidth/2 : 1F/32F;
            //Left end
            float width = baseWidth * (1F + leftDraw) * leftCut;
            depth = baseDepth * (1F + leftSpread - leftDraw/2);
            rotPos = MathUtil.rotatePointByYaw(new Vector3f(-width - spacing, leftPunch + yOff, -depth/2), rot);
            rotSize = MathUtil.rotatePointByYaw(new Vector3f(width, leftHeight, depth), rot);
            boxPos = pos.add(rotPos.x(), rotPos.y(), rotPos.z());
            cachedBoxes[1] = new AABB(boxPos, boxPos.add(rotSize.x(), rotSize.y(), rotSize.z()));
            //Right end
            width = baseWidth * (1F + rightDraw) * rightCut;
            depth = baseDepth * (1F + rightSpread - rightDraw/2);
            rotPos = MathUtil.rotatePointByYaw(new Vector3f(spacing, rightPunch + yOff, -depth/2), rot);
            rotSize = MathUtil.rotatePointByYaw(new Vector3f(width, rightHeight, depth), rot);
            boxPos = pos.add(rotPos.x(), rotPos.y(), rotPos.z());
            cachedBoxes[2] = new AABB(boxPos, boxPos.add(rotSize.x(), rotSize.y(), rotSize.z()));
        }
        return cachedBoxes;
    }
}
