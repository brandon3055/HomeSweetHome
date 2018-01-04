package com.brandon3055.homesweethome.network;

import com.brandon3055.brandonscore.network.MessageHandlerWrapper;
import com.brandon3055.homesweethome.data.PlayerHome;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class PacketSendConfig implements IMessage {

    private double homeSickness;
    private double tiredness;
    private boolean hasHome;
    private int homeX;
    private int homeY;
    private int homeZ;
    private double levelWithProgress;

    public PacketSendConfig() {
    }

    public PacketSendConfig(double homeSickness, double tiredness, @Nullable PlayerHome home) {
        this.homeSickness = homeSickness;
        this.tiredness = tiredness;
        this.hasHome = home != null;
        if (home != null) {
            homeX = (int) home.getPos().x;
            homeY = (int) home.getPos().y;
            homeZ = (int) home.getPos().z;
            levelWithProgress = home.homeliness.getLevelWithProgress();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    public static class Handler extends MessageHandlerWrapper<PacketSendConfig, IMessage> {

        @Override
        public IMessage handleMessage(PacketSendConfig message, MessageContext ctx) {


            return null;
        }
    }
}
