package com.brandon3055.homesweethome;

import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.helpers.PlayerTickHelper;
import com.brandon3055.homesweethome.helpers.SleepHelper;
import com.brandon3055.homesweethome.network.PacketDispatcher;
import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 11/11/2016.
 */
public class ModEventHandler {

    public static Map<String, PlayerTickHelper> playerTickHandlerMap = new HashMap<>();

//    private static List<String> wakingPlayers = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerSleep(PlayerSleepInBedEvent event) {
        EntityPlayerMP player = event.getEntityPlayer() instanceof EntityPlayerMP ? (EntityPlayerMP) event.getEntityPlayer() : null;
        if (player == null) {
            return;
        }

        LogHelper.dev("Sleep");

//        SleepResult result = SleepHelper.trySleep(player, event.getPos());
//        if (result == EntityPlayer.SleepResult.OK)
//        {
//            player.addStat(StatList.SLEEP_IN_BED);
//            Packet<?> packet = new SPacketUseBed(player, event.getPos());
//            player.getServerWorld().getEntityTracker().sendToTracking(player, packet);
//            player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
//            player.connection.sendPacket(packet);
//            CriteriaTriggers.SLEPT_IN_BED.trigger(player);
//        }
//
//        if (result != EntityPlayer.SleepResult.OK)
//        {
//            if (result == EntityPlayer.SleepResult.NOT_POSSIBLE_NOW)
//            {
//                player.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep"), true);
//            }
//            else if (result == EntityPlayer.SleepResult.NOT_SAFE)
//            {
//                player.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe"), true);
//            }
//            else if (result == EntityPlayer.SleepResult.TOO_FAR_AWAY)
//            {
//                player.sendStatusMessage(new TextComponentTranslation("tile.bed.tooFarAway"), true);
//            }
//        }

//        event.setResult(SleepResult.OTHER_PROBLEM);

//        player.spawnShoulderEntities(); //Protected
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerWakeUp(PlayerWakeUpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }

        long time = player.world.getWorldTime() % 24000L;
        if (time >= 0 && time <= 6000) {
            SleepHelper.onPlayerWakeUp((EntityPlayerMP) player);
//            wakingPlayers.add(player.getName());
        }
    }

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
        if (event.phase != TickEvent.Phase.END) return;
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
