package com.brandon3055.homesweethome.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.homesweethome.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;

public class ClientPacketHandler implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        if (packet.getType() == PacketDispatcher.C_SERVER_CONFIG_SYNC) {
            ModConfig.receiveConfigFromServer(packet);
        }
    }
}