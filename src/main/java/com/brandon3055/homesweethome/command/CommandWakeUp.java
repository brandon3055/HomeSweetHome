package com.brandon3055.homesweethome.command;

import com.brandon3055.homesweethome.helpers.SleepHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Created by brandon3055 on 6/01/2018.
 */
public class CommandWakeUp extends CommandBase {
    @Override
    public String getName() {
        return "wakeup";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /home_sweet_home wakeup";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);

        if (player.isPlayerFullyAsleep() && SleepHelper.playersAsleep.contains(player.getName())) {
            SleepHelper.onPlayerCompleteSleep(player);
        }

        SleepHelper.wakePlayer(player, false, false, false);
    }
}
