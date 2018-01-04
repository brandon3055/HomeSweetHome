package com.brandon3055.homesweethome.command;

import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.ModConfig.ConfigProperty;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.util.text.TextFormatting.*;
import static net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND;
import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;

/**
 * Created by brandon3055 on 3/01/2018.
 */
public class CommandConfig extends CommandBase {
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /home_sweet_home config help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return super.getRequiredPermissionLevel();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1 && args[0].equals("list")) {
            list(sender);
        }
        else if (args.length >= 2 && args[0].equals("get")) {
            ConfigProperty prop = ModConfig.propertyMap.get(args[1]);
            if (prop != null) {
                printProp(sender, prop);
            }
            else {
                throw new CommandException("The specified property does not exist!");
            }
        }
        else if (args.length >= 3 && args[0].equals("set")) {
            ConfigProperty prop = ModConfig.propertyMap.get(args[1]);
            if (prop != null) {
                prop.setValue(args[2]);
                ChatHelper.message(sender, GREEN + "Property " + args[1] + " changed to " + args[2]);
            }
            else {
                throw new CommandException("The specified property does not exist!");
            }
        }
        else {
            help(sender);
        }
    }

    private void list(ICommandSender sender) {
        ChatHelper.message(sender, "");
        for (ConfigProperty prop : ModConfig.properties) {
            if (prop.isInfo()) {
                ChatHelper.message(sender, DARK_PURPLE + prop.getInfo());
            }
            else {
                printProp(sender, prop);
            }
        }
        ChatHelper.message(sender, BLUE + "Mouse over a property for more info.");
    }

    private void printProp(ICommandSender sender, ConfigProperty prop) {
        TextComponentString msg = new TextComponentString(DARK_AQUA + "" + prop.getName() + ": " + RESET + "" + GOLD + prop.getValue());
        Style style = new Style();
        style.setHoverEvent(new HoverEvent(SHOW_TEXT, new TextComponentString(GOLD + prop.getName() + "\n" + TextFormatting.BLUE + prop.getInfo() + GRAY + "\nClick to set value")));
        style.setClickEvent(new ClickEvent(SUGGEST_COMMAND, "/home_sweet_home config set " + prop.getName() + " "));
        msg.setStyle(style);
        sender.sendMessage(msg);
    }

    private void help(ICommandSender sender) {
        ChatHelper.message(sender, "############# Home Sweet Home Properties #############", AQUA);
        ChatHelper.message(sender, "/home_sweet_home config list", BLUE);
        ChatHelper.message(sender, "-List all configurable properties.", GRAY);
        ChatHelper.message(sender, "/home_sweet_home config set <property> <value>", BLUE);
        ChatHelper.message(sender, "-Sets the specified property to the specified value.", GRAY);
        ChatHelper.message(sender, "/home_sweet_home config get <property>", BLUE);
        ChatHelper.message(sender, "-Displays the value for the specified property", GRAY);
        ChatHelper.message(sender, "/home_sweet_home config help", BLUE);
        ChatHelper.message(sender, "-Displays this help text", GRAY);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "set", "get", "help");
        }
        else if (args.length == 2 && (args[0].equals("get") || args[0].equals("set"))) {
            return getListOfStringsMatchingLastWord(args, ModConfig.propertyMap.keySet());
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
