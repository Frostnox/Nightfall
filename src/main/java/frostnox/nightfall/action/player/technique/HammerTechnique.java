package frostnox.nightfall.action.player.technique;

import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.attack.VerticalSwingCharged;
import frostnox.nightfall.block.block.anvil.AnvilAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.world.GridUseToServer;
import frostnox.nightfall.registry.EntriesNF;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class HammerTechnique extends VerticalSwingCharged {
    private final AnvilAction type, chargedType;

    public HammerTechnique(AnvilAction type, AnvilAction chargedType, float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
        this.type = type;
        this.chargedType = chargedType;
    }

    public HammerTechnique(AnvilAction type, AnvilAction chargedType, float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
        this.type = type;
        this.chargedType = chargedType;
    }

    public HammerTechnique(AnvilAction type, AnvilAction chargedType, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(0, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
        this.type = type;
        this.chargedType = chargedType;
    }

    @Override
    public void onClientTick(Player player) {
        if(!player.level.isClientSide()) return;
        Vec3i lookingAt = ClientEngine.get().microHitResult;
        BlockPos pos = ClientEngine.get().microBlockEntityPos;
        IActionTracker capA = ActionTracker.get(player);
        if(capA.getState() == 1 && !capA.isStunned()) {
            if(capA.getFrame() == getBlockHitFrame(1, player) && lookingAt != null && pos != null) {
                AnvilAction type = (capA.getCharge() > capA.getAction().getMaxCharge() / 2) ? chargedType : this.type;
                NetworkHandler.toServer(new GridUseToServer(type.ordinal(), lookingAt, pos));
                PlayerData.get(player).setHitStopFrame(capA.getFrame());
            }
        }
    }

    @Override
    public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
        List<Component> tooltips = super.getTooltips(stack, level, isAdvanced);
        if(ClientEngine.get().isShiftHeld() && ClientEngine.get().getPlayer() != null && PlayerData.get(ClientEngine.get().getPlayer()).hasCompletedEntry(EntriesNF.SMITHING.getId())) {
            tooltips.add(new TextComponent(" ").append(new TranslatableComponent("anvil.action.context").withStyle(ChatFormatting.GRAY))
                    .append(new TranslatableComponent("anvil.action." + type.toString().toLowerCase(Locale.ROOT) + ".info").withStyle(ChatFormatting.DARK_AQUA)));
            tooltips.add(new TextComponent(" ").append(new TranslatableComponent("anvil.action.context_charged").withStyle(ChatFormatting.GRAY))
                    .append(new TranslatableComponent("anvil.action." + chargedType.toString().toLowerCase(Locale.ROOT) + ".info").withStyle(ChatFormatting.DARK_AQUA)));
        }
        return tooltips;
    }
}
