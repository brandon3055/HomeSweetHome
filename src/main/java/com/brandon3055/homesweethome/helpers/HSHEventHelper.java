package com.brandon3055.homesweethome.helpers;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.helpers.EventCommandHelper.EventHandler;
import com.brandon3055.homesweethome.util.LogHelper;
import com.google.common.collect.ImmutableSet;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by brandon3055 on 5/01/2018.
 */
public class HSHEventHelper {


    public static void fireEvent(Event event, EntityPlayerMP player, PlayerData data) {
        LogHelper.dev("Event Fired: " + event);
        LogHelper.dev(EventCommandHelper.eventHandlerMap);
        List<EventHandler> handlers = EventCommandHelper.eventHandlerMap.get(event);
        if (handlers != null) {
            handlers.forEach(handler -> handler.runCommand(player, data));
        }

    }

    public enum Event {
        BECOME_HOMESICK,
        END_HOMESICK,
        BECOME_TIRED,
        END_TIRED,
        FIRST_HOME,
        MOVE_HOME,
        MAKE_PERM_HOME,
        SLEEP,
        SLEEP_HOME,
        SLEEP_AWAY,
        SLEEP_NON_PERM_HOME,
        SLEEP_HOME_EFFECT,
        SLEEP_NO_PERM_HOME_EFFECT,
        SLEEP_AWAY_EFFECT,
        ENTER_HOME,
        LEAVE_HOME,
        ENTER_PERM_HOME,
        LEAVE_PERM_HOME,
        ENTER_NON_PERM_HOME,
        LEAVE_NON_PERM_HOME,
        HOME_EFFECT_APPLIED,
        TIRED_EFFECT_APPLIED,
        HOMESICK_EFFECT_APPLIED;

        public static final Set<String> args;
        static {
            Set<String> args_set = new HashSet<>();
            DataUtils.forEach(values(), Event -> args_set.add(Event.name().toLowerCase()));
            args = ImmutableSet.copyOf(args_set);
        }

        public static Event fromArg(String arg) throws CommandException {
            if (args.contains(arg.toLowerCase())) {
                return Event.valueOf(arg.toUpperCase());
            }
            throw new CommandException("The specified event does not exist");
        }

        public String asArg() {
            return name().toLowerCase();
        }
    }

}
