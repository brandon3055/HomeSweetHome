package com.brandon3055.homesweethome.network;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.homesweethome.ModConfig;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class PacketDispatcher {

    public static final int C_SERVER_CONFIG_SYNC = 1;

    public static void sendConfigToClient(EntityPlayerMP player) {
        PacketCustom packet = new PacketCustom("HSHPCChannel", C_SERVER_CONFIG_SYNC);
        ModConfig.writeConfigForSync(packet);
        if (player != null){
            packet.sendToPlayer(player);
        }
        else {
            packet.sendToClients();
        }
        LogHelperBC.dev("Sending Config To Client: " + (player == null ? "[all players]" : player));
    }
}
