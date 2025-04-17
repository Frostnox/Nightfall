package frostnox.nightfall.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.encyclopedia.EncyclopediaTab;
import frostnox.nightfall.client.gui.screen.encyclopedia.EntryClient;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class EntryToast extends SoundToast {
    protected enum Type {
        STANDARD, HIDDEN, ADDENDUM
    }
    public static final TranslatableComponent PREFIX_HIDDEN = new TranslatableComponent(Nightfall.MODID + ".entry_toast.prefix_hidden");
    public static final TranslatableComponent PREFIX_ADDENDUM = new TranslatableComponent(Nightfall.MODID + ".entry_toast.prefix_addendum");
    public static final TranslatableComponent PREFIX = new TranslatableComponent(Nightfall.MODID + ".entry_toast.prefix");
    protected final EntryClient entry;
    protected final Type type;
    protected final TranslatableComponent text, prefix;

    public EntryToast(Entry entry) {
        this.entry = entry.isAddendum ? ClientEngine.get().getEntry(entry.parents.get(0).getId()) : ClientEngine.get().getEntry(entry.getRegistryName());
        if(entry.isAddendum) type = Type.ADDENDUM;
        else if(entry.isHidden) type = Type.HIDDEN;
        else type = Type.STANDARD;
        prefix = switch(type) {
            case STANDARD -> PREFIX;
            case HIDDEN -> PREFIX_HIDDEN;
            case ADDENDUM -> PREFIX_ADDENDUM;
        };
        text = new TranslatableComponent(this.entry.entry.get().getDescriptionId());
    }

    @Override
    protected Component getText() {
        return prefix.copy().append(text);
    }

    @Override
    protected void renderBg(PoseStack stack, ToastComponent toastComponent, long timeSinceLastVisible) {
        if(!entry.itemIcon.isEmpty()) {
            RenderUtil.renderItem(stack, entry.itemIcon, 2, 2, 0, type == Type.HIDDEN);
        }
        else {
            if(type == Type.HIDDEN) RenderSystem.setShaderColor(0F, 0F, 0F, 1F);
            RenderSystem.setShaderTexture(0, entry.icon);
            GuiComponent.blit(stack, 2, 2, 0, 0, EncyclopediaTab.ICON_SIZE, EncyclopediaTab.ICON_SIZE, EncyclopediaTab.ICON_SIZE, EncyclopediaTab.ICON_SIZE);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void playSound(Visibility visibility, SoundManager soundManager) {
        if(visibility == Visibility.SHOW) ClientEngine.get().playToastSound(SoundsNF.ENTRY_UPDATED.get(), 1F, 1F);
    }
}
