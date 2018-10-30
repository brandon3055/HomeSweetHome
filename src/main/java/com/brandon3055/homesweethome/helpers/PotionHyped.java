package com.brandon3055.homesweethome.helpers;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.homesweethome.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * Created by brandon3055 on 2/27/2018.
 */
public class PotionHyped extends Potion {

    public PotionHyped(int liquidColorIn) {
        super(false, liquidColorIn);
        setBeneficial();
        setPotionName("hsh.effect.hyped.name");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            PlayerData data = PlayerData.getPlayerData(player);
            if (data == null) return;
            int ticks = 1 + amplifier;
            data.addTimeAwake(-((ticks / 20D) / 60D));
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        ResourceHelperBC.bindTexture(ResourceHelperBC.getResourceRAW("homesweethome:textures/gui/effect_hyped.png"));
        GuiHelper.drawTexturedRect(x + 4, y + 5, 22, 22, 0, 0, 1 ,1, 0, 1);

        String nameString = I18n.format(getName());
        if (effect.getAmplifier() > 0) {
            nameString = nameString + " " + I18n.format("enchantment.level." + (effect.getAmplifier() + 1));
        }
        mc.fontRenderer.drawStringWithShadow(nameString, (float) (x + 28), (float) (y + 6), 16777215);
        String s = Potion.getPotionDurationString(effect, 1.0F);
        mc.fontRenderer.drawStringWithShadow(s, (float) (x + 28), (float) (y + 16), 8355711);
    }

    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        ResourceHelperBC.bindTexture(ResourceHelperBC.getResourceRAW("homesweethome:textures/gui/effect_hyped.png"));
        GuiHelper.drawTexturedRect(x + 2, y + 2, 20, 20, 0, 0, 1 ,1, 0, 1);
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return false;
    }
}
