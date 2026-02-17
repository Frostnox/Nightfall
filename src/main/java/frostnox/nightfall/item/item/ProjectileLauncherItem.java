package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.gui.screen.item.ModifiableItemScreen;
import frostnox.nightfall.client.gui.screen.item.SimpleModifiableItemScreen;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.item.client.IHeldClientTick;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.item.client.IClientSwapBehavior;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.ActionToServer;
import frostnox.nightfall.util.LevelUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class ProjectileLauncherItem extends ScreenCacheItem implements IActionableItem, IModifiable, IClientSwapBehavior, IHeldClientTick {
    public final RegistryObject<? extends Action> useAction;
    public final TagKey<Item> ammoTag;

    public ProjectileLauncherItem(RegistryObject<? extends Action> useAction, TagKey<Item> ammoTag, Properties properties) {
        super(properties);
        this.useAction = useAction;
        this.ammoTag = ammoTag;
    }

    public abstract Entity createProjectile(ItemStack item, Player user, InteractionHand hand, float velocity, ItemStack ammoItem);

    protected abstract Item getDefaultItem();

    /**
     * @return id to save to stack (0 should be the default)
     */
    protected int getAmmoId(ItemStack ammoItem) {
        return 0;
    }

    public @Nullable Entity launchProjectile(ItemStack item, Player user, InteractionHand hand, float velocity) {
        List<ItemStack> ammo = getAmmo(user);
        int index = getSelectedAmmoIndex(item, ammo, LevelUtil.getModifiableItemIndex(user.level, user, hand));
        if(ammo.isEmpty() && user.getAbilities().instabuild) {
            index = 0;
            ammo.add(new ItemStack(getDefaultItem()));
        }
        if(index < 0 || index >= ammo.size()) return null;
        item.hurtAndBreak(1, user, (p) -> p.broadcastBreakEvent(hand));
        if(item.hasTag()) item.getTag().remove("ammo");
        return createProjectile(item, user, hand, velocity, user.getAbilities().instabuild ? ammo.get(index) : ammo.get(index).split(1));
    }

    public List<ItemStack> getAmmo(Player player) {
        List<ItemStack> ammo = new ObjectArrayList<>();
        for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if(item.is(ammoTag)) ammo.add(item);
        }
        return ammo;
    }

    protected int getSelectedAmmoIndex(ItemStack launcher, List<ItemStack> ammo, int fallbackIndex) {
        if(launcher.hasTag()) {
            CompoundTag tag = launcher.getTag();
            if(tag != null && tag.contains("ammo_item")) {
                ResourceLocation ammoId = ResourceLocation.tryParse(tag.getString("ammo_item"));
                if(ammoId != null) {
                    for(int i = 0; i < ammo.size(); i++) {
                        if(ammo.get(i).getItem().getRegistryName().equals(ammoId)) return i;
                    }
                }
            }
        }
        return fallbackIndex;
    }

    public static int getAmmoByte(ItemStack item) {
        return item.hasTag() ? item.getTag().getByte("ammo") : 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if(!useAction.get().canStart(player)) return InteractionResultHolder.fail(item);
        List<ItemStack> ammo = getAmmo(player);
        if(!ammo.isEmpty() || player.getAbilities().instabuild) {
            if(level.isClientSide()) {
                IPlayerData capP = PlayerData.get(player);
                if(capP.getActiveHand() == hand) {
                    ActionTracker.get(player).startAction(useAction.getId());
                    NetworkHandler.toServer(new ActionToServer(capP.isMainhandActive(), useAction.getId()));
                }
            }
            else if(!ammo.isEmpty()) {
                int index = LevelUtil.getModifiableItemIndex(level, player, hand);
                if(index < 0 || index >= ammo.size()) return InteractionResultHolder.fail(item);
                int id = getAmmoId(ammo.get(index));
                CompoundTag tag = item.getOrCreateTag();
                tag.putString("ammo_item", ammo.get(index).getItem().getRegistryName().toString());
                if(id != 0) tag.putByte("ammo", (byte) id);
            }
        }
        return InteractionResultHolder.fail(item);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !newStack.is(oldStack.getItem());
    }

    @Override
    public boolean hasAction(ResourceLocation id, Player player) {
        if(useAction.getId().equals(id)) return true;
        for(ResourceLocation linkedId : useAction.get().linkedActions) {
            if(linkedId.equals(id)) return true;
        }
        return false;
    }

    @Override
    public Optional<Screen> modifyStartClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand) {
        if(mc.screen == null) {
            IActionTracker capA = ActionTracker.get(player);
            if(capA.isInactive() || (capA.getAction() == useAction.get() && capA.getState() > useAction.get().getChargeState() + 1)) {
                List<ItemStack> ammo = getAmmo(player);
                if(!ammo.isEmpty()) return Optional.of(new SimpleModifiableItemScreen(PlayerData.get(player).isMainhandActive(), this, ammo));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Screen> modifyContinueClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        return modifyStartClient(mc, item, player, hand);
    }

    @Override
    public void modifyReleaseClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        if(mc.screen instanceof SimpleModifiableItemScreen) mc.screen.onClose();
    }

    @Override
    public void swapClient(Minecraft mc, ItemStack item, Player player, boolean mainHand) {
        ModifiableItemScreen.initSelection(mc, getAmmo(player), this, mainHand);
    }

    @Override
    public void onHeldTickClient(Minecraft mc, ItemStack item, Player player, boolean mainHand) {
        if(PlayerData.get(player).isMainhandActive() == mainHand) swapClient(mc, item, player, mainHand);
    }
}
