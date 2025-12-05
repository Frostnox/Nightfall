package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.Impact;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.PlayerAttribute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class AttributesNF {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Nightfall.MODID);
    public static final RegistryObject<Attribute> INVENTORY_CAPACITY = ATTRIBUTES.register("inventory_capacity",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID + ".inventory_capacity", 0.0D, 0.0D, 12.0D).setSyncable(true));
    public static final RegistryObject<Attribute> ACTION_SPEED = ATTRIBUTES.register("action_speed",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID + ".action_speed", 1.0D, 0.0D, 100.0D).setSyncable(true));
    public static final RegistryObject<Attribute> STAMINA_REDUCTION = ATTRIBUTES.register("stamina_reduction",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID + ".stamina_reduction", 1.0D, 0.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> HEARING_RANGE = ATTRIBUTES.register("hearing_range",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".hearing_range", 15.0D, 0.0D, 1024.0D));
    public static final RegistryObject<Attribute> ENDURANCE = ATTRIBUTES.register("endurance",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".endurance", 10.0D, 1.0D, 20.0D).setSyncable(true));
    public static final RegistryObject<Attribute> WILLPOWER = ATTRIBUTES.register("willpower",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".willpower", 10.0D, 1.0D, 20.0D).setSyncable(true));
    public static final RegistryObject<Attribute> STRENGTH = ATTRIBUTES.register("strength",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".strength", 10.0D, 1.0D, 20.0D).setSyncable(true));
    public static final RegistryObject<Attribute> PERCEPTION = ATTRIBUTES.register("perception",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".perception", 10.0D, 1.0D, 20.0D).setSyncable(true));
    public static final RegistryObject<Attribute> STRIKING_DEFENSE = ATTRIBUTES.register("striking_defense",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".striking_defense", 0.0D, -1.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> SLASHING_DEFENSE = ATTRIBUTES.register("slashing_defense",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".slashing_defense", 0.0D, -1.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> PIERCING_DEFENSE = ATTRIBUTES.register("piercing_defense",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".piercing_defense", 0.0D, -1.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> FIRE_DEFENSE = ATTRIBUTES.register("fire_defense",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".fire_defense", 0.0D, -1.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> FROST_DEFENSE = ATTRIBUTES.register("frost_defense",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".frost_defense", 0.0D, -1.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> ELECTRIC_DEFENSE = ATTRIBUTES.register("electric_defense",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".electric_defense", 0.0D, -1.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> WITHER_DEFENSE = ATTRIBUTES.register("wither_defense",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".wither_defense", 0.0D, -1.0D, 1.0D));
    public static final RegistryObject<Attribute> POISE = ATTRIBUTES.register("poise",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".poise", 0.0D, 0D, Poise.values().length - 1).setSyncable(true));
    public static final RegistryObject<Attribute> BLEEDING_RESISTANCE = ATTRIBUTES.register("bleeding_resistance",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".bleeding_resistance", 0.0D, -1.0D, 1.0D).setSyncable(true));
    public static final RegistryObject<Attribute> POISON_RESISTANCE = ATTRIBUTES.register("poison_resistance",
            () -> new RangedAttribute("attribute.name." + Nightfall.MODID +".poison_resistance", 0.0D, -1.0D, 1.0D).setSyncable(true));

    public static void register() {
        ATTRIBUTES.register(Nightfall.MOD_EVENT_BUS);
    }

    public static int getInventoryCapacity(Player player) {
        if(player.isCreative() || player.isSpectator()) return 12;
        return (int) Math.round(player.getAttribute(INVENTORY_CAPACITY.get()).getValue());
    }

    public static int getValue(Player player, PlayerAttribute attribute) {
        return switch(attribute) {
            case VITALITY -> getVitality(player);
            case ENDURANCE -> getEndurance(player);
            case WILLPOWER -> 10;
            case STRENGTH -> getStrength(player);
            case AGILITY -> getAgility(player);
            case PERCEPTION -> getPerception(player);
        };
    }

    public static int getVitality(LivingEntity entity) {
        return (int) Math.round(entity.getAttribute(Attributes.MAX_HEALTH).getValue() / 10D);
    }

    public static int getEndurance(LivingEntity entity) {
        return (int) Math.round(entity.getAttribute(ENDURANCE.get()).getValue());
    }

    public static int getStrength(LivingEntity entity) {
        return (int) Math.round(entity.getAttribute(STRENGTH.get()).getValue());
    }

    public static int getAgility(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        double modifiers = 0;
        for(AttributeModifier modifier : attribute.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
            modifiers += modifier.getAmount();
        }
        return (int) Math.round(10 + (attribute.getBaseValue() - 0.1D) / 0.005D + modifiers / 0.03D);
    }

    public static int getPerception(LivingEntity entity) {
        return (int) Math.round(entity.getAttribute(PERCEPTION.get()).getValue());
    }

    public static @Nullable Attribute getDefense(DamageType type) {
        return switch(type) {
            case STRIKING -> STRIKING_DEFENSE.get();
            case SLASHING -> SLASHING_DEFENSE.get();
            case PIERCING -> PIERCING_DEFENSE.get();
            case FIRE -> FIRE_DEFENSE.get();
            case FROST -> FROST_DEFENSE.get();
            case ELECTRIC -> ELECTRIC_DEFENSE.get();
            case WITHER -> WITHER_DEFENSE.get();
            default -> null;
        };
    }

    public static double getMaxStamina(Player player) {
        return player.getAttribute(ENDURANCE.get()).getValue() * 10D;
    }

    public static double getMaxEssence(LivingEntity entity) {
        if(entity.getAttribute(WILLPOWER.get()) != null) return entity.getAttribute(WILLPOWER.get()).getValue() * 10D;
        return 0D;
    }

    public static double getStaminaRegenMultiplier(Player player) {
        double regen = 1D + (player.getAttribute(ENDURANCE.get()).getValue() - ENDURANCE.get().getDefaultValue()) * 0.05D;
        float temp = PlayerData.get(player).getTemperature();
        if(temp > 1F) regen *= 1F - Math.min(1F, (temp - 1) * 4);
        return regen;
    }

    public static float getStrengthMultiplier(LivingEntity entity) {
        if(entity.getAttribute(STRENGTH.get()) != null) {
            return (float) (1D + (entity.getAttribute(STRENGTH.get()).getValue() - STRENGTH.get().getDefaultValue()) * 0.05D);
        }
        return 1;
    }

    public static int getPerceptionInfluence(LivingEntity entity) {
        AttributeInstance perception = entity.getAttribute(PERCEPTION.get());
        if(perception != null) return (int) (perception.getValue() - PERCEPTION.get().getDefaultValue());
        else return 0;
    }

    public static Poise getPoise(LivingEntity entity) {
        AttributeInstance poise = entity.getAttribute(POISE.get());
        if(poise == null) return Poise.NONE;
        else {
            double maxPoise = poise.getBaseValue();
            for(AttributeModifier modifier : poise.getModifiers()) {
                if(modifier.getAmount() > maxPoise) maxPoise = modifier.getAmount();
            }
            return Poise.values()[(int) maxPoise];
        }
    }
}
