package com.brandon3055.homesweethome.effects;

import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.effects.EffectHelper.EffectTrigger.Source;
import com.brandon3055.homesweethome.util.DelayedTask;
import com.brandon3055.homesweethome.util.LogHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Supplier;

import static com.brandon3055.homesweethome.effects.EffectHelper.EffectTrigger.Source.*;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class EffectHelper {

    public static Map<String, EffectHandler> effectHandlerMap = new HashMap<>(); //This is the main data field
    //Maps <source to <effectID to handler>>
    public static Map<Source, List<EffectHandler>> sourceEffectMap = new HashMap<>(); //This is a helper field

    public static void sleepNoHome(EntityPlayerMP player, PlayerData data) {
        updateSourceEffectsMap();
        List<EffectHandler> handlers = sourceEffectMap.get(SLEPT_NO_HOME);
        if (handlers != null) {
            handlers.forEach(handler -> handler.tryApply(SLEPT_NO_HOME, player, data));
        }
    }

    public static void sleepHasHome(EntityPlayerMP player, PlayerData data, PlayerHome home, boolean inHome) {
        if (inHome) {
            //Clear negative effects
            List<PotionEffect> activeEffects = new ArrayList<>(player.getActivePotionEffects());
            activeEffects.forEach(effect -> {
                if (effect.getPotion().isBadEffect()) {
                    player.removePotionEffect(effect.getPotion());
                }
            });

            List<EffectHandler> handlers = sourceEffectMap.get(SLEPT_AT_HOME);
            if (handlers != null) {
                handlers.forEach(handler -> handler.tryApply(SLEPT_AT_HOME, player, data));
            }
        }
        else {
            List<EffectHandler> handlers = sourceEffectMap.get(SLEPT_AWAY);
            if (handlers != null) {
                handlers.forEach(handler -> handler.tryApply(SLEPT_AWAY, player, data));
            }
        }
    }

    public static void addEffect(String effect, int amplifier, Source source, int triggerValue, int duration) throws CommandException {
        EffectHandler handler = effectHandlerMap.computeIfAbsent(effect, EffectHandler::new);
        EffectTrigger trigger = new EffectTrigger(source, triggerValue, handler);
        if (trigger.source.hasDuration) {
            trigger.setDuration(duration);
        }
        if (handler.levelTriggerMap.getOrDefault(amplifier, Collections.emptyList()).contains(trigger)) {
            throw new CommandException("This effect trigger already exists!");
        }
        handler.addLevelTrigger(amplifier, trigger);
        ModConfig.saveConfig();
    }

    //This defines the condition under which an effect is applied
    public static class EffectTrigger {
        private Source source;
        private double triggerValue;
        private EffectHandler handler;
        private int duration = 0; //Duration in ticks

        public EffectTrigger(Source source, double triggerValue, EffectHandler handler) {
            this.source = source;
            this.triggerValue = triggerValue;
            this.handler = handler;
        }

        public EffectTrigger setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public int getDuration() {
            return duration;
        }

        /**
         * @param data the player data to check
         * @return true if this condition is met by the specified player data.
         */
        public boolean isConditionMet(PlayerData data, EntityPlayer player) {
            PlayerHome home = data.getHome();
            switch (source) {
                case TIREDNESS:
                    return data.getTiredTime() >= triggerValue;
                case HOMESICKNESS:
                    return data.getHomesickTime() >= triggerValue;
                case SLEPT_AWAY:
                    return home != null && !data.getHome().isPlayerInHome(player) && home.getDistFromRadius(player) >= triggerValue;
                case SLEPT_NO_HOME:
                    return home == null || !home.isPermanent();
                case SLEPT_AT_HOME:
                    return home != null && home.isPlayerInHome(player) && home.homeliness.getLevel() >= triggerValue;
                case IN_HOME:
                    return home != null && home.isPlayerInHome(player) && home.homeliness.getLevel() >= triggerValue;
            }
            return false;
        }

        public Source getSource() {
            return source;
        }

        public void toJson(JsonObject obj) {
            obj.addProperty("source", source.name());
            obj.addProperty("trigger_value", triggerValue);
            if (source.hasDuration()) {
                obj.addProperty("duration", duration);
            }
        }

        public static EffectTrigger fromJson(JsonObject obj, EffectHandler handler) {
            Source source = Source.valueOf(obj.get("source").getAsString());
            int triggerValue = obj.get("trigger_value").getAsInt();
            EffectTrigger trigger = new EffectTrigger(source, triggerValue, handler);
            if (source.hasDuration()) {
                trigger.setDuration(obj.get("duration").getAsInt());
            }
            return trigger;
        }

        @Override
        public String toString() {
            return String.format("EffectTrigger: [source: %s, trigValue: %s, Hash: %s]", source, triggerValue, hashCode());
        }

        @Override
        public int hashCode() {
            return Objects.hash(source.ordinal(), triggerValue);
        }

        public double getTriggerValue() {
            return triggerValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof EffectTrigger)) {
                return false;
            }

            return ((EffectTrigger) obj).source == source && ((EffectTrigger) obj).triggerValue == triggerValue;
        }

        //The source of the effect
        public enum Source {
            TIREDNESS(false, false, () -> ModConfig.tiredEffectsRender == 1),
            HOMESICKNESS(false, false, () -> ModConfig.homesickEffectsRender == 1),
            SLEPT_AWAY(false, true, () -> ModConfig.sleepAwayEffectsRender == 1),
            SLEPT_NO_HOME(false, true, () -> ModConfig.sleepAwayEffectsRender == 1),
            SLEPT_AT_HOME(true, true, () -> ModConfig.sleepHomeEffectsRender == 1),
            IN_HOME(true, false, () -> ModConfig.homeEffectsRender == 1);

            private boolean isPositive;
            private boolean hasDuration;
            private Supplier<Boolean> drawAmbient;

            Source(boolean isPositive, boolean hasDuration, Supplier<Boolean> drawAmbient) {
                this.isPositive = isPositive;
                this.hasDuration = hasDuration;
                this.drawAmbient = drawAmbient;
            }

            public static final Set<String> names = new HashSet<>();

            static {
                for (Source s : values()) {
                    names.add(s.name().toLowerCase());
                }
            }

            public String commandName() {
                return name().toLowerCase();
            }

            public boolean isPositive() {
                return isPositive;
            }

            public boolean hasDuration() {
                return hasDuration;
            }

            public static Source getSource(String name) throws CommandException {
                if (!names.contains(name.toLowerCase())) {
                    throw new CommandException("The Specified source does not exist!");
                }
                return valueOf(name.toUpperCase());
            }

            public boolean drawAmbient() {
                return drawAmbient.get();
            }
        }
    }

    //there is one of these per effect type (type as in slowness, speed, nausea etc.)
    //It defines under what "EffectCondition's" the effect is active and at what levels (It maps EffectCondition -> effect level)
    public static class EffectHandler {
        public String effectID;
        public Map<Integer, List<EffectTrigger>> levelTriggerMap = new HashMap<>(); //This is the main data holder
        public Map<EffectTrigger, Integer> triggerLevelMap = new HashMap<>(); //This is a convenience data holder
        public LinkedList<Integer> levels = new LinkedList<>(); //This is a convenience data holder

        public EffectHandler(String effectID) {
            this.effectID = effectID;
        }

        public void addLevelTrigger(Integer effectLevel, EffectTrigger trigger) {
            levelTriggerMap.computeIfAbsent(effectLevel, level -> new LinkedList<>()).add(trigger);
            updateLevelMap();
            updateSourceEffectsMap();
        }

        public void removeLevelTriggers(int effectLevel) {
            levelTriggerMap.remove(effectLevel);
            updateLevelMap();
            updateSourceEffectsMap();
        }

        public boolean removeLevelTrigger(int effectLevel, Source source, double triggerValue) {
            if (!levelTriggerMap.containsKey(effectLevel)) {
                return false;
            }

            for (EffectTrigger trigger : levelTriggerMap.get(effectLevel)) {
                if (trigger.source == source && trigger.getTriggerValue() == triggerValue) {
                    levelTriggerMap.get(effectLevel).remove(trigger);
                    updateLevelMap();
                    updateSourceEffectsMap();
                    return true;
                }
            }

            return false;
        }

        private void updateLevelMap() {
            triggerLevelMap.clear();
            levels.clear();

            levelTriggerMap.forEach((level, triggers) -> {
                triggers.forEach(trigger -> triggerLevelMap.put(trigger, level));
                levels.add(level);
            });

            levels.sort(Collections.reverseOrder());
        }

        public EffectTrigger tryApply(Source source, EntityPlayerMP player, PlayerData data) {
            Potion potion = getPotion();
            if (potion == null) {
                LogHelper.bigError("Detected invalid potion handler! " + this);
                return null;
            }
            for (int level : levels) {
                for (EffectTrigger trigger : levelTriggerMap.get(level)) {
                    if (trigger.source == source && trigger.isConditionMet(data, player)) {
                        int duration = trigger.source.hasDuration() ? trigger.duration : 200;
                        PotionEffect effect = new PotionEffect(getPotion(), duration, level, false, false);

                        if (!trigger.source.hasDuration()) {
                            PotionEffect active = player.getActivePotionEffect(potion);
                            //Cant just re add the effect because that breaks things like health boost.
                            if (active != null && active.getAmplifier() == level && !active.getIsAmbient() && !active.doesShowParticles()) {
                                active.duration = 200;
                            }
                            else {
                                //This tricks the client into showing the max duration text **:** instead of the actual duration
                                effect.duration = 1000000;
                                player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), effect));
                                effect.duration = duration;
                                DelayedTask.run(5, () -> player.addPotionEffect(effect));
                            }
                        }
                        else {
                            player.addPotionEffect(effect);
                        }

                        return trigger;
                    }
                }
            }
            return null;
        }

        public void toJson(JsonObject obj) {
            obj.addProperty("effect_id", effectID);
            JsonArray levels = new JsonArray();

            for (int level : this.levels) {
                JsonObject levelData = new JsonObject();
                levelData.addProperty("level", level);
                JsonArray triggers = new JsonArray();

                for (EffectTrigger trigger : levelTriggerMap.get(level)) {
                    JsonObject cond = new JsonObject();
                    trigger.toJson(cond);
                    triggers.add(cond);
                }

                levelData.add("triggers", triggers);
                levels.add(levelData);
            }

            obj.add("level_triggers", levels);
        }

        public static EffectHandler fromJson(JsonObject obj) {
            String effectID = obj.get("effect_id").getAsString();
            EffectHandler handler = new EffectHandler(effectID);
            JsonArray levels = obj.getAsJsonArray("level_triggers");

            for (JsonElement levelE : levels) {
                JsonObject levelData = levelE.getAsJsonObject();
                int level = levelData.get("level").getAsInt();
                JsonArray triggers = levelData.getAsJsonArray("triggers");

                for (JsonElement triggerE : triggers) {
                    JsonObject triggerData = triggerE.getAsJsonObject();
                    EffectTrigger trigger = EffectTrigger.fromJson(triggerData, handler);
                    handler.addLevelTrigger(level, trigger);
                }
            }

            return handler;
        }

        public Potion getPotion() {
            return Potion.REGISTRY.getObject(new ResourceLocation(effectID));
        }

        public PotionEffect getEffect(EffectTrigger trigger) {
            Potion potion = getPotion();

            if (potion == null || !triggerLevelMap.containsKey(trigger)) {
                return null;
            }

            Source source = trigger.source;
            return new PotionEffect(potion, source.hasDuration ? trigger.duration : 20000000, triggerLevelMap.get(trigger), !source.hasDuration(), false);
        }

        @Override
        public String toString() {
            return String.format("EffectHandler: [effect: %s, Triggers: %s]", effectID, triggerLevelMap);
        }
    }

    public static void writeEffectsToConfig(JsonObject obj) {
        JsonArray array = new JsonArray();
        for (EffectHandler handler : effectHandlerMap.values()) {
            try {
                JsonObject handlerData = new JsonObject();
                handler.toJson(handlerData);
                array.add(handlerData);
            }
            catch (Throwable e) {
                LogHelper.info("Detected an error writing effect handler to config! Handler for effect: " + handler.effectID);
                e.printStackTrace();
            }
        }

        obj.add("effect_handlers", array);
    }

    public static void readEffectsFromConfig(JsonObject obj) {
        if (!obj.has("effect_handlers")) {
            return;
        }

        JsonArray array = obj.getAsJsonArray("effect_handlers");
        for (JsonElement element : array) {
            try {
                EffectHandler handler = EffectHandler.fromJson(element.getAsJsonObject());
                effectHandlerMap.put(handler.effectID, handler);
            }
            catch (Throwable e) {
                LogHelper.info("Detected an error while reading effect from config! " + element);
                e.printStackTrace();
            }
        }


        updateSourceEffectsMap();
    }

    public static void updateSourceEffectsMap() {
        sourceEffectMap.clear();
        for (EffectHandler handler : effectHandlerMap.values()) {
            for (EffectTrigger trigger : handler.triggerLevelMap.keySet()) {
                sourceEffectMap.computeIfAbsent(trigger.source, s -> new ArrayList<>()).add(handler);
            }
        }
    }
}