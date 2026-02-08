package frostnox.nightfall.block.block.anvil;

import com.mojang.math.Vector3f;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.item.item.MeleeWeaponItem;
import frostnox.nightfall.item.item.TongsItem;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
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
import java.util.*;

public class TieredAnvilBlockEntity extends BlockEntity {
    public int[] work = new int[8];
    protected @Nullable Item item;
    protected AnvilSection section;
    protected float temperature;
    protected int stableTempTicks;
    protected Color color;
    protected AABB[] cachedBoxes = new AABB[3];
    protected boolean dirtyCache = true;

    public TieredAnvilBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.ANVIL.get(), pos, state);
    }

    protected TieredAnvilBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
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

    public AnvilSection getSection() {
        return section;
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, TieredAnvilBlockEntity entity) {
        boolean raining = level.isRainingAt(pos.above());
        if(entity.stableTempTicks > 0) {
            entity.stableTempTicks = Math.max(0, entity.stableTempTicks - (raining ? 2 : 1));
            entity.setChanged();
        }
        else if(entity.temperature > 0) {
            entity.temperature = Math.max(0, entity.temperature - (raining ? 1 : 0.5F));
            entity.setChanged();
        }
        entity.setChanged();
    }

    public void actWorkpiece(AnvilAction action, int index, Player player, ItemStack tool) {
        if(level != null && level.isClientSide) return;
        switch(action) {
            case STRIKE -> {
                boolean badTool = !(tool.getItem() instanceof MeleeWeaponItem weapon) || weapon.material.getTier() < Metal.fromString(tool.toString()).getTier() - 1;
                TieredHeat heat = TieredHeat.fromTemp(temperature);
                AABB box = getWorkpieceBoxes()[index];
                Vec3 center = box.getCenter();
                if(heat != TieredHeat.NONE) {
                    ((ServerLevel) level).sendParticles(heat.getSparkParticle().get(), center.x, center.y, center.z,
                            (badTool ? 2 : 11) + level.random.nextInt(5), 0, 0, 0, 0.12F);
                }
                level.playSound(null, center.x, center.y, center.z, SoundsNF.ANVIL_STRIKE.get(), SoundSource.BLOCKS, 1F, 1F);
                if(!player.getAbilities().instabuild) tool.hurtAndBreak(badTool ? 2 : 1, player, (p) -> p.broadcastBreakEvent(PlayerData.get(p).getActiveHand()));
                if(!badTool) {

                }
            }
            case CUT -> {

            }
            case FLIP_XZ -> {

            }
            case FLIP_Y -> {

            }
            case TAKE -> {
                TongsItem tongs = (TongsItem) tool.getItem();
                if(!tongs.hasWorkpiece(tool) && temperature <= TieredHeat.fromTier(tongs.getMaxHeatTier() + 1).getBaseTemp()) {
                    dirtyCache = true;
                    tool.getTag().putString("item", ForgeRegistries.ITEMS.getKey(item).toString());
                    tool.getTag().putInt("color", getColor().getRGB());
                    tool.getTag().putInt("stableTempTicks", stableTempTicks);
                    tool.getTag().putFloat("temperature", getTemperature());
                    tool.getTag().putIntArray("work", work);
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
        work = tongs.getWork(item);
        this.item = tongs.getWorkpiece(item);
        section = AnvilSection.FLAT;
        temperature = tongs.getTemperature(item);
        stableTempTicks = tongs.getStableTempTicks(item);
        color = new Color(tongs.getColor(item));
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
            dirtyCache = false;
            float rot = -getRotationDegrees();
            Vec3 pos = new Vec3(worldPosition.getX() + section.center, worldPosition.getY() + 1, worldPosition.getZ() + 0.5);
            float draw = work[1] * 0.5F/2F;
            float centerY = draw/2/16F;
            float rightPunch = work[7] * 0.5F / 16F;
            float rightDraw = work[6] * 0.5F/2F;
            float rightY = rightPunch + rightDraw/2/16F;
            float leftDraw = work[3] * 0.5F/2F;
            float leftPunch = work[4] * 0.5F / 16F;
            float leftY = leftPunch + leftDraw/2/16F;
            float yOff = section.center == 0.25 ? -Math.min(rightY, centerY) : -Math.min(Math.min(centerY, leftY), rightY);
            float baseWidth = 2F/16F, baseHeight = 2F/16F, baseDepth = 3F/16F;
            //Center
            float spread = work[0] * 0.5F/2F;
            float centerWidth = baseWidth * (1F + draw);
            float height = baseHeight * (1F - spread - draw/2);
            float depth = baseDepth * (1F + spread - draw/2);
            Vector3f rotPos = MathUtil.rotatePointByYaw(new Vector3f(-centerWidth/2, centerY + yOff, -depth/2), rot);
            Vector3f rotSize = MathUtil.rotatePointByYaw(new Vector3f(centerWidth, height, depth), rot);
            Vec3 boxPos = pos.add(rotPos.x(), rotPos.y(), rotPos.z());
            cachedBoxes[0] = new AABB(boxPos, boxPos.add(rotSize.x(), rotSize.y(), rotSize.z()));
            //Left end
            spread = work[2] * 0.5F/2F;
            float width = baseWidth * (1F + leftDraw);
            height = baseHeight * (1F - spread - leftDraw/2);
            depth = baseDepth * (1F + spread - leftDraw/2);
            rotPos = MathUtil.rotatePointByYaw(new Vector3f(-width - centerWidth/2, leftY + yOff, -depth/2), rot);
            rotSize = MathUtil.rotatePointByYaw(new Vector3f(width, height, depth), rot);
            boxPos = pos.add(rotPos.x(), rotPos.y(), rotPos.z());
            cachedBoxes[1] = new AABB(boxPos, boxPos.add(rotSize.x(), rotSize.y(), rotSize.z()));
            //Right end
            spread = work[5] * 0.5F/2F;
            width = baseWidth * (1F + rightDraw);
            height = baseHeight * (1F - spread - rightDraw/2);
            depth = baseDepth * (1F + spread - rightDraw/2);
            rotPos = MathUtil.rotatePointByYaw(new Vector3f(centerWidth/2, rightY + yOff, -depth/2), rot);
            rotSize = MathUtil.rotatePointByYaw(new Vector3f(width, height, depth), rot);
            boxPos = pos.add(rotPos.x(), rotPos.y(), rotPos.z());
            cachedBoxes[2] = new AABB(boxPos, boxPos.add(rotSize.x(), rotSize.y(), rotSize.z()));
        }
        return cachedBoxes;
    }
}
