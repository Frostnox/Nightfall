package frostnox.nightfall.item.item;

import frostnox.nightfall.item.IItemLightSource;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class LightItem extends ItemNF implements IItemLightSource {
    private final Supplier<? extends Item> extinguishedItem;
    private final double equippedHeight, radiusSqr;
    private final int brightness;

    public LightItem(double equippedHeight, int brightness, double radius, Supplier<? extends Item> extinguishedItem, Properties properties) {
        super(properties);
        this.extinguishedItem = extinguishedItem;
        this.equippedHeight = equippedHeight;
        this.radiusSqr = radius * radius;
        this.brightness = brightness;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(entity.isInWater() && extinguishedItem.get() != this) LevelUtil.extinguishItemEntity(entity, extinguishedItem.get(), true);
        return false;
    }

    @Override
    public Item getExtinguishedItem() {
        return extinguishedItem.get();
    }

    @Override
    public double getEquippedHeight(Pose pose) {
        return equippedHeight;
    }

    @Override
    public int getBrightness() {
        return brightness;
    }

    @Override
    public double getLightRadiusSqr() {
        return radiusSqr;
    }
}
