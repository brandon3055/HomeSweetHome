package com.brandon3055.homesweethome;

import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.effects.EffectHelper;
import com.brandon3055.homesweethome.network.PacketMakeHome;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class SleepHandler {

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
            }
        }
        else { //If player has no home
            data.setTimeSinceSleep(ModConfig.resetSleepOutside);

            PlayerHome home = data.getHome();
            if (home == null || !home.isPlayerInHome(player)) {
                home = data.setHome(new Vec3d(player.posX, player.posY, player.posZ));
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
}
