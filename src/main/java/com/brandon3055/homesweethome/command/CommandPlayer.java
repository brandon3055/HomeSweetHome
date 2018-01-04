package com.brandon3055.homesweethome.command;

import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.homesweethome.data.DataHandler;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * Created by brandon3055 on 4/01/2018.
 */
public class CommandPlayer extends CommandBase {
    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage /home_sweet_home player help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
        DataHandler dataHandler = DataHandler.getDataInstance(player.world);
        if (dataHandler == null) {
            throw new CommandException("Error! world data instance is null!");
        }
        PlayerData data = PlayerData.getPlayerData(player);
        if (data == null) {
            throw new CommandException("Error! player data instance was null!");
        }

        if (args.length >= 1 && args[0].equals("reset")) {
            dataHandler.clearPlayerData(player);
            ChatHelper.message(player, "Your Home Sweet Home data has been reset.", RED);
            if (sender != player) {
                ChatHelper.message(player, player.getName() + "'s Home Sweet Home data has been reset.", RED);
            }
        }
        else if (args.length >= 1 && args[0].equals("clear_negative")) {
            data.setTimeAway(0);
            data.setTimeSinceSleep(0);
            ChatHelper.message(player, "Your time away and time since sleep have been reset to 0", GREEN);
            if (sender != player) {
                ChatHelper.message(player, player.getName() + "'s time away and time since sleep have been reset to 0", GREEN);
            }
        }
        else if (args.length >= 3 && args[0].equals("set_homeliness_level")) {
            PlayerHome home = data.getHome();
            if (home == null) {
                throw new CommandException("Error! the player needs to sleep at least one to set their home before their home level can be modified");
            }

            int level = parseInt(args[2], 0, 1000000);
            home.homeliness.addLevel(level - home.homeliness.getLevel());

            ChatHelper.message(player, "Your homeliness level has been updates to " + level, GREEN);
            if (sender != player) {
                ChatHelper.message(player, player.getName() + "'s homeliness level has been updates to " + level, GREEN);
            }
        }
        else {
            help(sender);
        }
    }

    private void help(ICommandSender sender) {
        ChatHelper.message(sender, "############# Home Sweet Home Player #############", AQUA);
        ChatHelper.message(sender, "/home_sweet_home player reset [player]", BLUE);
        ChatHelper.message(sender, "-Removes the home and all stats for the specified player.", GRAY);
        ChatHelper.message(sender, "/home_sweet_home player set_homeliness_level <player> <level>", BLUE);
        ChatHelper.message(sender, "-Sets the homeliness level for the specified player", GRAY);
        ChatHelper.message(sender, "/home_sweet_home player clear_negative [player]", BLUE);
        ChatHelper.message(sender, "-Resets the players time away from home and time awake to 0", GRAY);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reset", "set_homeliness_level", "clear_negative");
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
