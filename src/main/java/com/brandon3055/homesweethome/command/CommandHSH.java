package com.brandon3055.homesweethome.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 3/01/2018.
 */
public class CommandHSH extends CommandTreeBase {

    public CommandHSH() {
        addSubcommand(new CommandConfig());
        addSubcommand(new CommandEffect());
        addSubcommand(new CommandPlayer());
    }

    @Override
    public String getName() {
        return "home_sweet_home";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/home_sweet_home <sub command> (hint use tab completion)";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
