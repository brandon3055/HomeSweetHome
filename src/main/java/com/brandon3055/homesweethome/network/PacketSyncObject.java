//package com.brandon3055.homesweethome.network;
//
//import com.brandon3055.homesweethome.util.LogHelper;
//import net.minecraft.client.Minecraft;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
//import net.minecraftforge.fml.relauncher.Side;
//
///**
// * Created by brandon3055 on 10/4/2016.
// * This is used for packet thread synchronization.
// */
//public abstract class PacketSyncObject<REQ extends IMessage, REPLY extends IMessage> implements Runnable {
//
//    public final REQ message;
//    public REPLY reply;
//
//    public final MessageContext ctx;
//
//    public PacketSyncObject(REQ message, MessageContext ctx) {
//        this.message = message;
//        this.ctx = ctx;
//    }
//
//    @Override
//    public abstract void run();
//
//    public void addPacketServer() {
//        if (ctx.side == Side.CLIENT) {
//            LogHelper.error("[SyncPacket#addPacketServer] HAY!!! I caught you this time! WRONG SIDE!!!! - " + message.getClass());
//            return;
//        }
//        ctx.getServerHandler().player.getServer().addScheduledTask(this);
//    }
//
//    public void addPacketClient() {
//        if (ctx.side == Side.SERVER) {
//            LogHelper.error("[SyncPacket#addPacketClient] HAY!!! I caught you this time! WRONG SIDE!!!! - " + message.getClass());
//            return;
//        }
//        Minecraft.getMinecraft().addScheduledTask(this);
//    }
//}