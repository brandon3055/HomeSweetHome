package com.brandon3055.homesweethome.client;

import com.brandon3055.brandonscore.client.gui.ButtonColourRect;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.homesweethome.HomeSweetHome;
import com.brandon3055.homesweethome.network.PacketMakeHome;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

/**
 * Created by brandon3055 on 3/01/2018.
 */
public class GuiMakeHome extends GuiScreen {

    private int xSize = 300;
    private int ySize = 150;

    @Override
    public void initGui() {
        buttonList.clear();
        ySize = 120;
        int x = (width / 2) - (xSize / 2);
        int y = (height / 2) - (ySize / 2);
        buttonList.add(new ButtonColourRect(0, TextFormatting.GOLD + I18n.format("hsh.gui.makeHome.confirmHome"), x + 10, y + 80, xSize - 20, 14, 0x0, 0x705000FF, 0xFF5000FF));
        buttonList.add(new ButtonColourRect(1, TextFormatting.GOLD + I18n.format("hsh.gui.makeHome.cancelHome"), x + 10, y + 98, xSize - 20, 14, 0x0, 0x705000FF, 0xFF5000FF));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            HomeSweetHome.network.sendToServer(new PacketMakeHome(true));
        }

        this.mc.displayGuiScreen((GuiScreen)null);
        if (this.mc.currentScreen == null)
        {
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int x = (width / 2) - (xSize / 2);
        int y = (height / 2) - (ySize / 2);

        HSHGuiHelper.drawToolTipBackground(x, y, xSize, ySize);

        GuiHelper.drawCenteredSplitString(fontRenderer, I18n.format("hsh.gui.makeHome.youCanNowMakeThisYourHome"), x + xSize / 2, y + 3, xSize - 4, 0x00FF00, false);
        GuiHelper.drawCenteredSplitString(fontRenderer, I18n.format("hsh.gui.makeHome.makeHomeInfo1"), x + xSize / 2, y + 16, xSize - 4, 0xFF0000, false);
        GuiHelper.drawCenteredSplitString(fontRenderer, I18n.format("hsh.gui.makeHome.makeHomeInfo2"), x + xSize / 2, y + 38, xSize - 4, 0xFFFFFF, false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
