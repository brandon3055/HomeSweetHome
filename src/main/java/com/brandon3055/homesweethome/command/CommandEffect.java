package com.brandon3055.homesweethome.command;

import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.homesweethome.effects.EffectHelper;
import com.brandon3055.homesweethome.effects.EffectHelper.EffectHandler;
import com.brandon3055.homesweethome.effects.EffectHelper.EffectTrigger;
import com.brandon3055.homesweethome.effects.EffectHelper.EffectTrigger.Source;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.util.text.TextFormatting.*;
import static net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND;
import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;

/**
 * Created by brandon3055 on 4/01/2018.
 */
public class CommandEffect extends CommandBase {
    @Override
    public String getName() {
        return "effect";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /home_sweet_home effect help";
    }

    // /home_sweet_home effect list
    // /home_sweet_home effect list_sources <effectID>
    // /home_sweet_home effect list_effects <source>
    // /home_sweet_home effect remove <effectID>
    // /home_sweet_home effect remove <effectID> <effect amplifier>
    // /home_sweet_home effect remove <effectID> <effect amplifier> <source> <trigger value>
    // /home_sweet_home effect add <effectID> <effect amplifier> <source> <trigger value>

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1 && args[0].equals("list")) {
            list(sender);
        }
        else if (args.length >= 2 && args[0].equals("list_sources")) {
            ResourceLocation effect = new ResourceLocation(args[1]);
            if (Potion.REGISTRY.getObject(effect) == null) {
                throw new CommandException("The specified effect does not exist!");
            }
            if (!EffectHelper.effectHandlerMap.containsKey(effect.toString())) {
                throw new CommandException("There are no sources for that effect!");
            }
            ChatHelper.message(sender, "############# Effect Configuration #############", AQUA);
            sendHandlerInfo(sender, EffectHelper.effectHandlerMap.get(effect.toString()), null);
        }
        else if (args.length >= 2 && args[0].equals("list_effects")) {
            Source source = Source.getSource(args[1]);
            if (!EffectHelper.sourceEffectMap.containsKey(source)) {
                throw new CommandException("There are no effects configured for this source");
            }

            ChatHelper.message(sender, "############# Effect caused by source #############", AQUA);
            EffectHelper.sourceEffectMap.get(source).forEach(handler -> handler.levelTriggerMap.forEach((level, triggers) -> {
                triggers.forEach(trigger -> {
                    if (trigger.getSource() == source) {
                        sendHandlerInfo(sender, handler, source);
                    }
                });
            }));
        }
        else if (args.length >= 2 && args[0].equals("remove")) {
            ResourceLocation effect = new ResourceLocation(args[1]);
            if (Potion.REGISTRY.getObject(effect) == null) {
                throw new CommandException("The specified effect does not exist!");
            }
            if (!EffectHelper.effectHandlerMap.containsKey(effect.toString())) {
                throw new CommandException("There are no sources for that effect!");
            }

            int amp = args.length >= 3 ? parseInt(args[2]) : 0;
            if (args.length == 2) {
                EffectHelper.effectHandlerMap.remove(effect.toString());
                EffectHelper.updateSourceEffectsMap();
                ModConfig.saveConfig();
                ChatHelper.message(sender, "All sources of this effect have been removed!", GREEN);
            }
            else if (args.length == 3) {
                EffectHandler handler = EffectHelper.effectHandlerMap.get(effect.toString());
                if (!handler.levelTriggerMap.containsKey(amp)) {
                    throw new CommandException("There are no sources for that effect and amplifier combination!");
                }
                handler.removeLevelTriggers(amp);
                ModConfig.saveConfig();
                ChatHelper.message(sender, "All sources of this effect and amplifier combination have been removed!", GREEN);
            }
            else if (args.length == 5) {
                EffectHandler handler = EffectHelper.effectHandlerMap.get(effect.toString());
                if (!handler.levelTriggerMap.containsKey(amp)) {
                    throw new CommandException("There are no sources for that effect and amplifier combination!");
                }
                Source source = Source.getSource(args[3]);
                int trigger = parseInt(args[4]);
                if (source == Source.SLEPT_NO_HOME) {
                    trigger = 0;
                }
                if (!handler.removeLevelTrigger(amp, source, trigger)) {
                    throw new CommandException("Did not find the specified effect source and trigger combination!");
                }
                ModConfig.saveConfig();
                ChatHelper.message(sender, "The specified effect source and trigger combination has been removed!", GREEN);
            }
        }
        else if (args.length >= 1 && args[0].equals("add")) {
            ResourceLocation effect;
            int amplifier;
            Source source = args.length >= 4 ? Source.getSource(args[3]) : null;
            if (args.length == 1) { //choose Effect
                ChatHelper.message(sender, "# Choose effect and effect amplifier", AQUA);
                Potion.REGISTRY.getKeys().forEach(key -> {
                    Potion p = Potion.REGISTRY.getObject(key);
                    localizedChatWithCommand(sender, p.isBadEffect() ? RED : GREEN, p.getName(), GOLD + key.toString() + GRAY + "\nClick to select this effect with a default amplifier of 0\n" + //
                            "Then change the amplifier to your desired value.", "/home_sweet_home effect add " + key + " 0", SUGGEST_COMMAND);
                });
                ChatHelper.message(sender, "# Choose effect and effect amplifier", AQUA);
            }
            else if (args.length == 2) { //choose Amplifier
                ChatHelper.message(sender, "Please specify an amplifier value", RED);
            }
            else if (args.length == 3) { //choose Source
                amplifier = parseInt(args[2], 0, 255);
                effect = new ResourceLocation(args[1]);
                ChatHelper.message(sender, "# Choose effect source and trigger value", AQUA);
                for (Source option : Source.values()) {
                    chatWithCommand(sender, (option.isPositive() ? GREEN : RED) + option.commandName(), GRAY + "Click to select this effect source.", "/home_sweet_home effect add " + effect.toString() + " " + amplifier + " " + option.commandName(), SUGGEST_COMMAND);
                }
                ChatHelper.message(sender, "# Choose effect source and trigger value", AQUA);
            }
            else if (args.length == 4) { //choose Trigger
                ChatHelper.message(sender, "Please specify a trigger value", RED);
                ChatHelper.message(sender, getSourceInfo(source), GRAY);
            }
            else if (args.length == 5 || args.length == 6) {
                effect = new ResourceLocation(args[1]);
                amplifier = parseInt(args[2], 0, 255);
                LogHelper.info(source.hasDuration());
                if (source.hasDuration() && args.length == 5) {
                    throw new CommandException("This effect requires a duration in seconds!");
                }
                else if (!source.hasDuration() && args.length == 6) {
                    throw new CommandException("This effect does not require a duration!");
                }
                int trigger = parseInt(args[4], 0, Integer.MAX_VALUE);
                if (Potion.REGISTRY.getObject(effect) == null) {
                    throw new CommandException("The specified effect does not exist!");
                }
                int duration = args.length == 6 ? parseInt(args[5], 0, 1000000) : 0;
                if (source == Source.SLEPT_NO_HOME) {
                    trigger = 0;
                }
                EffectHelper.addEffect(effect.toString(), amplifier, source, trigger, duration * 20);
                ChatHelper.message(sender, "Effect added!", GREEN);
            }
            else {
                help(sender);
            }
        }
        else {
            help(sender);
        }
    }

    private void list(ICommandSender sender) {
        ChatHelper.message(sender, "############# Configured effects #############", AQUA);
        for (EffectHandler handler : EffectHelper.effectHandlerMap.values()) {
            sendHandlerInfo(sender, handler, null);
        }
    }

    private void sendHandlerInfo(ICommandSender sender, EffectHandler handler, Source filter) {
        Potion potion = handler.getPotion();
        String name = potion == null ? "[invalid-potion-id]" : potion.getName();
        ITextComponent msg = buildComponent(GRAY + "Handler for: ", GRAY + "Effect handler for: " + GOLD + handler.effectID).appendSibling(buildComponent(GOLD, name, true));
        ITextComponent remove = buildComponent(RED + " [Remove]", GRAY + "Click for command to remove all source of this effect", SUGGEST_COMMAND, "/home_sweet_home effect remove " + handler.effectID);
        msg.appendSibling(remove);
        sender.sendMessage(msg);

        handler.levelTriggerMap.forEach((amplifier, triggers) -> {
            if (filter != null) {
                boolean valid = false;
                for (EffectTrigger trigger : triggers) {
                    if (trigger.getSource() == filter) {
                        valid = true;
                        break;
                    }
                }
                if (!valid) {
                    return;
                }
            }

            ITextComponent ampMsg = buildComponent("  Amplifier level " + amplifier + " sources");
            ITextComponent ampRemove = buildComponent(RED + " [Remove]", GRAY + "Click for command to remove all source of this effect at this amplifier level.", SUGGEST_COMMAND, "/home_sweet_home effect remove " + handler.effectID + " " + amplifier);
            sender.sendMessage(ampMsg.appendSibling(ampRemove));

            triggers.forEach(trigger -> {
                if (filter != null && filter != trigger.getSource()) {
                    return;
                }
                ITextComponent trigMsg = buildComponent(GREEN + "    Source: " + GOLD + trigger.getSource().commandName() + GREEN + ", Trigger Value: " + GOLD + " " + (int) trigger.getTriggerValue(), GRAY + getLevelInfo(trigger));
                ITextComponent trigRemove = buildComponent(RED + " [Remove]", GRAY + "Click for command to remove this source", SUGGEST_COMMAND, "/home_sweet_home effect remove " + handler.effectID + " " + amplifier + " " + trigger.getSource().commandName() + " " + (int) trigger.getTriggerValue());
                sender.sendMessage(trigMsg.appendSibling(trigRemove));
                if (trigger.getSource().hasDuration()) {
                    sender.sendMessage(buildComponent(GRAY + "    -Duration: " + trigger.getDuration() / 20 + " seconds"));
                }
            });
        });
    }

    private ITextComponent buildComponent(TextFormatting colour, String text, boolean translate, HoverEvent.Action hoverAction, String hover, Action action, String command) {
        ITextComponent comp = translate ? new TextComponentTranslation(text) : new TextComponentString(text);
        Style style = new Style();
        if (colour != null) {
            style.setColor(colour);
        }
        if (hoverAction != null) {
            style.setHoverEvent(new HoverEvent(hoverAction, new TextComponentString(hover)));
        }
        if (action != null) {
            style.setClickEvent(new ClickEvent(action, command));
        }
        comp.setStyle(style);
        return comp;
    }

    private ITextComponent buildComponent(String text, String tip, Action action, String actionArg) {
        return buildComponent(null, text, false, SHOW_TEXT, tip, action, actionArg);
    }

    private ITextComponent buildComponent(String text, String tip, String command) {
        return buildComponent(null, text, false, SHOW_TEXT, tip, RUN_COMMAND, command);
    }

    private ITextComponent buildComponent(String text, String tip) {
        return buildComponent(null, text, false, SHOW_TEXT, tip, null, "");
    }

    private ITextComponent buildComponent(String text) {
        return buildComponent(null, text, false, null, "", null, "");
    }

    private ITextComponent buildComponent(TextFormatting colour, String text, boolean translate) {
        return buildComponent(colour, text, translate, null, "", null, "");
    }

    private ITextComponent buildComponent(TextFormatting colour, String text, boolean translate, String tip) {
        return buildComponent(colour, text, translate, SHOW_TEXT, tip, null, "");
    }

    private void chatWithHover(ICommandSender sender, String text, String hover) {
        TextComponentString msg = new TextComponentString(text);
        Style style = new Style();
        style.setHoverEvent(new HoverEvent(SHOW_TEXT, new TextComponentString(hover)));
        msg.setStyle(style);
        sender.sendMessage(msg);
    }

    private void chatWithCommand(ICommandSender sender, String text, String hover, String command, Action action) {
        TextComponentString msg = new TextComponentString(text);
        Style style = new Style();
        style.setHoverEvent(new HoverEvent(SHOW_TEXT, new TextComponentString(hover)));
        style.setClickEvent(new ClickEvent(action, command));
        msg.setStyle(style);
        sender.sendMessage(msg);
    }

    private void localizedChatWithCommand(ICommandSender sender, TextFormatting colour, String unlocalizedText, String hover, String command, Action action) {
        TextComponentTranslation msg = new TextComponentTranslation(unlocalizedText);
        Style style = new Style();
        style.setColor(colour);
        style.setHoverEvent(new HoverEvent(SHOW_TEXT, new TextComponentString(hover)));
        style.setClickEvent(new ClickEvent(action, command));
        msg.setStyle(style);
        sender.sendMessage(msg);
    }

    public static String getSourceInfo(Source source) {
        switch (source) {
            case TIREDNESS:
                return "For this source the trigger value is the number of minutes the player must be awake past the time until tired config in order for this effect to trigger.";
            case HOMESICKNESS:
                return "For this source the trigger value is the number of minutes the player must be away past the time until homesick config in order for this effect to trigger.";
            case SLEPT_AWAY:
                return "For this source the trigger value is how far (in blocks) the player must be from home for the effect to trigger.";
            case SLEPT_NO_HOME:
                return "This source does not use the trigger value but a value must still be entered.";
            case SLEPT_AT_HOME:
                return "For this source the trigger value is the required homeliness level for the effect to trigger.";
            case IN_HOME:
                return "For this source the trigger value is the required homeliness level for the effect to trigger.";
        }
        return "";
    }

    public static String getLevelInfo(EffectTrigger trigger) {
        switch (trigger.getSource()) {
            case TIREDNESS:
                return "This effect is applied when the player's time awake minus timeUntilTired is greater or equal to " + (int) trigger.getTriggerValue() + " minutes.";
            case HOMESICKNESS:
                return "This effect is applied when the player's time away from home minus timeUntilHomesick is greater or equal to " + (int) trigger.getTriggerValue() + " minutes.";
            case SLEPT_AWAY:
                return "This effect is applied when the player sleeps and is " + (int) trigger.getTriggerValue() + " or more blocks from the perimeter of their home.";
            case SLEPT_NO_HOME:
                return "This effect is applied when the player sleeps and has no permanent home. This source does not use the trigger value";
            case SLEPT_AT_HOME:
                return "This effect is applied when sleeps at home and their homeliness level is greater or equal to " + (int) trigger.getTriggerValue();
            case IN_HOME:
                return "This effect is applied when the player is at home and their  homeliness level is greater or equal to " + (int) trigger.getTriggerValue();
        }
        return "";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "list_sources", "list_effects", "remove", "add", "help");
        }
        if (args.length >= 2 && args[0].equals("add")) {
            if (args.length == 2) { //complete Effect
                return getListOfStringsMatchingLastWord(args, Potion.REGISTRY.getKeys());
            }
            else if (args.length == 3) { //complete Amplifier
                return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "etc...");
            }
            else if (args.length == 4) { //complete Source
                return getListOfStringsMatchingLastWord(args, Source.names);
            }
            else if (args.length == 5) { //complete Trigger
                return getListOfStringsMatchingLastWord(args, "1", "2", "3", "4", "5", "6", "7", "8", "9", "etc...");
            }
        }
        else if (args.length == 2 && (args[0].equals("list_sources") || args[0].equals("remove"))) {
            return getListOfStringsMatchingLastWord(args, EffectHelper.effectHandlerMap.keySet());
        }
        else if (args.length == 2 && args[0].equals("list_effects")) {
            return getListOfStringsMatchingLastWord(args, Source.names);
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    private void help(ICommandSender sender) {
        ChatHelper.message(sender, "############# Home Sweet Home Effects #############", AQUA);
        ChatHelper.message(sender, "Hover mouse over these commands in chat for details", GRAY);

        chatWithHover(sender, BLUE + "/home_sweet_home effect add [effectID] [effect amplifier] [source] [trigger value] [duration (for sleep effects)]",//
                GOLD + "Add a trigger for the specified effect and amplifier.\n" + //
                        "The following is a list of sources and the stat which can trigger them.\n" + //
                        "If the source stat is greater or equal to the specified trigger value the effect will activate\n" + //
                        BLUE + "tiredness->daysAwake, homesickness->daysAway, slept_away->distanceInBlocksFromHome, slept_at_home->homelinessLevel, in_home->homelinessLevel\n" + //
                        GRAY + "You are allowed to have multiple sources dor an effect. If multiple sources are triggered the one that applies the higher effect amplifier will take priority\n" + //
                        "Run \"/home_sweet_home effect add\" for advance auto complete options");
        chatWithHover(sender, BLUE + "/home_sweet_home effect list", GOLD + "Displays a detailed list of all configured effects and their sources.");
        chatWithHover(sender, BLUE + "/home_sweet_home effect list_sources <effectID>", GOLD + "Displays a list off all sources that trigger the specified effect.");
        chatWithHover(sender, BLUE + "/home_sweet_home effect list_effects <source>", GOLD + "Displays a list off all effects triggered by the specified source.");
        chatWithHover(sender, BLUE + "/home_sweet_home effect remove <effectID>", GOLD + "Remove all sources of the specified effect.");
        chatWithHover(sender, BLUE + "/home_sweet_home effect remove <effectID> <effect amplifier>", GOLD + "Remove all sources of the specified effect and amplifier");
        chatWithHover(sender, BLUE + "/home_sweet_home effect remove <effectID> <effect amplifier> <source> <trigger value>", GOLD + "Remove a specific effect source");
        chatWithHover(sender, BLUE + "/home_sweet_home effect help", GOLD + "Displays this help text");
    }
}
