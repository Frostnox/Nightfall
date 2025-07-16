package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class OlmurEntity extends MonsterEntity {
    public OlmurEntity(EntityType<? extends MonsterEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.MOVEMENT_SPEED, 0.2675F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.STRIKING_ABSORPTION.get(), 0.2)
                .add(AttributesNF.SLASHING_ABSORPTION.get(), -0.2)
                .add(AttributesNF.FIRE_ABSORPTION.get(), -0.25);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }
}
