package com.brandon3055.homesweethome;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 11/11/2016.
 */
public class ModEventHandler {

    private static Map<String, PlayerTickHandler> playerTickHandlerMap = new HashMap<>();

//    private static List<String> wakingPlayers = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerWakeUp(PlayerWakeUpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }

        long time = player.world.getWorldTime() % 24000L;
        if (time >= 0 && time <= 6000) {
            SleepHandler.onPlayerWakeUp((EntityPlayerMP) player);
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
            if (SleepHandler.shouldCancelSetSpawn(event.getEntityPlayer())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent()
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if (player instanceof EntityPlayerMP) {
            PlayerTickHandler tickHandler = playerTickHandlerMap.get(player.getName());
            if (tickHandler == null) {
                playerTickHandlerMap.put(player.getName(), (tickHandler = new PlayerTickHandler(player.getName())));
            }
            tickHandler.update((EntityPlayerMP) player);
        }
    }

    @SubscribeEvent()
    public void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EntityPlayer player = event.player;
        if (player instanceof EntityPlayerMP) {
            PlayerTickHandler tickHandler = playerTickHandlerMap.get(player.getName());
            if (tickHandler != null) {
                tickHandler.clearEffects((EntityPlayerMP) player);
            }
        }
    }

//    @SubscribeEvent()
//    public void playerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
//        EntityPlayer player = event.player;
//    }
}
