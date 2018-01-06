package com.brandon3055.homesweethome.command;

import io.netty.channel.Channel;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by brandon3055 on 3/01/2018.
 */
public class CommandHSH extends CommandTreeBase {

    public CommandHSH() {
        addSubcommand(new CommandConfig());
        addSubcommand(new CommandEffect());
        addSubcommand(new CommandPlayer());
        addSubcommand(new CommandEventCmd());
        addSubcommand(new CommandVoteDay());
        addSubcommand(new CommandWakeUp());
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
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            List<String> keys = new ArrayList<>();

            for (ICommand c : getSubCommands()) {
                if (c.checkPermission(server, sender)) {
                    if (c.getName().equals("voteday") || c.getName().equals("wakeup")) continue;
                    keys.add(c.getName());
                }
            }

            keys.sort(null);
            return getListOfStringsMatchingLastWord(args, keys);
        }

        ICommand cmd = getSubCommand(args[0]);

        if (cmd != null) {
            return cmd.getTabCompletions(server, sender, shiftArgs(args), pos);
        }

        return Collections.emptyList();
    }

    @Nullable
    public ICommand getSubCommand(String command) {
        ICommand cmd = getCommandMap().get(command);
        if (cmd != null) {
            return cmd;
        }
//        return commandAliasMap.get(command);
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            String subCommandsString = getAvailableSubCommandsString(server, sender);
            sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, "commands.tree_base.available_subcommands", subCommandsString));
        }
        else {
            ICommand cmd = getSubCommand(args[0]);

            if (cmd == null) {
                String subCommandsString = getAvailableSubCommandsString(server, sender);
                throw new CommandException("commands.tree_base.invalid_cmd.list_subcommands", args[0], subCommandsString);
            }
            else if (!cmd.checkPermission(server, sender)) {
                throw new CommandException("commands.generic.permission");
            }
            else {
                cmd.execute(server, sender, shiftArgs(args));
            }
        }
    }

    private static String[] shiftArgs(@Nullable String[] s) {
        if (s == null || s.length == 0) {
            return new String[0];
        }

        String[] s1 = new String[s.length - 1];
        System.arraycopy(s, 1, s1, 0, s1.length);
        return s1;
    }

    private String getAvailableSubCommandsString(MinecraftServer server, ICommandSender sender) {
        Collection<String> availableCommands = new ArrayList<>();
        for (ICommand command : getSubCommands()) {
            if (command.checkPermission(server, sender)) {
                if (command.getName().equals("voteday") || command.getName().equals("wakeup")) continue;
                availableCommands.add(command.getName());
            }
        }
        return CommandBase.joinNiceStringFromCollection(availableCommands);
    }

    public static class TextComponentHelper {
        private TextComponentHelper() {}

        /**
         * Detects when sending to a vanilla client and falls back to sending english,
         * since they don't have the lang data necessary to translate on the client.
         */
        public static TextComponentBase createComponentTranslation(ICommandSender sender, final String translation, final Object... args) {
            if (isVanillaClient(sender)) {
                return new TextComponentString(I18n.translateToLocalFormatted(translation, args));
            }
            return new TextComponentTranslation(translation, args);
        }

        private static boolean isVanillaClient(ICommandSender sender) {
            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) sender;
                Channel channel = playerMP.connection.netManager.channel();
                return !channel.attr(NetworkRegistry.FML_MARKER).get();
            }
            return false;
        }
    }
}
