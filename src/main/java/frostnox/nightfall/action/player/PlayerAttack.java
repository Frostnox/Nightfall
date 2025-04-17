package frostnox.nightfall.action.player;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.*;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.util.RenderUtil;
import frostnox.nightfall.util.animation.AnimationData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.List;

public abstract class PlayerAttack extends Attack implements IClientAction {
    public PlayerAttack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public PlayerAttack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean canStart(LivingEntity user) {
        if(super.canStart(user)) return PlayerData.get((Player) user).hasNoSwapDelay();
        else return false;
    }

    @Override
    public Action getAction() {
        return this;
    }

    @Override
    public float getDamage(LivingEntity user) {
        if(user instanceof Player player) {
            if(user.getItemInHand(PlayerData.get(player).getActiveHand()).getItem() instanceof IWeaponItem weapon) {
                return (super.getDamage(user) * weapon.getDamageMultiplier() + weapon.getBaseDamage()) * AttributesNF.getStrengthMultiplier(user);
            }
        }
        //else if(user.getMainHandItem().getItem() instanceof IWeapon weapon) return super.getDamage(user) + weapon.getDamage();
        return super.getDamage(user);
    }

    @Override
    public HurtSphere getHurtSpheres(LivingEntity user) {
        if(user instanceof Player player) {
            if(player.getItemInHand(PlayerData.get(player).getActiveHand()).getItem() instanceof IWeaponItem weapon) return weapon.getHurtSpheres();
        }
        return super.getHurtSpheres(user);
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        /*
        * X and Y from FP matrix translation values at default FOV
        * Since FOV does not affect FP rendering, weapon visuals become inconsistent as FOV deviates
        * Z value affects reach and is matched to third person range of weapons (measured as distance from shoulder to hand)
        * However, the Z value, originally 0.72, is tied to the FOV, so changing it shifts the default value
        * A Z value of 0.5 shifts the default FOV (70) to 85.5 repeating
        * Players typically use higher FOV values, so lower Z values have a beneficial side effect
        */
        return new Vector3f(-0.56F, -0.52F, 0.5F);
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HAND_RIGHT, new AnimationData());
        return map;
    }

    @Override
    public float getMaxXRot(int state) {
        if(state == getTotalStates() - 1) return 22.5F;
        else if(state == 0) return 12.5F;
        else return 8F;
    }

    @Override
    public float getMaxYRot(int state) {
        if(state == getTotalStates() - 1) return 45F;
        else if(state == 0) return 25F;
        else return 17.5F;
    }

    @Override
    public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
        DecimalFormat format = new DecimalFormat("0.0");
        float itemDamage;
        if(stack.getItem() instanceof IWeaponItem weapon) itemDamage = damage * weapon.getDamageMultiplier() + weapon.getBaseDamage();
        else itemDamage = damage;
        TextComponent text = new TextComponent(format.format(itemDamage) + " ");
        text.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
        for(int i = 0; i < damageType.length; i++) {
            text.append(RenderUtil.getDamageTypeText(damageType[i]));
            if(i != damageType.length - 1) text.append("/");
        }
        for(int i = 0; i < effects.length; i++) {
            text.append(", " + (int) (effects[i].chance * 100) + "% ");
            text.append(effects[i].effect.get().getDisplayName());
        }
        return ObjectArrayList.of(text);
    }
}
