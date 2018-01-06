package com.brandon3055.homesweethome.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.homesweethome.helpers.SleepHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.text.TextFormatting;

public class ServerPacketHandler implements ICustomPacketHandler.IServerPacketHandler {
    @Override
    public void handlePacket(PacketCustom packet, EntityPlayerMP player, INetHandlerPlayServer handler) {
        if (packet.getType() == PacketDispatcher.S_WAKE_UP) {
            wakeUpPlayer(player, packet.readBoolean());
        }

    }

    private void wakeUpPlayer(EntityPlayerMP player, boolean slept) {
        if (!slept) {
            SleepHelper.wakePlayer(player, true, true, false);
        }
        else {
            if (player.isPlayerFullyAsleep() && SleepHelper.playersAsleep.contains(player.getName())) {
                SleepHelper.onPlayerCompleteSleep(player);
                SleepHelper.wakePlayer(player, true, true, false);
            }
            else {
                ChatHelper.translate(player, "hsh.msg.sleep.errorNotReadyToWakeUp", TextFormatting.RED);
            }
        }
    }
}