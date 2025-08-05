package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.entity.effect.DamageEffect;
import frostnox.nightfall.entity.effect.StarvationEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class EffectsNF {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Nightfall.MODID);
    public static final RegistryObject<MobEffect> DESPAIR = EFFECTS.register("despair", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0) {}
            .addAttributeModifier(Attributes.MAX_HEALTH, "3eb0c333-e6d2-46ed-acfb-b12fa5ae7e7a", -10, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.ENDURANCE.get(), "8dee21aa-5d0c-4bae-b2b4-0c6f09b5fe5e", -1, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.WILLPOWER.get(), "be42557c-0db3-43c8-b708-eecb422f3e34", -1, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.STRENGTH.get(), "9e944581-840d-432a-a48a-5576314f09c1", -1, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "b5f3fc7d-16f3-4867-b7c5-9973ddcbcb49", -0.03, AttributeModifier.Operation.MULTIPLY_BASE)
            .addAttributeModifier(AttributesNF.PERCEPTION.get(), "c23eca0f-114f-47f5-9442-26fc14a12788", -1, AttributeModifier.Operation.ADDITION)
    );
    public static final RegistryObject<MobEffect> STARVATION_1 = EFFECTS.register("starvation_1", () -> new StarvationEffect() {
        @Override
        public void onTickUp(LivingEntity entity, int amplifier, int duration) {
            if(entity.level.isClientSide) return;
            if(duration > 20 * 60 * 10) {
                entity.removeEffect(this);
                entity.addEffect(new MobEffectInstance(EffectsNF.STARVATION.get(), duration, amplifier));
            }
        }
    }
            .addAttributeModifier(AttributesNF.ENDURANCE.get(), "8d9d96b6-8df3-461a-8ae7-1c7cf42351a8", -2D, AttributeModifier.Operation.ADDITION));
    private static final UUID STARVATION_ENDURANCE_UUID = UUID.fromString("995a05cb-0eaf-4ec8-99e6-ecb751531794");
    public static final RegistryObject<MobEffect> STARVATION = EFFECTS.register("starvation", () -> new StarvationEffect() {
        @Override
        public void onTickUp(LivingEntity entity, int amplifier, int duration) {
            if(entity.level.isClientSide) return;
            int expectedAmplifier;
            if(duration > 20 * 60 * 40) expectedAmplifier = 3;
            else if(duration > 20 * 60 * 20) expectedAmplifier = 2;
            else if(duration > 20 * 60 * 10) expectedAmplifier = 1;
            else expectedAmplifier = 0;
            if(amplifier != expectedAmplifier) {
                entity.removeEffect(this);
                if(expectedAmplifier == 0) entity.addEffect(new MobEffectInstance(EffectsNF.STARVATION_1.get(), duration, expectedAmplifier));
                else entity.addEffect(new MobEffectInstance(EffectsNF.STARVATION.get(), duration, expectedAmplifier));
            }
        }

        @Override
        public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
            if(pModifier.getId().equals(STARVATION_ENDURANCE_UUID)) return super.getAttributeModifierValue(pAmplifier, pModifier);
            else return pModifier.getAmount() * (double) (pAmplifier);
        }
    }
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "e0d13cbb-521f-4afc-b476-43b40dd051f2", -0.125D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "01ed0754-2303-431d-9fc1-5a56ef6ad916", -0.125D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributesNF.STRENGTH.get(), "c0b371d8-e9f7-418c-b6ee-4997fa9b4e9d", -2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(Attributes.MAX_HEALTH, "253f436b-cc13-4474-a4e9-3f3d534e93a9", -10D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.ENDURANCE.get(), "995a05cb-0eaf-4ec8-99e6-ecb751531794", -2D, AttributeModifier.Operation.ADDITION));
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
            .addAttributeModifier(AttributesNF.STRIKING_DEFENSE.get(), "eb96b12a-6a7e-4c33-a0a0-055e8c9380dc", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.SLASHING_DEFENSE.get(), "3875dd84-b979-4752-a8e1-7cf350d1f97c", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.PIERCING_DEFENSE.get(), "123d9b3a-2dfc-4faf-8d98-ec7a8e6bf655", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.FIRE_DEFENSE.get(), "29130a79-b037-4bc7-8278-608e4918d707", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.FROST_DEFENSE.get(), "5abe03c1-9448-48cf-aa59-10c617cbb504", 0.2D, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(AttributesNF.ELECTRIC_DEFENSE.get(), "cf58f418-c961-44ea-9bb2-f02b0bdb6176", 0.2D, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> PARALYSIS = EFFECTS.register("paralysis", () -> new DamageEffect(MobEffectCategory.HARMFUL)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "5e59ad86-9311-4fbd-8e70-db765be34d30", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "728607eb-2232-457d-a9ac-f61d42fa91f3", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributesNF.ENDURANCE.get(), "47a84663-d865-4df0-8672-01608ce5d124", -2D, AttributeModifier.Operation.ADDITION));

    public static void register() {
        EFFECTS.register(Nightfall.MOD_EVENT_BUS);
    }
}
