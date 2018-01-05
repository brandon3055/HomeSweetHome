package com.brandon3055.homesweethome;

import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.helpers.PlayerTickHelper;
import com.brandon3055.homesweethome.helpers.SleepHelper;
import com.brandon3055.homesweethome.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 11/11/2016.
 */
public class ModEventHandler {

    public static Map<String, PlayerTickHelper> playerTickHandlerMap = new HashMap<>();


    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.END) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null){
                SleepHelper.updateSleepingPlayers(server);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerSleep(PlayerSleepInBedEvent event) {
        EntityPlayerMP player = event.getEntityPlayer() instanceof EntityPlayerMP ? (EntityPlayerMP) event.getEntityPlayer() : null;
        if (player == null) {
            return;
        }

        SleepHelper.playerTrySleep(player, event.getPos());
        event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerWakeUp(PlayerWakeUpEvent event) {
        SleepHelper.onPlayerWakeUp(event.getEntityPlayer());
    }

//Old event ^new stuff
//    @SubscribeEvent(priority = EventPriority.LOW)
//    public void playerWakeUp(PlayerWakeUpEvent event) {
//        EntityPlayer player = event.getEntityPlayer();
//        if (player.world.isRemote || !(player instanceof EntityPlayerMP)) {
//            return;
//        }
//
//        long time = player.world.getWorldTime() % 24000L;
//        if (time >= 0 && time <= 6000) {
//            SleepHelper.onPlayerWakeUp((EntityPlayerMP) player);
//        }
//    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerSetSpawn(PlayerSetSpawnEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!event.isForced()) {
//            if (wakingPlayers.contains(player.getName())) {
//                HomeManager.onPlayerWakeUp(player);
//                wakingPlayers.remove(player.getName());
//            }
            if (SleepHelper.shouldCancelSetSpawn(event.getEntityPlayer())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == Phase.START) {
            SleepHelper.updateSleepState(event.player);
            return;
        }

        EntityPlayer player = event.player;
        if (player instanceof EntityPlayerMP) {
            PlayerTickHelper tickHandler = playerTickHandlerMap.get(player.getName());
            if (tickHandler == null) {
                PlayerData data = PlayerData.getPlayerData(player);
                if (data == null) return;
                playerTickHandlerMap.put(player.getName(), (tickHandler = new PlayerTickHelper(player.getName(), (EntityPlayerMP) player, data)));
            }
            tickHandler.update((EntityPlayerMP) player);
        }
    }

    @SubscribeEvent
    public void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EntityPlayer player = event.player;
        if (player instanceof EntityPlayerMP) {
            PlayerTickHelper tickHandler = playerTickHandlerMap.get(player.getName());
            if (tickHandler != null) {
                tickHandler.clearEffects((EntityPlayerMP) player);
                playerTickHandlerMap.remove(player.getName());
            }
        }
        SleepHelper.playersAsleep.remove(player.getName());
        SleepHelper.playersVoted.remove(player.getName());
    }

    @SubscribeEvent
    public void disconnectEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ModConfig.disconnectFromServer();
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP && event.player.getServer() != null && event.player.getServer().isDedicatedServer()) {
            PacketDispatcher.sendConfigToClient((EntityPlayerMP) event.player);
        }
    }
}
