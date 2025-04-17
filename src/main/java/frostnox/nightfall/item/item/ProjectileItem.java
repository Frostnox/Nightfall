package frostnox.nightfall.item.item;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.item.IProjectileItem;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class ProjectileItem extends ItemNF implements IProjectileItem {
    private final float damage, velocityScale, inaccuracy;
    private final DamageType[] damageType;
    private final int ammoId;

    public ProjectileItem(float damage, float velocityScale, float inaccuracy, DamageType[] damageType, int ammoId, Properties properties) {
        super(properties);
        this.damage = damage;
        this.velocityScale = velocityScale;
        this.inaccuracy = inaccuracy;
        this.damageType = damageType;
        this.ammoId = ammoId;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        DecimalFormat format = new DecimalFormat("0.0");
        TextComponent text = new TextComponent(format.format(damage) + " ");
        text.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
        for(int i = 0; i < damageType.length; i++) {
            text.append(RenderUtil.getDamageTypeText(damageType[i]));
            if(i != damageType.length - 1) text.append("/");
        }
        pTooltipComponents.add(text);
    }

    @Override
    public Item getItem() {
        return this;
    }

    @Override
    public DamageType[] getProjectileDamageType() {
        return damageType;
    }

    @Override
    public float getProjectileDamage() {
        return damage;
    }

    @Override
    public float getProjectileVelocityScalar() {
        return velocityScale;
    }

    @Override
    public float getProjectileInaccuracy() {
        return inaccuracy;
    }

    @Override
    public int getAmmoId() {
        return ammoId;
    }
}
