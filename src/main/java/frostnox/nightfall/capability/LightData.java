package frostnox.nightfall.capability;

import frostnox.nightfall.world.ILightSource;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import frostnox.nightfall.item.IItemLightSource;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LightData implements ILightData {
    public static final Capability<ILightData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {}); //Reference to manager instance
    private final Entity entity;
    private final Object2DoubleMap<BlockPos> lightPosMap;
    private int brightness, lastProcessedBrightness;
    private double lightRadiusSqr, lastProcessedLightRadiusSqr;
    private double lightX, lightY = Double.MIN_VALUE, lightZ;
    private boolean emitsLightServer = true, dirtyLight = true;

    private LightData(Entity entity) {
        this.entity = entity;
        if(entity.level.isClientSide) lightPosMap = new Object2DoubleOpenHashMap<>(1024);
        else lightPosMap = null;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    @Override
    public void setLightRadiusSqr(double lightRadiusSqr) {
        this.lightRadiusSqr = lightRadiusSqr;
    }

    @Override
    public int getLastProcessedBrightness() {
        return lastProcessedBrightness;
    }

    @Override
    public void setLastProcessedBrightness(int brightness) {
        lastProcessedBrightness = brightness;
    }

    @Override
    public double getLastProcessedLightRadiusSqr() {
        return lastProcessedLightRadiusSqr;
    }

    @Override
    public void setLastProcessedLightRadiusSqr(double lightRadiusSqr) {
        this.lastProcessedLightRadiusSqr = lightRadiusSqr;
    }

    @Override
    public double getLightX() {
        return lightX;
    }

    @Override
    public double getLightY() {
        return lightY;
    }

    @Override
    public double getLightZ() {
        return lightZ;
    }

    @Override
    public void setLightX(double x) {
        lightX = x;
    }

    @Override
    public void setLightY(double y) {
        lightY = y;
    }

    @Override
    public void setLightZ(double z) {
        lightZ = z;
    }

    private void updateLight(ItemStack item) {
        if(item.getItem() instanceof ILightSource source) {
            if(source.getBrightness() > brightness) brightness = source.getBrightness();
            if(source.getLightRadiusSqr() > lightRadiusSqr) lightRadiusSqr = source.getLightRadiusSqr();
        }
    }

    @Override
    public void updateLight() {
        if(entity instanceof Player player) {
            brightness = 0;
            lightRadiusSqr = 0;
            if(!player.isSpectator()) {
                for(EquipmentSlot slot : EquipmentSlot.values()) updateLight(player.getItemBySlot(slot));
                AccessoryInventory accessoryInventory = PlayerData.get(player).getAccessoryInventory();
                for(AccessorySlot slot : AccessorySlot.values()) updateLight(accessoryInventory.getItem(slot));
            }
        }
    }

    private ItemStack tryExtinguish(Player player, ItemStack item, IItemLightSource source, double itemHeight) {
        double y = player.getY() + itemHeight;
        BlockPos pos = new BlockPos(player.getX(), y, player.getZ());
        FluidState fluid = player.level.getFluidState(pos);
        if(fluid.is(FluidTags.WATER)) {
            if(pos.getY() + fluid.getHeight(player.level, pos) > y) {
                NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.REMOVE_LIGHT_SOURCE_CLIENT, player.getId()));
                player.level.playSound(null, player, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + player.level.random.nextFloat());
                return new ItemStack(source.getExtinguishedItem(), item.getCount());
            }
        }
        return item;
    }

    @Override
    public void inWaterTickServer() {
        if(entity instanceof ServerPlayer player && !player.isSpectator() && !player.isCreative()) {
            for(EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = player.getItemBySlot(slot);
                if(item.getItem() instanceof IItemLightSource source && source.getExtinguishedItem() != item.getItem()) {
                    ItemStack extinguishedItem = tryExtinguish(player, item, source,
                            slot.getType() == EquipmentSlot.Type.HAND ? (0.85F * player.getBbHeight() / 1.8F) : source.getEquippedHeight(player.getPose()));
                    if(!extinguishedItem.is(item.getItem())) player.setItemSlot(slot, extinguishedItem);
                }
            }
            AccessoryInventory accessoryInventory = PlayerData.get(player).getAccessoryInventory();
            for(AccessorySlot slot : AccessorySlot.values()) {
                ItemStack item = accessoryInventory.getItem(slot);
                if(item.getItem() instanceof IItemLightSource source && source.getExtinguishedItem() != item.getItem()) {
                    ItemStack extinguishedItem = tryExtinguish(player, item, source, source.getEquippedHeight(player.getPose()));
                    if(!extinguishedItem.is(item.getItem())) accessoryInventory.setItem(slot, extinguishedItem);
                }
            }
        }
    }

    @Override
    public void init() {
        if(entity instanceof ItemEntity itemEntity) {
            ILightSource source = (ILightSource) itemEntity.getItem().getItem();
            brightness = source.getBrightness();
            lightRadiusSqr = source.getLightRadiusSqr();
        }
    }

    @Override
    public boolean notifyClientOnStopTracking() {
        return emitsLightServer;
    }

    @Override
    public void setupClientNotifications() {
        if(entity instanceof ItemEntity itemEntity && !(itemEntity.getItem().getItem() instanceof ILightSource)) {
            emitsLightServer = false;
        }
    }

    @Override
    public Object2DoubleMap<BlockPos> getLightMap() {
        return lightPosMap;
    }

    @Override
    public boolean isLightDirty() {
        return dirtyLight;
    }

    @Override
    public void setLightDirty(boolean dirty) {
        dirtyLight = dirty;
    }

    @Override
    public int getBrightness() {
        return brightness;
    }

    @Override
    public double getLightRadiusSqr() {
        return lightRadiusSqr;
    }

    public static ILightData get(Entity entity) {
        return entity.getCapability(CAPABILITY, null).orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional."));
    }

    public static boolean isPresent(Entity entity) {
        return entity.getCapability(CAPABILITY).isPresent();
    }

    public static class LightDataCapability implements ICapabilitySerializable<CompoundTag> {
        private final LightData cap;
        private final LazyOptional<ILightData> holder;

        public LightDataCapability(Entity entity) {
            cap = new LightData(entity);
            holder = LazyOptional.of(() -> cap);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> c, Direction side) {
            return CAPABILITY == c ? (LazyOptional<T>) holder : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("emitsLight", cap.emitsLightServer);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            cap.emitsLightServer = tag.getBoolean("emitsLight");
        }
    }
}
