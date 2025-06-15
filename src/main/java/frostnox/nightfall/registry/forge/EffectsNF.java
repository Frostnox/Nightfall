package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.effect.DamageEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectsNF {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Nightfall.MODID);
    public static final RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding", () -> new DamageEffect(MobEffectCategory.HARMFUL, 0F, DamageTypeSource.BLEEDING) {
        @Override
        public float getDamage(LivingEntity entity, int duration, int amplifier) {
            if(duration > 90 * 20) return entity.getMaxHealth() / 20F;
            if(duration > 60 * 20) return entity.getMaxHealth() / 30F;
            if(duration > 30 * 20) return entity.getMaxHealth() / 40F;
            return 0F;
        }
    });
    public static final RegistryObject<MobEffect> POISON = EFFECTS.register("poison", () -> new DamageEffect(MobEffectCategory.HARMFUL, 2.5F, DamageTypeSource.POISON) {
        @Override
        public float getDamage(LivingEntity entity, int duration, int amplifier) {
            if(entity.getHealth() > damage) return damage; //2.5 damage every 2 seconds; 37.5 damage over 30 seconds
            else return 0F;
        }
    });
    public static final RegistryObject<MobEffect> BANDAGED = EFFECTS.register("bandaged", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if(entity.getHealth() < entity.getMaxHealth()) entity.heal(1F);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 90 == 0;
        }
    });
    public static final RegistryObject<MobEffect> MOON_BLESSING = EFFECTS.register("moon_blessing", () -> new DamageEffect(MobEffectCategory.BENEFICIAL)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "07485be1-69a1-4313-9012-c54a7133957b", 0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributesNF.STRENGTH.get(), "09d6a029-e15f-4731-99e8-d546f22977ed", 0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributesNF.STRIKING_ABSORPTION.get(), "eb96b12a-6a7e-4c33-a0a0-055e8c9380dc", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.SLASHING_ABSORPTION.get(), "3875dd84-b979-4752-a8e1-7cf350d1f97c", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.PIERCING_ABSORPTION.get(), "123d9b3a-2dfc-4faf-8d98-ec7a8e6bf655", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.FIRE_ABSORPTION.get(), "29130a79-b037-4bc7-8278-608e4918d707", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.FROST_ABSORPTION.get(), "5abe03c1-9448-48cf-aa59-10c617cbb504", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.ELECTRIC_ABSORPTION.get(), "cf58f418-c961-44ea-9bb2-f02b0bdb6176", 0.2D, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> PARALYSIS = EFFECTS.register("paralysis", () -> new DamageEffect(MobEffectCategory.HARMFUL)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "5e59ad86-9311-4fbd-8e70-db765be34d30", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "728607eb-2232-457d-a9ac-f61d42fa91f3", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributesNF.ENDURANCE.get(), "47a84663-d865-4df0-8672-01608ce5d124", -2D, AttributeModifier.Operation.ADDITION));

    public static void register() {
        EFFECTS.register(Nightfall.MOD_EVENT_BUS);
    }
}
