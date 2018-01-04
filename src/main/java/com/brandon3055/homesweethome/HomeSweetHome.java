package com.brandon3055.homesweethome;

import com.brandon3055.homesweethome.command.CommandHSH;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import static com.brandon3055.homesweethome.HomeSweetHome.*;

/**
 * Created by brandon3055 on 2/01/2018.
 */
@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION, dependencies = DEPENDENCIES)
public class HomeSweetHome {

    public static final String MOD_ID = "homesweethome";
    public static final String MOD_NAME = "Home Sweet Home";
    public static final String VERSION = "${mod_version}";
    public static final String DEPENDENCIES = "";
    public static final String PROXY_CLIENT = "com.brandon3055.homesweethome.client.ClientProxy";
    public static final String PROXY_SERVER = "com.brandon3055.homesweethome.CommonProxy";
    public static final String HOME_SWEET_HOME_NET = "HomeSHomeNet";
    public static SimpleNetworkWrapper network;

    @Mod.Instance(HomeSweetHome.MOD_ID)
    public static HomeSweetHome instance;

    @SidedProxy(clientSide = HomeSweetHome.PROXY_CLIENT, serverSide = HomeSweetHome.PROXY_SERVER)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHSH());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

}
