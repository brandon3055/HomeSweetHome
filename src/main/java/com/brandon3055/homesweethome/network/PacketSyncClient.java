package com.brandon3055.homesweethome.network;

import com.brandon3055.brandonscore.network.MessageHandlerWrapper;
import com.brandon3055.homesweethome.HomeSweetHome;
import com.brandon3055.homesweethome.client.ClientEventHandler;
import com.brandon3055.homesweethome.data.PlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class PacketSyncClient implements IMessage {

    private boolean request;
    private PlayerData data;

    public PacketSyncClient() {
        request = true;
    }

    public PacketSyncClient(PlayerData data) {
        this.data = data;
        request = false;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(request);
        if (request) return;
        ByteBufUtils.writeTag(buf, data.saveToNBT(new NBTTagCompound()));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) return;
        data = new PlayerData(null).loadFromNBT(ByteBufUtils.readTag(buf));
    }

    public static void requestUpdateClientSide() {
        HomeSweetHome.network.sendToServer(new PacketSyncClient());
    }

    public static void requestUpdateServerSide(EntityPlayerMP player) {
        PlayerData data = PlayerData.getPlayerData(player);
        if (data != null){
            HomeSweetHome.network.sendTo(new PacketSyncClient(data), player);
        }
    }

    public static class Handler extends MessageHandlerWrapper<PacketSyncClient, IMessage> {

        @Override
        public IMessage handleMessage(PacketSyncClient message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                requestUpdateServerSide(ctx.getServerHandler().player);
            }
            else {
                ClientEventHandler.data = message.data;
            }
            return null;
        }
    }
}
