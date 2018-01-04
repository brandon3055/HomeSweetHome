package com.brandon3055.homesweethome.client;

import com.brandon3055.brandonscore.client.utils.GuiHelper;

/**
 * Created by brandon3055 on 3/01/2018.
 */
public class HSHGuiHelper {
    public static void drawToolTipBackground(int x, int y, int width, int height) {
        int backgroundColor = 0xF0100010;
        GuiHelper.drawGradientRect(x - 3, y - 4, x + width + 3, y - 3, backgroundColor, backgroundColor, 1, 0);
        GuiHelper.drawGradientRect(x - 3, y + height + 3, x + width + 3, y + height + 4, backgroundColor, backgroundColor, 1, 0);
        GuiHelper.drawGradientRect(x - 3, y - 3, x + width + 3, y + height + 3, backgroundColor, backgroundColor, 1, 0);
        GuiHelper.drawGradientRect(x - 4, y - 3, x - 3, y + height + 3, backgroundColor, backgroundColor, 1, 0);
        GuiHelper.drawGradientRect(x + width + 3, y - 3, x + width + 4, y + height + 3, backgroundColor, backgroundColor, 1, 0);
        int border = 0x505000FF;
        int l1 = (border & 0xFEFEFE) >> 1 | border & 0xFF000000;
        GuiHelper.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, border, l1, 1, 0);
        GuiHelper.drawGradientRect(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, border, l1, 1, 0);
        GuiHelper.drawGradientRect(x - 3, y - 3, x + width + 3, y - 3 + 1, border, border, 1, 0);
        GuiHelper.drawGradientRect(x - 3, y + height + 2, x + width + 3, y + height + 3, l1, l1, 1, 0);
    }
}
