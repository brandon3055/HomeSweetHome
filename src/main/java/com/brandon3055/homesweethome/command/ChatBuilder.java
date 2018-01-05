package com.brandon3055.homesweethome.command;

import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import static net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND;
import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;

/**
 * Created by brandon3055 on 5/01/2018.
 */
public class ChatBuilder {
    public static ITextComponent buildComponent(TextFormatting colour, String text, boolean translate, HoverEvent.Action hoverAction, String hover, ClickEvent.Action action, String command) {
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

    public static ITextComponent buildComponent(String text, String tip, ClickEvent.Action action, String actionArg) {
        return buildComponent(null, text, false, SHOW_TEXT, tip, action, actionArg);
    }

    public static ITextComponent buildComponent(String text, String tip, String command) {
        return buildComponent(null, text, false, SHOW_TEXT, tip, RUN_COMMAND, command);
    }

    public static ITextComponent buildComponent(String text, String tip) {
        return buildComponent(null, text, false, SHOW_TEXT, tip, null, "");
    }

    public static ITextComponent buildComponent(String text) {
        return buildComponent(null, text, false, null, "", null, "");
    }

    public static ITextComponent buildComponent(TextFormatting colour, String text, boolean translate) {
        return buildComponent(colour, text, translate, null, "", null, "");
    }

    public static ITextComponent buildComponent(TextFormatting colour, String text, boolean translate, String tip) {
        return buildComponent(colour, text, translate, SHOW_TEXT, tip, null, "");
    }
}
