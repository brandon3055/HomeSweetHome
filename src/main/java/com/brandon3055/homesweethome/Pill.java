package com.brandon3055.homesweethome;

import com.brandon3055.brandonscore.items.ItemBCore;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.Map;

/**
 * Created by brandon3055 on 2/27/2018.
 */
public class Pill extends ItemBCore {
    public final int itemUseDuration;

    public Pill() {
        this.itemUseDuration = 32;
        this.setCreativeTab(CreativeTabs.FOOD);
        this.addName(0, "sleep");
        this.addName(1, "caffeine");
        this.setHasSubtypes(true);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
        }
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entityLiving;
            worldIn.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
            this.onFoodEaten(stack, worldIn, entityplayer);
            StatBase stat = StatList.getObjectUseStats(this);
            if (stat != null) {
                entityplayer.addStat(stat);
            }

            if (entityplayer instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) entityplayer, stack);
            }
        }

        stack.shrink(1);
        return stack;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        Map<Potion, PotionEffect> activeMap = player.getActivePotionMap();
        Potion potion = getPotion(stack);
        int amp = 0;

        if (activeMap.containsKey(potion)) {
            amp += (1 + activeMap.get(potion).getAmplifier());
        }

        if (potion != null) {
            int baseDuration = stack.getMetadata() == 0 ? 60 : 180;
            player.addPotionEffect(new PotionEffect(potion, (int) (baseDuration * (1D + (amp / 5D))) * 20, amp));
        }
    }

    private Potion getPotion(ItemStack stack) {
        if (stack.getMetadata() == 0) {
            return HomeSweetHome.POTION_DROWSY;
        }
        else {
            return HomeSweetHome.POTION_HYPED;
        }
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.EAT;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);

        Map<Potion, PotionEffect> activeMap = player.getActivePotionMap();
        Potion potion = getPotion(stack);
        if (activeMap.containsKey(potion) && activeMap.get(potion).getAmplifier() >= 8) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (player.canEat(true)) {
            player.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        else {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
    }
}
