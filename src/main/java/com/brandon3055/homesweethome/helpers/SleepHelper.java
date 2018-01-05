package com.brandon3055.homesweethome.helpers;

import com.brandon3055.homesweethome.HomeSweetHome;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.network.PacketMakeHome;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static com.brandon3055.homesweethome.helpers.HSHEventHelper.Event.*;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class SleepHelper {

    /**
     * Called when the player wakes up having skipped the night.
     */
    public static void onPlayerWakeUp(EntityPlayerMP player) {
        PlayerData data = PlayerData.getPlayerData(player);
        if (data == null) return;

        if (data.hasPermHome()) {
            PlayerHome home = data.getHome();

            if (home.isPlayerInHome(player)) { //Slept in permanent home
                //Update sleep time
                data.setTimeSinceSleep(0);

                //decrease home sickness
                double timeAway = data.getTimeAway();
                data.setTimeAway(timeAway - (timeAway * ModConfig.homesickReductionSleep));

                //Add Homeliness
                data.getHome().homeliness.add(ModConfig.gainPerSleep);

                //apply effects
                EffectHelper.sleepHasHome(player, data, home, true);

                HSHEventHelper.fireEvent(SLEEP_HOME, player, data);
            }
            else { //Slept outside permanent home
                //Update sleep time
                data.setTimeSinceSleep(ModConfig.resetSleepOutside);

                //increase home sickness
                double timeAway = data.getTimeAway();
                data.setTimeAway(timeAway + ModConfig.homesickSleepAway);

                //Remove Homeliness
                data.getHome().homeliness.remove(ModConfig.lossPerSleepAway);

                //Apply effects
                EffectHelper.sleepHasHome(player, data, home, false);

                HSHEventHelper.fireEvent(SLEEP_AWAY, player, data);
            }
        }
        else { //If player has no home
            data.setTimeSinceSleep(ModConfig.resetSleepOutside);

            PlayerHome home = data.getHome();
            if (home == null || !home.isPlayerInHome(player)) {
                boolean first = home == null;
                home = data.setHome(new Vec3d(player.posX, player.posY, player.posZ));
                HSHEventHelper.fireEvent(first ? FIRST_HOME : MOVE_HOME, player, data);
            }
            else {
                HSHEventHelper.fireEvent(SLEEP_NON_PERM_HOME, player, data);
            }

            home.homeliness.add(ModConfig.gainPerSleep);
            EffectHelper.sleepNoHome(player, data);
            if (home.homeliness.getLevel() >= ModConfig.levelForPerm) {
                HomeSweetHome.network.sendTo(new PacketMakeHome(), player);
            }
        }
    }

    public static boolean shouldCancelSetSpawn(EntityPlayer player) {
        PlayerData data = PlayerData.getPlayerData(player);
        return data != null && data.hasPermHome();
    }


    public static SleepResult trySleep(EntityPlayer player, BlockPos bedLocation)
    {
//        SleepResult ret = net.minecraftforge.event.ForgeEventFactory.onPlayerSleepInBed(player, bedLocation);
//        if (ret != null) return ret;
        final IBlockState state = player.world.isBlockLoaded(bedLocation) ? player.world.getBlockState(bedLocation) : null;
        final boolean isBed = state != null && state.getBlock().isBed(state, player.world, bedLocation, player);
        final EnumFacing enumfacing = isBed && state.getBlock() instanceof BlockHorizontal ? (EnumFacing)state.getValue(BlockHorizontal.FACING) : null;

        if (!player.world.isRemote)
        {
            if (player.isPlayerSleeping() || !player.isEntityAlive())
            {
                return SleepResult.OTHER_PROBLEM;
            }

            if (!player.world.provider.isSurfaceWorld())
            {
                return SleepResult.NOT_POSSIBLE_HERE;
            }

            if (player.world.isDaytime())
            {
                //return SleepResult.NOT_POSSIBLE_NOW;
            }

            if (!player.bedInRange(bedLocation, enumfacing)) //bedInRange private
            {
                return SleepResult.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D; // SleepEnemyPredicate private //EntityPlayer.SleepEnemyPredicate private
            List<EntityMob> list = player.world.<EntityMob>getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)bedLocation.getX() - 8.0D, (double)bedLocation.getY() - 5.0D, (double)bedLocation.getZ() - 8.0D, (double)bedLocation.getX() + 8.0D, (double)bedLocation.getY() + 5.0D, (double)bedLocation.getZ() + 8.0D), new EntityPlayer.SleepEnemyPredicate(player));

            if (!list.isEmpty())
            {
                return SleepResult.NOT_SAFE;
            }
        }

        if (player.isRiding())
        {
            player.dismountRidingEntity();
        }

        player.spawnShoulderEntities(); //spawnShoulderEntities private
        player.setSize(0.2F, 0.2F); //setSize private

        if (enumfacing != null) {
            float f1 = 0.5F + (float)enumfacing.getFrontOffsetX() * 0.4F;
            float f = 0.5F + (float)enumfacing.getFrontOffsetZ() * 0.4F;
            player.setRenderOffsetForSleep(enumfacing); //setRenderOffsetForSleep private
            player.setPosition((double)((float)bedLocation.getX() + f1), (double)((float)bedLocation.getY() + 0.6875F), (double)((float)bedLocation.getZ() + f));
        }
        else
        {
            player.setPosition((double)((float)bedLocation.getX() + 0.5F), (double)((float)bedLocation.getY() + 0.6875F), (double)((float)bedLocation.getZ() + 0.5F));
        }

        player.sleeping = true; //sleeping private
        player.sleepTimer = 0; //sleepTimer private
        player.bedLocation = bedLocation;
        player.motionX = 0.0D;
        player.motionY = 0.0D;
        player.motionZ = 0.0D;

        if (!player.world.isRemote)
        {
            player.world.updateAllPlayersSleepingFlag();
        }

        return SleepResult.OK;
    }



}
