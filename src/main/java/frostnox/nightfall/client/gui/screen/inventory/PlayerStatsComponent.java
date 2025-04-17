package frostnox.nightfall.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.client.gui.OverlayNF;
import frostnox.nightfall.client.gui.screen.ScreenGuiComponent;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public class PlayerStatsComponent extends ScreenGuiComponent {
    private final Minecraft mc;
    private final PlayerInventoryScreen screen;

    public PlayerStatsComponent(PlayerInventoryScreen screen) {
        mc = Minecraft.getInstance();
        this.screen = screen;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PlayerInventoryScreen.TEXTURE);
        int x = screen.getLeftPos() - 112 - 1;
        int y = screen.getTopPos();
        blit(poseStack, x, y, 176, 0, 112, PlayerInventoryScreen.IMAGE_HEIGHT, 512, 256);
        y += 13;
        LocalPlayer player = mc.player;
        RenderUtil.drawCenteredFont(poseStack, mc.font, RenderUtil.ATTRIBUTES_TEXT.plainCopy().withStyle(ChatFormatting.UNDERLINE), x + 112/2, y, 0x000000, false);
        y += 10;
        int margin = 8;
        RenderSystem.setShaderTexture(0, OverlayNF.TEXTURE);
        int iconYStart = y;
        for(int i = 0; i < 12; i++) {
            int iconY = y - 1 + i * 10;
            if(i > 5) iconY += 11;
            blit(poseStack, x + margin, iconY, i * 9, 96, 9, 9, 256, 256);
        }
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            mc.font.draw(poseStack, RenderUtil.getAttributeText(attribute), x + margin + 10, y, 0x000000);
            if(attribute == PlayerAttribute.WILLPOWER) RenderUtil.drawRightText(poseStack, mc.font, "?", x + 112 - margin, y, 0x000000, false, LightTexture.FULL_BRIGHT);
            else RenderUtil.drawRightText(poseStack, mc.font, "" + AttributesNF.getValue(player, attribute), x + 112 - margin, y, 0x000000, false, LightTexture.FULL_BRIGHT);
            y += 10;
        }
        RenderUtil.drawCenteredFont(poseStack, mc.font, RenderUtil.RESISTANCES_TEXT.plainCopy().withStyle(ChatFormatting.UNDERLINE), x + 112/2, y, 0x000000, false);
        y += 1;
        EnumMap<DamageType, Double> armorDefenses = new EnumMap<>(DamageType.class);
        EnumMap<DamageType, Double> armorAbsorptions = new EnumMap<>(DamageType.class);
        EnumMap<DamageType, Double> baseDefenses = new EnumMap<>(DamageType.class);
        EnumMap<DamageType, Double> baseAbsorptions = new EnumMap<>(DamageType.class);
        for(DamageType type : DamageType.STANDARD_TYPES) {
            armorDefenses.put(type, 0D);
            armorAbsorptions.put(type, 0D);
            baseDefenses.put(type, player.getAttribute(AttributesNF.getDefense(type)).getValue());
            baseAbsorptions.put(type, player.getAttribute(AttributesNF.getAbsorption(type)).getValue() * 100);
        }
        for(int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if(stack.getItem() instanceof TieredArmorItem armor) {
                float durabilityPenalty = CombatUtil.getArmorDefenseDurabilityPenalty(stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage());
                for(DamageType type : DamageType.STANDARD_TYPES) {
                    armorDefenses.put(type, armorDefenses.get(type) + armor.material.getDefense(armor.slot, type.asArray(), true) * durabilityPenalty);
                    armorAbsorptions.put(type, armorAbsorptions.get(type) + armor.material.getAbsorption(armor.slot, type.asArray(), true) * 100);
                }
            }
        }
        //TODO: Accessories
        DecimalFormat format = new DecimalFormat("0.0");
        DecimalFormat formatShort = new DecimalFormat("0");
        for(DamageType type : DamageType.STANDARD_TYPES) {
            y += 10;
            mc.font.draw(poseStack, RenderUtil.getDamageTypeText(type).getString(), x + 10 + margin, y, 0x000000);
            RenderUtil.drawRightText(poseStack, mc.font,
                    formatShort.format(armorDefenses.get(type) + baseDefenses.get(type)) + "/" +
                            formatShort.format((armorAbsorptions.get(type) + baseAbsorptions.get(type))) + "%",
                    x + 112 - margin, y, 0x000000, false, LightTexture.FULL_BRIGHT);
        }
        //Tooltips
        if(mouseX >= x + margin && mouseX < x + 112 - margin) {
            for(int i = 0; i < 13; i++) {
                int iconY = iconYStart - 1 + i * 10;
                if(i > 6) iconY += 1;
                if(mouseY >= iconY - 1 && mouseY < iconY + (i == 6 ? 10 : 9)) {
                    List<Component> components = new ObjectArrayList<>();
                    if(i < 6) {
                        PlayerAttribute attribute = PlayerAttribute.values()[i];
                        int modifier = AttributesNF.getValue(player, attribute) - 10;
                        String plus = modifier >= 0 ? "+" : "";
                        switch(attribute) {
                            case VITALITY -> {
                                components.add(new TranslatableComponent("screen." + attribute + ".info", (int) player.getAttributeValue(Attributes.MAX_HEALTH)));
                            }
                            case ENDURANCE -> {
                                components.add(new TranslatableComponent("screen." + attribute + ".info", (int) AttributesNF.getMaxStamina(player), plus + 5 * modifier));
                            }
                            case WILLPOWER -> {
                                components.add(new TranslatableComponent("screen." + attribute + ".info"));
                            }
                            case STRENGTH -> {
                                components.add(new TranslatableComponent("screen." + attribute + ".info", plus + 5 * modifier));
                            }
                            case AGILITY -> {
                                components.add(new TranslatableComponent("screen." + attribute + ".info", plus + 3 * modifier));
                            }
                            case PERCEPTION -> {
                                components.add(new TranslatableComponent("screen." + attribute + ".info"));
                            }
                        }
                    }
                    else if(i == 6) {
                        components.add(new TranslatableComponent("screen.defenses.info_0"));
                        components.add(new TranslatableComponent("screen.defenses.info_1"));
                        components.add(new TranslatableComponent("screen.defenses.info_2"));
                    }
                    else {
                        DamageType type = DamageType.values()[i - 7];
                        components.add(RenderUtil.EFFECT_DEFENSE_TEXT.plainCopy().append(format.format(baseDefenses.get(type)) + "/" +
                                format.format(baseAbsorptions.get(type)) + "%"));
                        components.add(RenderUtil.ARMOR_DEFENSE_TEXT.plainCopy().append(format.format(armorDefenses.get(type)) + "/" +
                                format.format(armorAbsorptions.get(type)) + "%"));
                    }
                    screen.renderTooltip(poseStack, components, Optional.empty(), mouseX, mouseY);
                    break;
                }
            }
        }
    }
}