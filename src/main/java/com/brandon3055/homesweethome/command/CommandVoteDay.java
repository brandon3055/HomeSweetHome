package com.brandon3055.homesweethome.command;

import com.brandon3055.homesweethome.helpers.SleepHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by brandon3055 on 6/01/2018.
 */
public class CommandVoteDay extends CommandBase {
    @Override
    public String getName() {
        return "voteday";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /home_sweet_home voteday";
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
        if (SleepHelper.playersAsleep.isEmpty()) {
            throw new CommandException("You can not vote to skip the night when none is sleeping!");
        }
        else if (SleepHelper.playersVoted.contains(sender.getName())) {
            throw new CommandException("You have already voted!");
        }
        SleepHelper.onPlayerVoted(getCommandSenderAsPlayer(sender));
    }
}
