package com.brandon3055.homesweethome;

import com.brandon3055.homesweethome.data.DataHandler;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

/**
 * Created by brandon3055 on 2/9/2018.
 */
public class ChunkLoadingHandler implements ForgeChunkManager.OrderedLoadingCallback, ForgeChunkManager.PlayerOrderedLoadingCallback {

    public static Map<String, List<Ticket>> userTicketList = new HashMap<>();

    public static void updateAllTickets() {
        MinecraftServer server;
        if ((server = FMLCommonHandler.instance().getMinecraftServerInstance()) == null) {
            return;
        }
        userTicketList.values().forEach(tickets -> tickets.forEach(ForgeChunkManager::releaseTicket));
        userTicketList.clear();

        if (!ModConfig.enableChunkLoading) {
            return;
        }

        DataHandler dataHandler = DataHandler.getDataInstance(server.getEntityWorld());
        if (dataHandler == null) {
            return;
        }

        dataHandler.getPlayerDataMap().forEach((user, data) -> {
            if (data.hasHome()) {
                reloadPlayerChunks(data.getUsername(), data.getHome(), server);
            }
        });
    }

    public static void reloadPlayerChunks(String username, PlayerHome home, MinecraftServer server) {
        if (userTicketList.containsKey(username)) {
            List<Ticket> get = userTicketList.get(username);
            //This avoids CCM exceptions when a player has a crazy number of loaded chunks.
            for (int i = 0; i < get.size(); i++) {
                Ticket ticket = get.get(i);
                ForgeChunkManager.releaseTicket(ticket);
            }
            userTicketList.remove(username);
        }

        if (ModConfig.offlineLoading || server.getPlayerList().getPlayerByUsername(username) != null) {
            List<ChunkPos> chunks = home.getLoadingChunks();
            World world = server.getWorld(home.getDimension());
            if (chunks.isEmpty() || world == null) {
                return;
            }

            Ticket ticket = ForgeChunkManager.requestPlayerTicket(HomeSweetHome.instance, username, world, ForgeChunkManager.Type.NORMAL);
            if (ticket == null) {
                return;
            }
            userTicketList.computeIfAbsent(username, s -> new LinkedList<>()).add(ticket);

            int loaded = 0;
            for (ChunkPos pos : chunks) {
                if (loaded == ticket.getMaxChunkListDepth()) {
                    ticket = ForgeChunkManager.requestPlayerTicket(HomeSweetHome.instance, username, world, ForgeChunkManager.Type.NORMAL);
                    if (ticket == null) {
                        return;
                    }
                    userTicketList.get(username).add(ticket);
                    loaded = 0;
                }

                ForgeChunkManager.forceChunk(ticket, pos);
                loaded++;
            }
        }
    }

    public static void playerLoggedOut(String username) {
        if (!ModConfig.offlineLoading && userTicketList.containsKey(username)) {
            userTicketList.get(username).forEach(ForgeChunkManager::releaseTicket);
            userTicketList.remove(username);
        }
    }


    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {}

    @Override
    public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
        return new LinkedList<>();
    }

    @Override
    public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, World world) {
        return LinkedListMultimap.create();
    }
}
