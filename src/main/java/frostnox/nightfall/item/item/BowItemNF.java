package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.entity.entity.projectile.ArrowEntity;
import frostnox.nightfall.item.IProjectileItem;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public class BowItemNF extends ProjectileLauncherItem {
    public BowItemNF(RegistryObject<? extends Action> useAction, TagKey<Item> ammoTag, Properties properties) {
        super(useAction, ammoTag, properties);
    }

    @Override
    public Entity createProjectile(ItemStack item, Player user, InteractionHand hand, float velocity, ItemStack ammoItem) {
        IProjectileItem projectile = ammoItem.getItem() instanceof IProjectileItem ? (IProjectileItem) ammoItem.getItem() : ItemsNF.FLINT_ARROW.get();
        ArrowEntity entity = new ArrowEntity(user.level, user, projectile);
        entity.setBaseDamage(projectile.getProjectileDamage() * AttributesNF.getStrengthMultiplier(user));
        if(user.getAbilities().instabuild) entity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        entity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F,
                velocity * projectile.getProjectileVelocityScalar(), CombatUtil.modifyProjectileAccuracy(user, projectile.getProjectileInaccuracy()));
        return entity;
    }

    @Override
    protected Item getDefaultItem() {
        return ItemsNF.FLINT_ARROW.get();
    }

    @Override
    protected int getAmmoId(ItemStack ammoItem) {
        if(ammoItem.getItem() instanceof IProjectileItem projectileItem) return projectileItem.getAmmoId();
        else return 0;
    }
}
