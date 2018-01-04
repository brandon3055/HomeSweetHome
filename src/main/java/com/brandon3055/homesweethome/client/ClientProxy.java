package com.brandon3055.homesweethome.client;

import com.brandon3055.brandonscore.client.ProcessHandlerClient;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.homesweethome.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        ClientEventHandler.init();
    }

    @Override
    public void runSidedProcess(IProcess process) {
        ProcessHandlerClient.addProcess(process);
    }
}
