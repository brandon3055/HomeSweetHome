package com.brandon3055.homesweethome;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.homesweethome.effects.EffectHelper;
import com.brandon3055.homesweethome.util.LogHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class ModConfig {

    //#### Properties related to mod mechanics ####

    //The homeliness level required to make a home permanent
    public static int levelForPerm = 4;
    //Homeliness gain offset before the level defined by levelForPerm (larger value decreases the base homeliness gain rate)
    public static double prePermGainOffset = 10;
    //Homeliness gain multiplier before the level defined by levelForPerm (larger value increases logarithmic curve so that the gain slows down more as the level increases)
    public static double prePermGainMultiplier = 2;
    //Homeliness gain offset after the level defined by levelForPerm (larger value decreases the base homeliness gain rate)
    public static double postPermGainOffset = 100;
    //Homeliness gain multiplier after the level defined by levelForPerm (larger value increases logarithmic curve so that the gain slows down more as the level increases)
    public static double postPermGainMultiplier = 10;
    //The minimum radius a home can have.
    public static double baseHomeRadius = 5;
    //This sets how much the home radius increases for each level of homeliness.
    public static double homeRadiusMultiplier = 1;
    //Sets how much homeliness the player gains every time they sleep
    public static double gainPerSleep = 10;
    //Sets how much homeliness the player loses when they sleep outside their home.
    public static double lossPerSleepAway = 10;
    //Sets passive homeliness gain while player is within their home (applied every second)
    public static double homePassiveGain = 0.011111;
    //Allows you to set the minutes awake to something other than 0 if the player sleeps outside their home.
    public static int resetSleepOutside = 0;
    //Sets the number of minutes in a day (used to calculate how many days a player has been awake)
    public static int minutesInDay = 20;
    //Sets how many minutes the player can be awake before starting to get tired.
    public static int timeUntilTired = 30;
    //Sets how many minutes the player can be away from home before becoming homesick.
    public static int timeUntilHomesick = 30;
    //Sets how much sleeping at home reduces homesickness 1 resets days away to 0, 0 has no effect.
    public static double homesickReductionSleep = 0.8;
    //Sets how many minutes of home sickness are removed for each minute the player spends at home
    public static double homesickReductionRate = 5;
    //Sets how many minutes are added to the time away counter when the player sleeps away from home.
    public static double homesickSleepAway = 20;
    //sets whether or not the potion icon in the top right of the screen will render for effects caused by being at home.
    public static int homeEffectsRender = 0;
    //sets whether or not the potion icon in the top right of the screen will render for effects caused by being homesick.
    public static int homesickEffectsRender = 0;
    //sets whether or not the potion icon in the top right of the screen will render for effects caused by being tired.
    public static int tiredEffectsRender = 0;
    //sets whether or not the potion icon in the top right of the screen will render for effects caused by being sleeping away from home.
    public static int sleepAwayEffectsRender = 0;
    //sets whether or not the potion icon in the top right of the screen will render for effects caused by being sleeping at home.
    public static int sleepHomeEffectsRender = 0;

    public static List<ConfigProperty> properties = new LinkedList<>();
    public static Map<String, ConfigProperty> propertyMap = new HashMap<>();

    private static boolean connectedToServer = false;

    public static void registerProperties() {
        properties.clear();
        propertyMap.clear();
        PropBuilder b = new PropBuilder();
//        b.addDouble("", () -> , value -> );
//        b.desc("");
//        b.addInt("", () -> , value -> );
//        b.desc("");

        b.addInfo("########## Home Config ##########");

        b.addInt("levelForPerm", () -> levelForPerm, value -> levelForPerm = value);
        b.desc("The homeliness level required to make a home permanent.");

        b.addDouble("baseHomeRadius", () -> baseHomeRadius, value -> baseHomeRadius = value);
        b.desc("The minimum radius a home can have.");

        b.addDouble("homeRadiusMultiplier", () -> homeRadiusMultiplier, value -> homeRadiusMultiplier = value);
        b.desc("This sets how much the home radius increases for each level of homeliness.");

        b.addInfo("########## Homeliness Config ##########");

        b.addDouble("prePermGainOffset", () -> prePermGainOffset, value -> prePermGainOffset = value);
        b.desc("Homeliness gain offset before the level defined by levelForPerm (larger value decreases the base homeliness gain rate)");

        b.addDouble("prePermGainMultiplier", () -> prePermGainMultiplier, value -> prePermGainMultiplier = value);
        b.desc("Homeliness gain multiplier before the level defined by levelForPerm (larger value increases logarithmic curve so that the gain slows down more as the level increases)");

        b.addDouble("postPermGainOffset", () -> postPermGainOffset, value -> postPermGainOffset = value);
        b.desc("Homeliness gain offset after the level defined by levelForPerm (larger value decreases the base homeliness gain rate)");

        b.addDouble("postPermGainMultiplier", () -> postPermGainMultiplier, value -> postPermGainMultiplier = value);
        b.desc("Homeliness gain multiplier after the level defined by levelForPerm (larger value increases logarithmic curve so that the gain slows down more as the level increases)");

        b.addDouble("gainPerSleep", () -> gainPerSleep, value -> gainPerSleep = value);
        b.desc("Sets how much homeliness the player gains every time they sleep");

        b.addDouble("lossPerSleepAway", () -> lossPerSleepAway, value -> lossPerSleepAway = value);
        b.desc("Sets how much homeliness the player loses when they sleep outside their home");

        b.addDouble("homePassiveGain", () -> homePassiveGain, value -> homePassiveGain = value);
        b.desc("Sets passive homeliness gain while player is within their home (applied every second)");

        b.addInfo("########## Time Config ##########");

        b.addInt("minutesInDay", () -> minutesInDay, value -> minutesInDay = value);
        b.desc("Sets the number of minutes in a day (used to calculate how many days a player has been awake)");

        b.addInt("timeUntilTired", () -> timeUntilTired, value -> timeUntilTired = value);
        b.desc("Sets how many minutes the player can be awake before starting to get tired.");

        b.addInt("timeUntilHomesick", () -> timeUntilHomesick, value -> timeUntilHomesick = value);
        b.desc("Sets how many minutes the player can be away from home before becoming homesick.");

        b.addDouble("homesickReductionSleep", () -> homesickReductionSleep, value -> homesickReductionSleep = value);
        b.desc("Sets how much sleeping at home reduces homesickness 1 resets days away to 0, 0 has no effect.");

        b.addDouble("homesickReductionRate", () -> homesickReductionRate, value -> homesickReductionRate = value);
        b.desc("Sets how many minutes of home sickness are removed for each minute the player spends at home");

        b.addInt("resetSleepOutside", () -> resetSleepOutside, value -> resetSleepOutside = value);
        b.desc("Allows you to set the minutes awake to something other than 0 if the player sleeps outside their home (so they will get tired again faster)");

        b.addDouble("homesickSleepAway", () -> homesickSleepAway, value -> homesickSleepAway = value);
        b.desc("ets how many minutes are added to the time away counter when the player sleeps away from home.");

//        b.addInfo("########## Effect Config ##########");
//
//        b.addInt("homeEffectsRender", () -> homeEffectsRender, value -> homeEffectsRender = value);
//        b.desc("Sets whether or not the potion icon in the top right of the screen will render for effects caused by being at home. ");
//        b.minMax(0, 1);
//
//        b.addInt("homesickEffectsRender", () -> homesickEffectsRender, value -> homesickEffectsRender = value);
//        b.desc("Sets whether or not the potion icon in the top right of the screen will render for effects caused by being homesick. ");
//        b.minMax(0, 1);
//
//        b.addInt("tiredEffectsRender", () -> tiredEffectsRender, value -> tiredEffectsRender = value);
//        b.desc("Sets whether or not the potion icon in the top right of the screen will render for effects caused by being tired.");
//        b.minMax(0, 1);
//
//        b.addInt("sleepAwayEffectsRender", () -> sleepAwayEffectsRender, value -> sleepAwayEffectsRender = value);
//        b.desc("Sets whether or not the potion icon in the top right of the screen will render for effects caused by being sleeping away from home. ");
//        b.minMax(0, 1);
//
//        b.addInt("sleepHomeEffectsRender", () -> sleepHomeEffectsRender, value -> sleepHomeEffectsRender = value);
//        b.desc("Sets whether or not the potion icon in the top right of the screen will render for effects caused by being sleeping at home. ");
//        b.minMax(0, 1);
    }

    public static class ConfigProperty {
        private String name;
        private boolean floating;
        private boolean isInfo = false;
        private Supplier<Double> getter = null;
        private Consumer<Double> setter = null;
        private String info = "";
        private double min = Integer.MIN_VALUE;
        private double max = Integer.MAX_VALUE;

        public ConfigProperty(String name, boolean floating, Supplier<Double> getter, Consumer<Double> setter) {
            this.name = name;
            this.floating = floating;
            this.getter = getter;
            this.setter = setter;
        }

        public ConfigProperty(String info) {
            this.info = info;
            isInfo = true;
        }

        public ConfigProperty minMax(double min, double max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public boolean isInfo() {
            return isInfo;
        }

        public String getInfo() {
            return info;
        }

        public double getValue() {
            return isInfo() ? 0 : getter.get();
        }

        public String getName() {
            return name;
        }

        public void setValue(double value) {
            if (!isInfo()) {
                setter.accept(value);
                saveConfig();
            }
        }

        private void set(double value) {
            if (!isInfo()) {
                setter.accept(value);
            }
        }

        public void setValue(String value) throws CommandException {
            if (isInfo()) return;

            if (floating) {
                setValue(CommandBase.parseDouble(value, min, max));
            }
            else {
                setValue(CommandBase.parseInt(value, (int) min, (int) max));
            }
        }

        private void toJson(JsonObject object) {
            if (!isInfo()) {
                if (floating) {
                    object.addProperty(getName(), getValue());
                }
                else {
                    object.addProperty(getName(), (int) getValue());
                }
            }
        }

        private void fromJson(JsonObject object) {
            if (!isInfo()) {
                JsonElement e = object.get(getName());
                if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                    if (floating) {
                        set(e.getAsDouble());
                    }
                    else {
                        set(e.getAsInt());
                    }
                }
            }
        }

        public void toBytes(MCDataOutput output) {
            if (isInfo()) return;
            if (floating) {
                output.writeDouble(getValue());
            }
            else {
                output.writeInt((int) getValue());
            }
        }

        public void fromBytes(MCDataInput input) {
            if (isInfo()) return;
            if (floating) {
                set(input.readDouble());
            }
            else {
                set(input.readInt());
            }
        }
    }

    private static class PropBuilder {
        private ConfigProperty prop;

        public PropBuilder() {}

        public void add(ConfigProperty prop) {
            this.prop = prop;
            properties.add(prop);
            if (!prop.isInfo()) {
                propertyMap.put(prop.name, prop);
            }
        }

        public void add(String name, boolean floating, Supplier<Double> getter, Consumer<Double> setter) {
            add(new ConfigProperty(name, floating, getter, setter));
        }

        public void addInt(String name, Supplier<Integer> getter, Consumer<Integer> setter) {
            add(new ConfigProperty(name, false, () -> (double) getter.get(), aDouble -> setter.accept(aDouble.intValue())));
        }

        public void addDouble(String name, Supplier<Double> getter, Consumer<Double> setter) {
            add(new ConfigProperty(name, true, getter, setter));
        }

        public void addInfo(String info) {
            add(new ConfigProperty(info));
        }

        public void minMax(double min, double max) {
            prop.min = min;
            prop.max = max;
        }

        public ConfigProperty get() {
            return prop;
        }

        public void desc(String info) {
            prop.setInfo(info);
        }
    }

    public static void writeConfigForSync(MCDataOutput output) {
        for (ConfigProperty property : properties) {
            property.toBytes(output);
        }
    }

    public static void receiveConfigFromServer(MCDataInput input) {
        LogHelper.dev("Received config from server!");
        for (ConfigProperty property : properties) {
            property.fromBytes(input);
        }
    }

    public static void disconnectFromServer() {
        connectedToServer = false;
        loadClientConfig();
    }

    //region #### Save / Load ####

    //#### Client config fields ####
    public static double[] homeHudPos = new double[]{0.58, 0.5};
    public static double[] statsHudPos = new double[]{0, 0.15};

    private static File config;
    private static File clientConfig;

    public static void initConfig() {
        config = new File(FileHandler.brandon3055Folder, "HomeSweetHome.json");
        clientConfig = new File(FileHandler.brandon3055Folder, "HomeSweetHome_client.json");

        registerProperties();

        if (!config.exists()) {
            saveConfig();
        }
        if (!clientConfig.exists()) {
            saveClientConfig();
        }
        loadConfig();
        loadClientConfig();
    }

    public static void saveConfig() {
        JsonObject obj = new JsonObject();

        for (ConfigProperty prop : properties) {
            prop.toJson(obj);
        }

        EffectHelper.writeEffectsToConfig(obj);

        writeJson(obj, config);
    }

    public static void loadConfig() {
        JsonObject obj = readObj(config);
        if (obj == null) return;

        for (ConfigProperty prop : properties) {
            prop.fromJson(obj);
        }

        EffectHelper.readEffectsFromConfig(obj);
    }

    public static void saveClientConfig() {
        if (connectedToServer) {
            return;
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("homeHudX", homeHudPos[0]);
        obj.addProperty("homeHudY", homeHudPos[1]);
        obj.addProperty("statsHudX", statsHudPos[0]);
        obj.addProperty("statsHudY", statsHudPos[1]);
        writeJson(obj, clientConfig);
    }

    public static void loadClientConfig() {
        if (connectedToServer) {
            return;
        }
        JsonObject cfg = readObj(clientConfig);
        if (cfg == null) return;

        JsonElement e = cfg.get("homeHudX");
        if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
            homeHudPos[0] = e.getAsDouble();
        }
        e = cfg.get("homeHudY");
        if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
            homeHudPos[1] = e.getAsDouble();
        }
        e = cfg.get("statsHudX");
        if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
            statsHudPos[0] = e.getAsDouble();
        }
        e = cfg.get("statsHudY");
        if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
            statsHudPos[1] = e.getAsDouble();
        }
    }

    private static JsonObject readObj(File file) {
        try {
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(file);
            JsonElement element = parser.parse(reader);
            IOUtils.closeQuietly(reader);
            return element.getAsJsonObject();
        }
        catch (Throwable e) {
            LogHelper.bigError("An error occurred while reading configuration!");
            e.printStackTrace();
            return null;
        }
    }

    private static void writeJson(JsonObject obj, File file) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent("  ");
            Streams.write(obj, writer);
            writer.flush();
            IOUtils.closeQuietly(writer);
        }
        catch (Throwable e) {
            LogHelper.bigError("An error occurred while saving configuration!");
            e.printStackTrace();
        }
    }

    //endregion
}
