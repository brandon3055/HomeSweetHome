package com.brandon3055.homesweethome;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.homesweethome.network.PacketMakeHome;
import com.brandon3055.homesweethome.network.PacketSyncClient;
import com.brandon3055.homesweethome.network.ServerPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
        initializeNetwork();
        ModConfig.initConfig();
    }

    public void initializeNetwork() {
        HomeSweetHome.network = NetworkRegistry.INSTANCE.newSimpleChannel(HomeSweetHome.HOME_SWEET_HOME_NET);
        HomeSweetHome.network.registerMessage(PacketSyncClient.Handler.class, PacketSyncClient.class, 0, Side.CLIENT);
        HomeSweetHome.network.registerMessage(PacketSyncClient.Handler.class, PacketSyncClient.class, 1, Side.SERVER);

        HomeSweetHome.network.registerMessage(PacketMakeHome.Handler.class, PacketMakeHome.class, 2, Side.CLIENT);
        HomeSweetHome.network.registerMessage(PacketMakeHome.Handler.class, PacketMakeHome.class, 3, Side.SERVER);
    }

    public void runSidedProcess(IProcess process) {
        ProcessHandler.addProcess(process);
    }

    public void registerPacketHandlers() {
        PacketCustom.assignHandler("HSHPCChannel", new ServerPacketHandler());
    }
}
