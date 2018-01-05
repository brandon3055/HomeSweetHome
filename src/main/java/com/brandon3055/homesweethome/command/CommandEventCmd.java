package com.brandon3055.homesweethome.command;

import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.homesweethome.helpers.EventCommandHelper;
import com.brandon3055.homesweethome.helpers.EventCommandHelper.CommandArg.Type;
import com.brandon3055.homesweethome.helpers.EventCommandHelper.EventHandler;
import com.brandon3055.homesweethome.helpers.HSHEventHelper.Event;
import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.util.text.TextFormatting.*;
import static net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND;

/**
 * Created by brandon3055 on 5/01/2018.
 */
public class CommandEventCmd extends CommandBase {
    @Override
    public String getName() {
        return "event_command";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /home_sweet_home event_command help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 3 && args[0].equals("add")) {
            Event event = Event.fromArg(args[1]);
            String command = buildString(args, 2);
            LogHelper.dev("Adding command for event " + event + ": " + command);
            EventCommandHelper.addHandler(event, command);
            ChatHelper.message(sender, "The specified command has been added and will be fired on the " + args[1] + " event.", GREEN);
        }
        else if (args.length >= 3 && args[0].equals("remove")) {
            Event event = Event.fromArg(args[1]);
            String command = buildString(args, 2);
            LogHelper.dev("Removing command for event " + event + ": " + command);
            EventCommandHelper.removeHandler(event, command);
            ChatHelper.message(sender, "The specified event command has been removed!", GREEN);
        }
        else if (args.length == 1 && args[0].equals("list")) {
            list(sender);
        }
        else {
            help(sender);
        }
    }

    private void list(ICommandSender sender) {
        ChatHelper.message(sender, "############# Configured event commands #############", AQUA);
        for (EventHandler handler : EventCommandHelper.eventHandlers) {
            sendCommandInfo(sender, handler);
        }
    }

    private void sendCommandInfo(ICommandSender sender, EventHandler handler) {
        ITextComponent msg = ChatBuilder.buildComponent(BLUE + "Event: " + GOLD + handler.getEvent().asArg() + ",\n" + DARK_AQUA + "Command: " + GOLD + handler.serializeCommand());
        ITextComponent remove = ChatBuilder.buildComponent(RED + " [Remove]", GRAY + "Click for command to remove this event command", SUGGEST_COMMAND, "/home_sweet_home event_command remove " + handler.getEvent().asArg() + " " + handler.serializeCommand());
        msg.appendSibling(remove);
        sender.sendMessage(msg);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "add", "remove", "help");
        }
        else if (args.length == 2 && args[0].equals("add")) {
            return getListOfStringsMatchingLastWord(args, DataUtils.arrayToLowercase(arrayToString(Event.values())));
        }
        else if (args.length > 2 && args[0].equals("add")) {
            return getListOfStringsMatchingLastWord(args, Type.patterns);
        }
        else if (args.length == 2 && args[0].equals("remove")) {
            return getListOfStringsMatchingLastWord(args, DataUtils.arrayToLowercase(arrayToString(Event.values())));
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    private void help(ICommandSender sender) {
        ChatHelper.message(sender, "######### Home Sweet Home Event fired Commands #########", AQUA);
        ChatHelper.message(sender, "/home_sweet_home event_command list", BLUE);
        ChatHelper.message(sender, "-List all configured event commands with options to delete.", GRAY);

        StringBuilder builder = new StringBuilder();
        builder.append(AQUA + "The following is a list of all events than can have commands bound to them.\n");
        DataUtils.forEach(Event.values(), event -> builder.append(GOLD + event.asArg() + ", "));
        builder.replace(builder.length() - 2, builder.length(), "\n");
        builder.append(AQUA + "The following is a list of all avalible arguments.\nThese will be replaced with their respective value when the command runs.\n");
        DataUtils.forEach(Type.values(), type -> builder.append(GOLD + type.pattern + " " + GRAY).append(getTypeDescription(type)).append("\n"));
        builder.replace(builder.length() - 1, builder.length(), "");
        ITextComponent msg = ChatBuilder.buildComponent(DARK_AQUA + "/home_sweet_home event_command add <event> </command arg1 arg2 arg3 arg4 etc>", builder.toString());
        sender.sendMessage(msg);
        ChatHelper.message(sender, "-Adds a new command to be triggered when the specified event occurs. ^hover to see events and arg options^", GRAY);

        ChatHelper.message(sender, "/home_sweet_home event_command remove <event> </command arg1 arg2 arg3 arg4 etc>", BLUE);
        ChatHelper.message(sender, "-Removes the specifies event command (this can also be done via the list command)", GRAY);
        ChatHelper.message(sender, "/home_sweet_home event_command help", BLUE);
        ChatHelper.message(sender, "-Displays this help text", GRAY);
    }

    private String getTypeDescription(Type type) {
        switch (type) {
            case PLAYER:
                return "The player for whom this event was fired.";
            case HOMELINESS:
                return "The players homeliness level";
            case DAYS_AWAKE:
                return "The players days awake";
            case DAYS_AWAY:
                return "The players days away";
            case MINUTES_AWAKE:
                return "The players minutes awake";
            case MINUTES_AWAY:
                return "The players minutes away";
        }
        return "";
    }

    //TODO move to BC when bc update is pushed
    public static String[] arrayToString(Object[] array) {
        String[] lowercaseArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            lowercaseArray[i] = String.valueOf(array[i]);
        }
        return lowercaseArray;
    }
}
