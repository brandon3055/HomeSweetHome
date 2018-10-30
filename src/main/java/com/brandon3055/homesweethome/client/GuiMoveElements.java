package com.brandon3055.homesweethome.client;

import com.brandon3055.brandonscore.client.gui.modulargui_old.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui_old.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui_old.ModularGuiScreen;
import com.brandon3055.brandonscore.client.gui.modulargui_old.modularelements.MGuiPopUpDialog;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.homesweethome.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.function.BiConsumer;

/**
 * Created by brandon3055 on 3/01/2018.
 */
public class GuiMoveElements extends ModularGuiScreen {

    @Override
    public void initGui() {
        super.initGui();
        manager.clear();
        manager.add(createDraggable(this, (x, y) -> ModConfig.homeHudPos = new double[] {x, y}).setScreenRelPos(ModConfig.homeHudPos));
        manager.add(createDraggable(this, (x, y) -> ModConfig.statsHudPos = new double[] {x, y}).setScreenRelPos(ModConfig.statsHudPos));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!ClientEventHandler.hudKeyDown) {
            this.mc.player.closeScreen();
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        String string = I18n.format("hsh.gui.moveElements.clickAndDrag");
        int w = fontRenderer.getStringWidth(string);
        GuiHelper.drawColouredRect(0, height - fontRenderer.FONT_HEIGHT - 1, w + 2, mc.fontRenderer.FONT_HEIGHT + 1, 1342177280);
        fontRenderer.drawString(string, 1, height - fontRenderer.FONT_HEIGHT, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public DraggableElement createDraggable(IModularGui modularGui, BiConsumer<Double, Double> posChangeCallback) {
        MGuiElementBase dummyParent = new MGuiElementBase(modularGui);
        dummyParent.setWorldAndResolution(mc, width, height);
        return new DraggableElement(modularGui, dummyParent, posChangeCallback);
    }

    public static class DraggableElement extends MGuiPopUpDialog {

        private BiConsumer<Double, Double> posChangeCallback;

        DraggableElement(IModularGui modularGui, MGuiElementBase parent, BiConsumer<Double, Double> posChangeCallback) {
            super(modularGui, parent);
            this.posChangeCallback = posChangeCallback;
            xSize = 50;
            ySize = 50;
            dragZoneSize = ySize;
            closeOnCapturedClick = closeOnOutsideClick = false;
            canDrag = true;
        }

        public DraggableElement setScreenRelPos(double[] pos) {
            xPos = (int) (modularGui.screenWidth() * pos[0]) - 25;
            yPos = (int) (modularGui.screenHeight() * pos[1]) - 25;
            return this;
        }

        @Override
        public void moveBy(int xAmount, int yAmount) {
            super.moveBy(xAmount, yAmount);
            posChangeCallback.accept(((xPos + 25D) / modularGui.screenWidth()), (yPos + 25D) / modularGui.screenHeight());
            ModConfig.saveClientConfig();
        }

        @Override
        public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
            drawBorderedRect(xPos, yPos, xSize, ySize, 1, 0, 0xFFFFFFFF);
            drawColouredRect(xPos + 17, yPos + 24, 16, 2, 0xFF00FF00);
            drawColouredRect(xPos + 24, yPos + 17, 2, 16, 0xFF00FF00);
        }
    }
}
