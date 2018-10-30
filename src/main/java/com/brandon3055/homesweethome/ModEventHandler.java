package com.brandon3055.homesweethome;

import com.brandon3055.brandonscore.lib.TeleportUtils;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.helpers.PlayerTickHelper;
import com.brandon3055.homesweethome.helpers.SleepHelper;
import com.brandon3055.homesweethome.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void disconnectEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ModConfig.disconnectFromServer();
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP && event.player.getServer() != null) {
            if (event.player.getServer().isDedicatedServer()) {
                PacketDispatcher.sendConfigToClient((EntityPlayerMP) event.player);
            }
            PlayerData data = PlayerData.getPlayerData(event.player);
            if (data != null && data.hasHome()){
                ChunkLoadingHandler.reloadPlayerChunks(data.getUsername(), data.getHome(), event.player.getServer());
            }
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
            ChunkLoadingHandler.playerLoggedOut(player.getName());
        }
        SleepHelper.playersAsleep.remove(player.getName());
        SleepHelper.playersVoted.remove(player.getName());
    }

    @SubscribeEvent
    public void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        PlayerData data = PlayerData.getPlayerData(player);

        if (!(player instanceof EntityPlayerMP) || data == null || !data.hasHome()) {
            return;
        }

        PlayerHome home = data.getHome();
        BlockPos pos = home.getSpawn();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        World world = server.getWorld(home.getDimension());

        List<BlockPos> validBlocks = new ArrayList<>();
        Iterable<BlockPos> candidates = BlockPos.getAllInBox(pos.add(-8, -6, -8), pos.add(8, 6, 8));
        DataUtils.forEachMatch(candidates, candidate -> isValidSpawn(candidate, world), validBlocks::add);

        if (validBlocks.size() == 0) {
            player.sendMessage(new TextComponentTranslation("hsh.msg.sleep.noSafePlaceToSpawn").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        BlockPos closest = validBlocks.get(0);
        double dist = Integer.MAX_VALUE;
        for (BlockPos candidate : validBlocks) {
            double d = candidate.distanceSq(pos);
            if (d < dist) {
                closest = candidate;
                dist = d;
            }
        }

        TeleportUtils.teleportEntity(player, home.getDimension(), closest.getX() + 0.5, closest.getY() + 0.2, closest.getZ() + 0.5);
    }

    @SubscribeEvent
    public void worldLoaded(WorldEvent.Load event) {
        if (event.getWorld().provider.getDimension() == 0) {
            ChunkLoadingHandler.updateAllTickets();
        }
    }

    public void putPlayerInSafePlace(EntityPlayer player, World targetWorld, BlockPos targetPos, int dimension, int horizontalRange, int verticalRange) {
        List<BlockPos> validBlocks = new ArrayList<>();
        Iterable<BlockPos> candidates = BlockPos.getAllInBox(targetPos.add(-horizontalRange, -verticalRange, -horizontalRange), targetPos.add(horizontalRange, verticalRange, horizontalRange));
        DataUtils.forEachMatch(candidates, candidate -> isValidSpawn(candidate, targetWorld), validBlocks::add);

        if (validBlocks.size() == 0) {
            player.sendMessage(new TextComponentTranslation("hsh.msg.sleep.noSafePlaceToSpawn").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        BlockPos closest = validBlocks.get(0);
        double dist = Integer.MAX_VALUE;
        for (BlockPos candidate : validBlocks) {
            double d = candidate.distanceSq(targetPos);
            if (d < dist) {
                closest = candidate;
                dist = d;
            }
        }

        TeleportUtils.teleportEntity(player, dimension, closest.getX() + 0.5, closest.getY() + 0.2, closest.getZ() + 0.5);
    }

    private boolean isValidSpawn(BlockPos pos, World world) {
        return world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.isSideSolid(pos.down(), EnumFacing.UP);
    }
}
