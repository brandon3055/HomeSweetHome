package com.brandon3055.homesweethome.client;

import codechicken.lib.util.ClientUtils;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.homesweethome.HomeSweetHome;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.network.PacketDispatcher;
import com.brandon3055.homesweethome.network.PacketSyncClient;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

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
    public void guiOpen(GuiScreenEvent.InitGuiEvent event) {
        if (event.getGui() instanceof GuiSleepMP) {
            GuiSleepMP gui = (GuiSleepMP) event.getGui();
            gui.buttonList.clear();
            gui.labelList.clear();
            gui.buttonList.add(new GuiButton(99, gui.width / 2 - 100, gui.height - 40, "") {

                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                    String text = mc.player.isPlayerFullyAsleep() ? I18n.format("hsh.msg.sleep.wakeUp") : I18n.format("multiplayer.stopSleeping");
                    if (this.visible) {
                        FontRenderer fontrenderer = mc.fontRenderer;
                        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                        int i = this.getHoverState(this.hovered);
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                        this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                        this.mouseDragged(mc, mouseX, mouseY);
                        int j = 14737632;

                        if (packedFGColour != 0) {
                            j = packedFGColour;
                        }
                        else if (!this.enabled) {
                            j = 10526880;
                        }
                        else if (this.hovered) {
                            j = 16777120;
                        }

                        this.drawCenteredString(fontrenderer, text, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
                    }
                }

                @Override
                public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                    boolean press = super.mousePressed(mc, mouseX, mouseY);
                    if (press) {
                        PacketDispatcher.wakeUpPlayer(mc.player.isPlayerFullyAsleep());
                    }
                    return press;
                }
            });
            GuiLabel label;
            gui.labelList.add(label = new GuiLabel(gui.mc.fontRenderer, 42, (gui.width / 2) - 150, 50, 300, 26, 0) {
                @Override
                public void drawLabel(Minecraft mc, int mouseX, int mouseY) {
                    visible = mc.player.isPlayerFullyAsleep();
                    if (visible) {
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        this.drawLabelBackground(mc, mouseX, mouseY);
                        GuiHelper.drawCenteredSplitString(gui.mc.fontRenderer, I18n.format("hsh.msg.sleep.youCanWakeUpOrWait"), this.x + this.width / 2, y + 5, width, 0x00FF00, false);
                    }
                }
            });
            label.setCentered();
            label.backColor = 0xFF000000;
            label.brColor = 0xFF00FFFF;
            label.ulColor = 0xFF004444;
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ClientUtils.inWorld()) {
            Minecraft mc = Minecraft.getMinecraft();
            RayTraceResult rtr = mc.objectMouseOver;
            tick++;

            if (data == null) {
                PacketSyncClient.requestUpdateClientSide();
                return;
            }

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

                if (data.isHomeSick() || data.isTired()) {
                    showEffects = true;
                }
            }

            if (showHomeHud || showEffects || tick % 100 == 0) {
                if (requestUpdate || tick % 20 == 0) {
                    PacketSyncClient.requestUpdateClientSide();
                }
                if (data.hasHome()) {
                    PlayerHome home = data.getHome();
                    dist = lookingAtBed && rtr != null && rtr.typeOfHit == RayTraceResult.Type.BLOCK ? home.getDistance(rtr.getBlockPos()) : home.getDistance(mc.player);
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

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.debugRenderer.shouldRender() || data == null || !data.hasHome()) {
            return;
        }

        EntityPlayer player = mc.player;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        double offsetX = player.prevPosX + (player.posX - player.prevPosX) * (double) mc.getRenderPartialTicks();
        double offsetY = player.prevPosY + (player.posY - player.prevPosY) * (double) mc.getRenderPartialTicks();
        double offsetZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) mc.getRenderPartialTicks();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.disableTexture2D();

        List<ChunkPos> chunks = data.getHome().getLoadingChunks();

        for (ChunkPos pos : chunks) {
            int x = (pos.x * 16) + 8;
            int z = (pos.z * 16) + 8;

            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            for (int i = 0; i < 4; i++) {
                double rot = ((BCClientEventHandler.elapsedTicks + mc.getRenderPartialTicks()) / 10F) + (i * Math.PI) / 2;
                double xRot = Math.cos(rot) * 2;
                double zRot = Math.sin(rot) * 2;
                greenLine(buffer, x + xRot, z + zRot, offsetX, offsetY, offsetZ);
            }
            redLine(buffer, x, z, offsetX, offsetY, offsetZ);

//            redLine(buffer, x - 8, z - 8, offsetX, offsetY, offsetZ);
//            redLine(buffer, x + 8, z - 8, offsetX, offsetY, offsetZ);
//            redLine(buffer, x - 8, z + 8, offsetX, offsetY, offsetZ);
//            redLine(buffer, x + 8, z + 8, offsetX, offsetY, offsetZ);
//            for (int i = 0; i < 32; i++) {
//                int yPos = i * 8;
//                buffer.pos(x - offsetX - 8, yPos - offsetY, z - offsetZ + 8).color(255, 255, 0, 255).endVertex();
//                buffer.pos(x - offsetX + 8, yPos - offsetY, z - offsetZ + 8).color(255, 255, 0, 255).endVertex();
//                buffer.pos(x - offsetX - 8, yPos - offsetY, z - offsetZ - 8).color(255, 255, 0, 255).endVertex();
//                buffer.pos(x - offsetX + 8, yPos - offsetY, z - offsetZ - 8).color(255, 255, 0, 255).endVertex();
//
//                buffer.pos(x - offsetX + 8, yPos - offsetY, z - offsetZ - 8).color(255, 255, 0, 255).endVertex();
//                buffer.pos(x - offsetX + 8, yPos - offsetY, z - offsetZ + 8).color(255, 255, 0, 255).endVertex();
//                buffer.pos(x - offsetX - 8, yPos - offsetY, z - offsetZ - 8).color(255, 255, 0, 255).endVertex();
//                buffer.pos(x - offsetX - 8, yPos - offsetY, z - offsetZ + 8).color(255, 255, 0, 255).endVertex();
//            }

            tessellator.draw();
        }

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void greenLine(BufferBuilder buffer, double x, double z, double offX, double offY, double offZ) {
        buffer.pos(x - offX, 0 - offY, z - offZ).color(0, 255, 0, 255).endVertex();
        buffer.pos(x - offX, 255 - offY, z - offZ).color(0, 255, 0, 255).endVertex();
    }

    private void redLine(BufferBuilder buffer, double x, double z, double offX, double offY, double offZ) {
        buffer.pos(x - offX, 0 - offY, z - offZ).color(255, 0, 0, 255).endVertex();
        buffer.pos(x - offX, 255 - offY, z - offZ).color(255, 0, 0, 255).endVertex();
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
            int minutes = (int) data.getTimeAway();
            int seconds = (int) ((data.getTimeAway() % 1) * 60);
            String mins = "(" + (minutes > 9 ? minutes : "0" + minutes) + ":" + (seconds > 9 ? seconds : "0" + seconds) + ")";
            lines.put(TextFormatting.WHITE + I18n.format("hsh.hud.daysAwayFromHome") + TextFormatting.RESET + " " + data.getDaysAwayRounded() + (shiftDown ? " " + mins : ""), daysAwayColour);
        }
        if (data.isTired()) {
            lines.put(TextFormatting.DARK_RED + I18n.format("hsh.hud.youAreTired"), 0xFFFFFF);
        }
        if (drawTimeSinceSleep) {
            int minutes = (int) data.getTimeAwake();
            int seconds = (int) ((data.getTimeAwake() % 1) * 60);
            String mins = "(" + (minutes > 9 ? minutes : "0" + minutes) + ":" + (seconds > 9 ? seconds : "0" + seconds) + ")";
            lines.put(TextFormatting.WHITE + I18n.format("hsh.hud.daysWithoutSleep") + TextFormatting.RESET + " " + data.getDaysAwakeRounded() + (shiftDown ? " " + mins : ""), daysSleepColour);
            if (hudKeyDown || lookingAtBed) {
                double time = Math.max(0, ModConfig.timeAwakeToSleep - data.getTimeAwake());
                minutes = (int) time;
                seconds = (int) ((time % 1) * 60);
                mins = (minutes > 9 ? minutes : "0" + minutes) + ":" + (seconds > 9 ? seconds : "0" + seconds);
                lines.put(I18n.format("hsh.hud.canSleepIn", mins), 0xFFFFFF);
            }
        }
        if (drawDaysAway || drawTimeSinceSleep) {
            if (dist == Integer.MAX_VALUE) {
                lines.put(I18n.format("hsh.hud.veryFarFromHome"), 0xFF5000);
            }
            else {
                lines.put(TextFormatting.WHITE + I18n.format("hsh.hud.distFromHome" + ((int) dist > 1 || (int) dist == 0 ? "2" : ""), RESET + "" + (int) dist + "" + WHITE), 0xFFAA00);
            }
        }
        if (hudKeyDown && data.hasHome()) {
            int lr = data.getHome().getLoadingRange();
            if (lr > 0 && ModConfig.enableChunkLoading){
                lines.put(TextFormatting.WHITE + I18n.format("hsh.hud.loading_range") + ": " + RESET + lr, 0xFFAA00);
            }
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
