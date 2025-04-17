package frostnox.nightfall.client.gui;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.sounds.SoundManager;

public interface ISoundToast extends Toast {
    void playSound(Visibility visibility, SoundManager soundManager);
}
