package com.brandon3055.homesweethome.integration;

import c4.comforts.common.blocks.BlockHammock;
import c4.comforts.common.blocks.BlockSleepingBag;
import c4.comforts.common.blocks.ComfortsBlocks;
import c4.comforts.common.items.ItemSleepingBag;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

/**
 * Created by brandon3055 on 1/9/2018.
 */
public class IntegrationHelper {

    public static void handleSleepingBag(EntityPlayer player, BlockPos bedPos) {
        if (Loader.isModLoaded("comforts")) {
            handleComfortsSleepingBag(player, bedPos);
        }
    }

    @Optional.Method(modid = "comforts")
    private static void handleComfortsSleepingBag(EntityPlayer player, BlockPos bedPos) {
        World world = player.world;
        if (world.getBlockState(bedPos).getBlock() instanceof BlockBed) {
            return;
        }

        EnumHand hand;
        if (player.getHeldItemMainhand().getItem() instanceof ItemSleepingBag) {
            hand = EnumHand.MAIN_HAND;
        }
        else if (player.getHeldItemOffhand().getItem() instanceof ItemSleepingBag) {
            hand = EnumHand.OFF_HAND;
        }
        else {
            return;
        }

        ItemStack stack = player.getHeldItem(hand);
        EnumFacing enumfacing = player.getHorizontalFacing();

        BlockPos blockpos = bedPos;
        BlockPos pos = blockpos.offset(enumfacing.getOpposite());

        IBlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();
        boolean flag = block.isReplaceable(world, pos);
        IBlockState iblockstate1 = world.getBlockState(blockpos);
        if (!flag) {
            pos = pos.up();
        }

        float xOffset = 0.5F + (float)enumfacing.getFrontOffsetX() * 0.4F;
        float zOffset = 0.5F + (float)enumfacing.getFrontOffsetZ() * 0.4F;
        float yOffset = player.world.getBlockState(blockpos).getBlock() instanceof BlockHammock ? 0.1875F : 0.3125F;
        player.setPosition((double)((float)blockpos.getX() + xOffset), (double)((float)blockpos.getY() + yOffset), (double)((float)blockpos.getZ() + zOffset));

        IBlockState bedFoot = ComfortsBlocks.SLEEPING_BAGS[stack.getMetadata()].getDefaultState().withProperty(BlockSleepingBag.OCCUPIED, false).withProperty(BlockSleepingBag.FACING, enumfacing).withProperty(BlockSleepingBag.PART, BlockSleepingBag.EnumPartType.FOOT);
        IBlockState beadHead = bedFoot.withProperty(BlockSleepingBag.PART, BlockSleepingBag.EnumPartType.HEAD).withProperty(BlockSleepingBag.OCCUPIED, true);
        world.setBlockState(pos, bedFoot, 10);
        world.setBlockState(blockpos, beadHead, 10);
        SoundType soundtype = bedFoot.getBlock().getSoundType(bedFoot, world, pos, player);
        world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        world.notifyNeighborsRespectDebug(pos, block, false);
        world.notifyNeighborsRespectDebug(blockpos, iblockstate1.getBlock(), false);
        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        stack.shrink(1);
    }
}
