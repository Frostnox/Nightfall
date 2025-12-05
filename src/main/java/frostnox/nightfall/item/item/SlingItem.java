package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.projectile.ThrownRockEntity;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public class SlingItem extends ProjectileLauncherItem {
    public final float inaccuracy;

    public SlingItem(RegistryObject<? extends Action> useAction, TagKey<Item> ammoTag, float inaccuracy, Properties properties) {
        super(useAction, ammoTag, properties);
        this.inaccuracy = inaccuracy;
    }

    @Override
    public Entity createProjectile(ItemStack item, Player user, InteractionHand hand, float velocity, ItemStack ammoItem) {
        ThrownRockEntity projectile = new ThrownRockEntity(user.level, user);
        projectile.setItem(ammoItem);
        projectile.setBaseDamage((ammoItem.is(TagsNF.IGNEOUS) ? 15F : (ammoItem.is(TagsNF.METAMORPHIC) ? 12.5F : 10F)) * AttributesNF.getStrengthMultiplier(user));
        projectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, velocity, CombatUtil.modifyProjectileAccuracy(user, inaccuracy));
        return projectile;
    }

    @Override
    protected Item getDefaultItem() {
        return ItemsNF.ROCKS.get(Stone.SHALE).get();
    }
}
