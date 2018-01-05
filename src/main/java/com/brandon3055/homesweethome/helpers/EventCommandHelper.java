package com.brandon3055.homesweethome.helpers;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.helpers.HSHEventHelper.Event;
import com.brandon3055.homesweethome.util.LogHelper;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by brandon3055 on 5/01/2018.
 */
public class EventCommandHelper {

    public static List<EventHandler> eventHandlers = new LinkedList<>();
    public static Map<Event, List<EventHandler>> eventHandlerMap = new HashMap<>();

    public static void addHandler(Event event, String rawCommand) throws CommandException {
        EventHandler handler = new EventHandler(event);
        handler.parseCommand(rawCommand);
        if (eventHandlers.contains(handler)) {
            throw new CommandException("That event command already exists!");
        }
        eventHandlers.add(handler);
        rebuildEventHandlerMap();
        ModConfig.saveConfig();
    }

    public static void removeHandler(Event event, String rawCommand) throws CommandException {
        EventHandler handler = new EventHandler(event);
        handler.parseCommand(rawCommand);
        if (!eventHandlers.contains(handler)) {
            throw new CommandException("The specified event command does not exist!");
        }
        eventHandlers.remove(handler);
        rebuildEventHandlerMap();
        ModConfig.saveConfig();
    }

    public static class EventHandler {
        private final Event event;
        private LinkedList<CommandArg> commandArgs = new LinkedList<>();

        public EventHandler(Event event) {
            this.event = event;
        }

        public EventHandler parseCommand(String rawCommand) {
            String[] args = rawCommand.split(" ");
            DataUtils.forEach(args, arg -> commandArgs.add(CommandArg.parsePart(arg)));
            return this;
        }

        public String serializeCommand() {
            StringBuilder builder = new StringBuilder();
            commandArgs.forEach(arg -> builder.append(arg.toString()).append(" "));
            return builder.toString().trim();
        }

        public String buildCommand(EntityPlayerMP player, PlayerData data) {
            StringBuilder builder = new StringBuilder();
            commandArgs.forEach(arg -> builder.append(arg.getAsArg(player, data)).append(" "));
            return builder.toString().trim();
        }

        public void runCommand(EntityPlayerMP player, PlayerData data) {
            MinecraftServer server = player.mcServer;
            if (server != null) {
                HSHSender.runCommand(server, buildCommand(player, data));
            }
        }

        @Override
        public String toString() {
            return String.format("EventCommandHandler: [event: %s, command: %s]", event.name(), serializeCommand());
        }

        public Event getEvent() {
            return event;
        }

        public void toJson(JsonObject obj) {
            obj.addProperty("event", event.name());
            obj.addProperty("command", serializeCommand());
        }

        public static EventHandler fromJson(JsonObject obj) {
            Event event = Event.valueOf(obj.get("event").getAsString());
            String rawCommand = obj.get("command").getAsString();
            return new EventHandler(event).parseCommand(rawCommand);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof EventHandler && obj.hashCode() == hashCode();

        }

        @Override
        public int hashCode() {
            return Objects.hash(commandArgs, event.ordinal());
        }
    }

    public static class CommandArg {
        private Type type = null;
        private String stringValue = "";

        public String getAsArg(EntityPlayerMP player, PlayerData data) {
            return type == null ? stringValue : type.getAsArg(player, data);
        }

        @Override
        public String toString() {
            return type == null ? stringValue : type.pattern;
        }

        public static CommandArg parsePart(String input) {
            CommandArg part = new CommandArg();

            for (Type type : Type.values()) {
                if (type.isMatch(input)) {
                    part.type = type;
                    return part;
                }
            }

            part.stringValue = input;
            return part;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof CommandArg) {
                CommandArg other = (CommandArg) obj;
                if (type != null && other.type != null && type == other.type) {
                    return other.stringValue.equals(stringValue);
                }
                return other.type == null && type == null && other.stringValue.equals(stringValue);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type == null ? null : type.ordinal(), stringValue);
        }

        public enum Type {
            PLAYER("<player>", (player, data) -> player.getName()),
            HOMELINESS("<homeliness>", (player, data) -> data.hasHome() ? data.getHome().homeliness.getLevel() : 0),
            DAYS_AWAKE("<days_awake>", (player, data) -> (int) data.getDaysAwakeRounded()),
            DAYS_AWAY("<days_away>", (player, data) -> (int) data.getDaysAwayRounded()),
            MINUTES_AWAKE("<minutes_awake>", (player, data) -> (int) data.getTimeAwake()),
            MINUTES_AWAY("<minutes_away>", (player, data) -> (int) data.getTimeAway());

            public final String pattern;
            private final PartProvider provider;

            Type(String pattern, PartProvider provider) {
                this.pattern = pattern;
                this.provider = provider;
            }

            public static final Set<String> patterns;

            static {
                Set<String> pat_set = new HashSet<>();
                DataUtils.forEach(values(), type -> pat_set.add(type.pattern));
                patterns = ImmutableSet.copyOf(pat_set);
            }

            public boolean isMatch(String input) {
                return input.toLowerCase().equals(pattern);
            }

            public String getAsArg(EntityPlayerMP player, PlayerData data) {
                return String.valueOf(provider.getArg(player, data));
            }
        }

        private interface PartProvider {
            Object getArg(EntityPlayerMP player, PlayerData data);
        }
    }

    public static class HSHSender implements ICommandSender {
        private World world;
        private MinecraftServer server;
        public static HSHSender instance = new HSHSender();

        @Override
        public String getName() {
            return "HomeSweetHome";
        }

        @Override
        public boolean canUseCommand(int permLevel, String commandName) {
            return true;
        }

        @Override
        public World getEntityWorld() {
            return world;
        }

        @Nullable
        @Override
        public MinecraftServer getServer() {
            return server;
        }

        public static void runCommand(MinecraftServer server, String command) {
            instance.server = server;
            instance.world = server.getEntityWorld();
            server.getCommandManager().executeCommand(instance, command);
        }
    }

    public static void writeCommandsToConfig(JsonObject obj) {
        JsonArray array = new JsonArray();
        for (EventHandler handler : eventHandlers) {
            try {
                JsonObject handlerData = new JsonObject();
                handler.toJson(handlerData);
                array.add(handlerData);
            }
            catch (Throwable e) {
                LogHelper.info("Detected an error writing event command to config! Command: " + handler);
                e.printStackTrace();
            }
        }

        obj.add("event_commands", array);
    }

    public static void readCommandsFromConfig(JsonObject obj) {
        if (!obj.has("event_commands")) {
            return;
        }

        JsonArray array = obj.getAsJsonArray("event_commands");
        for (JsonElement element : array) {
            try {
                EventHandler handler = EventHandler.fromJson(element.getAsJsonObject());
                eventHandlers.add(handler);
            }
            catch (Throwable e) {
                LogHelper.info("Detected an error while event command from config! " + element);
                e.printStackTrace();
            }
        }

        rebuildEventHandlerMap();
    }

    public static void rebuildEventHandlerMap() {
        eventHandlerMap.clear();
        eventHandlers.forEach(handler -> eventHandlerMap.computeIfAbsent(handler.event, event -> new ArrayList<>()).add(handler));
    }
}
