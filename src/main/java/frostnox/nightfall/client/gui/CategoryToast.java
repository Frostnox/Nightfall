package frostnox.nightfall.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.encyclopedia.EncyclopediaCategory;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class CategoryToast extends SoundToast {
    public static final TranslatableComponent PREFIX = new TranslatableComponent(Nightfall.MODID + ".category_toast.prefix");
    protected final EncyclopediaCategory category;
    protected final TranslatableComponent text;

    public CategoryToast(EncyclopediaCategory category) {
        this.category = category;
        text = new TranslatableComponent(category.name());
    }

    @Override
    public long visibleTime() {
        return 5000L;
    }

    @Override
    protected Component getText() {
        return PREFIX.copy().append(text);
    }

    @Override
    protected void renderBg(PoseStack stack, ToastComponent toastComponent, long timeSinceLastVisible) {
        RenderSystem.setShaderTexture(0, category.icon());
        GuiComponent.blit(stack, 2, 2, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void playSound(Visibility visibility, SoundManager soundManager) {
        if(visibility == Visibility.SHOW) ClientEngine.get().playToastSound(SoundsNF.CATEGORY_REVEALED.get(), 1F, 0.75F);
    }
}
