package com.brandon3055.homesweethome.client;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.homesweethome.HomeSweetHome;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.network.PacketSyncClient;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * Created by brandon3055 on 3/01/2018.
 */
public class ClientEventHandler {

    private boolean showHomeHud = false;
    private boolean showEffects = false;
    private boolean lookingAtBed = false;
    private boolean showShiftTip = false;
    private int tick = 0;
    public static PlayerData data = null;
    public static boolean hudKeyDown = false;
    public static boolean shiftDown = false;
    public double dist = 0;

    public static KeyBinding homeInfo = new KeyBinding("key.showHomeInfo", Keyboard.KEY_NONE, HomeSweetHome.MOD_NAME);

    public static void init() {
        ClientRegistry.registerKeyBinding(homeInfo);
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            RayTraceResult rtr = mc.objectMouseOver;
            tick++;

            boolean requestUpdate = false;

            hudKeyDown = Keyboard.isKeyDown(ClientEventHandler.homeInfo.getKeyCode());
            shiftDown = GuiScreen.isShiftKeyDown();

            if (hudKeyDown) { //Show both huds
                requestUpdate = !showHomeHud || !showEffects;
                showHomeHud = true;
                showEffects = true;
                lookingAtBed = false;
                showShiftTip = !(mc.currentScreen instanceof GuiMoveElements);
                if (GuiScreen.isCtrlKeyDown() && showShiftTip) {
                    mc.displayGuiScreen(new GuiMoveElements());
                }
            }
            else if (tick % 10 == 0) { //If the player is looking at a bed show home hud using the beds location
                showEffects = showHomeHud = showShiftTip = lookingAtBed = false;
                if (rtr != null && rtr.typeOfHit == RayTraceResult.Type.BLOCK) {
                    IBlockState state = mc.world.getBlockState(rtr.getBlockPos());
                    if (state.getBlock() instanceof BlockBed) {
                        requestUpdate = !showHomeHud;
                        showHomeHud = true;
                        lookingAtBed = true;
                    }
                }

                if (data != null && (data.isHomeSick() || data.isTired())) {
                    showEffects = true;
                }
            }

            if (showHomeHud || showEffects) {
                if (requestUpdate || tick % 20 == 0) {
                    PacketSyncClient.requestUpdateClientSide();
                }

                if (data != null && data.hasHome()) {
                    PlayerHome home = data.getHome();
                    dist = lookingAtBed ? home.getDistance(rtr.getBlockPos()) : home.getDistance(mc.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void renderGameOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || (mc.currentScreen != null && !(mc.currentScreen instanceof GuiMoveElements))) {
            return;
        }

        if ((showHomeHud || showEffects) && data != null) {
            GlStateManager.pushMatrix();
            ScaledResolution resolution = event.getResolution();
            int width = resolution.getScaledWidth();
            int height = resolution.getScaledHeight();

            int x = (int) (0.5D * width);
            int y = (int) (0.5D * height);
            if (showHomeHud) {
                renderHomeHud(mc.fontRenderer, mc, (int) (width * ModConfig.homeHudPos[0]), (int) (height * ModConfig.homeHudPos[1]), width, height);
            }
//            if (showEffects) {
            renderEffectHud(mc.fontRenderer, mc, (int) (width * ModConfig.statsHudPos[0]), (int) (height * ModConfig.statsHudPos[1]), width, height);
//            }

            if (showShiftTip) {
                String string = I18n.format("hsh.hud.shiftToMoveElements");
                int w = mc.fontRenderer.getStringWidth(string);
                GuiHelper.drawColouredRect(0, height - mc.fontRenderer.FONT_HEIGHT - 1, w + 2, mc.fontRenderer.FONT_HEIGHT + 1, 1342177280);
                mc.fontRenderer.drawString(string, 1, height - mc.fontRenderer.FONT_HEIGHT, 0xFFFFFF);
            }

            GlStateManager.popMatrix();
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderEffectHud(FontRenderer fr, Minecraft mc, int x, int y, int screenWidth, int screenHeight) {
        int ySize = 0;
        int xSize = 100;
        boolean drawDaysAway = hudKeyDown || data.isHomeSick();
        boolean drawTimeSinceSleep = hudKeyDown || data.isHomeSick() || lookingAtBed;
        int daysAwayColour = MathHelper.hsvToRGB(Math.max(0.0F, (float) data.getTimeUntilHomesickAsDouble()) / 3.0F, 1.0F, 1.0F);
        int daysSleepColour = MathHelper.hsvToRGB(Math.max(0.0F, (float) data.getTimeUntilTiredAsDouble()) / 3.0F, 1.0F, 1.0F);

        Map<String, Integer> lines = new LinkedHashMap<>();

        if (data.isHomeSick()) {
            lines.put(TextFormatting.DARK_RED + I18n.format("hsh.hud.youAreHomeSick"), 0xFFFFFF);
        }
        if (drawDaysAway) {
            lines.put(TextFormatting.WHITE + I18n.format("hsh.hud.daysAwayFromHome") + TextFormatting.RESET + " " + data.getTimeAwayReadable(), daysAwayColour);
        }
        if (data.isTired()) {
            lines.put(TextFormatting.DARK_RED + I18n.format("hsh.hud.youAreTired"), 0xFFFFFF);
        }
        if (drawTimeSinceSleep) {
            lines.put(TextFormatting.WHITE + I18n.format("hsh.hud.daysWithoutSleep") + TextFormatting.RESET + " " + data.getTimeSinceSleepReadable(), daysSleepColour);
        }
        if (drawDaysAway || drawTimeSinceSleep) {
            lines.put(TextFormatting.WHITE + I18n.format("hsh.hud.distFromHome", RESET + "" + (int) dist + "" + WHITE), 0xFFAA00);
        }

        if (lines.isEmpty()) return;

        for (String s : lines.keySet()) {
            ySize += fr.FONT_HEIGHT;
            xSize = Math.max(xSize, fr.getStringWidth(s));
        }
        y -= ySize / 2;

        //Ensure hud is within screen bounds
        if (x < 4) x = 4;
        else if (x + xSize + 4 > screenWidth) x = screenWidth - xSize - 4;
        if (y < 4) y = 4;
        if (y + ySize + 4 > screenHeight) y = screenHeight - ySize - 4;

        //Draw hud
        GuiHelper.drawColouredRect(x - 3, y - 3, xSize + 6, ySize + 6, 1342177280);

        int drawY = y;
        for (String s : lines.keySet()) {
            fr.drawString(s, x, drawY, lines.get(s), false);
            drawY += fr.FONT_HEIGHT;
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderHomeHud(FontRenderer fr, Minecraft mc, int x, int y, int screenWidth, int screenHeight) {
        int xSize = 100;
        int ySize = 0;

        PlayerHome home = data.getHome();
        List<String> lines = new ArrayList<>();
        boolean drawHomeliness = false;

        if (!data.hasHome()) {//If does not have home render homeless text
            lines.add(TextFormatting.DARK_RED + I18n.format("hsh.hud.noHome"));
            lines.add(I18n.format(lookingAtBed ? "hsh.hud.sleepInBed" : "hsh.hud.sleepInABed"));
        }
        else if (dist > home.getHomeRadius()) { //If not within home
            xSize = 160;
            lines.add(TextFormatting.DARK_RED + I18n.format("hsh.hud.notYourHome"));
            //If current home is not permanent tell player this can be their home
            if (!home.isPermanent()) {
                lines.add(I18n.format("hsh.hud.canStillBeHome"));
                lines.add(I18n.format("hsh.hud.canStillBeHome2"));
            }
            else {
                lines.add(TextFormatting.DARK_RED + I18n.format("hsh.hud.wontEnjoySleep"));
            }
        }
        else if (!home.isPermanent()) { //If not in home and have no perm home
            xSize = 170;
            if (home.homeliness.getLevel() >= ModConfig.levelForPerm) {
                lines.add(TextFormatting.GREEN + I18n.format("hsh.hud.canBeHome"));
                lines.add(I18n.format("hsh.hud.nextSleepOption"));
            }
            else {
                lines.add(TextFormatting.GREEN + I18n.format("hsh.hud.youHaveAHome"));
                lines.add(I18n.format("hsh.hud.butItsNotHomey"));
                lines.add(I18n.format("hsh.hud.requiredLevelForPerm", ModConfig.levelForPerm));
            }
            drawHomeliness = true;
        }
        else {  //If not in home and has a perm home
            xSize = 170;
            lines.add(TextFormatting.GREEN + I18n.format("hsh.hud.homeSweetHome"));
            drawHomeliness = true;
        }

        boolean extended = shiftDown;
        int statSize = extended ? 68 : 28;
        if (drawHomeliness) {
            ySize += statSize;
        }

        for (String s : lines) {
            ySize += fr.getWordWrappedHeight(s, xSize);
        }
        y -= ySize / 2;

        //Ensure hud is within screen bounds
        if (x < 4) x = 4;
        else if (x + xSize + 4 > screenWidth) x = screenWidth - xSize - 4;
        if (y < 4) y = 4;
        if (y + ySize + 4 > screenHeight) y = screenHeight - ySize - 4;

        //Draw hud
        HSHGuiHelper.drawToolTipBackground(x, y, xSize, ySize);

        int drawY = y;
        for (String s : lines) {
            GuiHelper.drawCenteredSplitString(fr, s, x + xSize / 2, drawY, xSize, 0xFFFFFF, false);
            drawY += fr.getWordWrappedHeight(s, xSize);
        }

        if (drawHomeliness) {
            drawHomeStats(fr, home, x, y + ySize - statSize, xSize, extended);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawHomeStats(FontRenderer fr, PlayerHome home, int x, int y, int xSize, boolean extended) {
        GuiHelper.drawBorderedRect(x, y, xSize, extended ? 68 : 21, 1, 0, 0xFF505050);
        fr.drawString(I18n.format("hsh.hud.level") + " " + GOLD + home.homeliness.getLevel(), x + 2, y + 2, 0x00FFFF);

        //Draw Bar
        GuiHelper.drawBorderedRect(x + 1, y + 10, xSize - 2, 10, 1, 0xFF000000, 0xFF5000FF);
        double progress = data.getHome().homeliness.getNextLevelProgress();
        GuiHelper.drawColouredRect(x + 2, y + 11, (int) ((xSize - 4) * progress), 10 - 2, 0x7000FFFF);
        if (!extended) {
            fr.drawString(I18n.format("hsh.hud.sneakForStats"), x + 2, y + 22, 0x404040);
            return;
        }

        //Draw lower stats
        Vec3d pos = home.getPos();
        fr.drawString(I18n.format("hsh.hud.homePos") + String.format(GOLD + " %s, %s, %s", (int) pos.x, (int) pos.y, (int) pos.z), x + 2, y + 22, 0x00FFFF);
        fr.drawString(I18n.format("hsh.hud.radius", GOLD.toString() + home.getHomeRadius()), x + 2, y + 31, 0x00FFFF);
        GuiHelper.drawCenteredSplitString(fr, GOLD + I18n.format("hsh.hud.distance", round(dist, 10)), x + xSize / 2, y + 41, xSize, 0xFFFFFF, false);
        fr.drawString(I18n.format("hsh.hud.levelHere") + " " + GOLD.toString() + home.getLevelHere(dist), x + 2, y + 59, 0x00FFFF);
    }

    private double round(double value, double mult) {
        return Math.round(value * mult) / mult;
    }
}