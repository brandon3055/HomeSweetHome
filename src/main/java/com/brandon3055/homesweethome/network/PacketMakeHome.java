package com.brandon3055.homesweethome.network;

import com.brandon3055.brandonscore.network.MessageHandlerWrapper;
import com.brandon3055.homesweethome.client.GuiMakeHome;
import com.brandon3055.homesweethome.data.PlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class PacketMakeHome implements IMessage {

    private boolean confirm;

    public PacketMakeHome() {
    }

    public PacketMakeHome(boolean confirm) {
        this.confirm = confirm;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(confirm);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        confirm = buf.readBoolean();
    }

    public static class Handler extends MessageHandlerWrapper<PacketMakeHome, IMessage> {

        @Override
        public IMessage handleMessage(PacketMakeHome message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                PlayerData data = PlayerData.getPlayerData(ctx.getServerHandler().player);
                if (data != null && message.confirm){
                    data.tryMakeHomePermanent(ctx.getServerHandler().player);
                }
            }
            else {
                openGui();
            }

            return null;
        }

        @SideOnly(Side.CLIENT)
        private void openGui() {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMakeHome());
        }
    }
}
