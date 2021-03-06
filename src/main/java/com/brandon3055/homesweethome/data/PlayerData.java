package com.brandon3055.homesweethome.data;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.homesweethome.helpers.HSHEventHelper;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

import static com.brandon3055.homesweethome.helpers.HSHEventHelper.Event.MAKE_PERM_HOME;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class PlayerData {
    private @Nullable DataHandler dataHandler;
    private String playerID;
    private String username;
    private PlayerHome home = null;
    private double timeAway = 0; //Time away in minutes
    private double timeSinceSleep = 0; //time since sleep in minutes
    public double gracePeriod = 0;

    public PlayerData(@Nullable DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    protected PlayerData(DataHandler dataHandler, String playerID, String username) {
        this.dataHandler = dataHandler;
        this.playerID = playerID;
        this.username = username;
    }

    public void markDirty() {
        if (dataHandler != null) {
            dataHandler.markDirty();
        }
    }

    public String getPlayerID() {
        return playerID;
    }

    public boolean hasPermHome() {
        return hasHome() && home.isPermanent();
    }

    public boolean hasNonPermHome() {
        return hasHome() && !home.isPermanent();
    }

    public boolean hasHome() {
        return home != null;
    }

    public PlayerHome getHome() {
        return home;
    }

    public PlayerHome setHome(Vec3d pos, int dimension) {
        home = new PlayerHome(this, pos, dimension);
        markDirty();
        return home;
    }

    public boolean isHomeSick() {
        return getHomesickTime() > 0;
    }

    public boolean isTired() {
        return getTiredTime() > 0;
    }

    /**
     * @return The number of minutes since the player became homesick
     */
    public double getHomesickTime() {
        return timeAway - ModConfig.timeUntilHomesick;
    }

    /**
     * @return The number of minutes since the player became tired
     */
    public double getTiredTime() {
        return timeSinceSleep - ModConfig.timeUntilTired;
    }

    /**
     * @return Return the time until tired as a value between 0 and 1 where 1 means timeSinceSleep is 0 and 0 means past timeUntilTired
     */
    public double getTimeUntilTiredAsDouble() {
        return MathHelper.clip(1D - (timeSinceSleep / ModConfig.timeUntilTired), 0, 1);
    }

    /**
     * @return Return the time until homesick as a value between 0 and 1 where 1 means time away is 0 and 0 means past timeUntilHomesick
     */
    public double getTimeUntilHomesickAsDouble() {
        return MathHelper.clip(1D - (timeAway / ModConfig.timeUntilHomesick), 0, 1);
    }

    /**
     * @return The number of minutes since the player last slept.
     */
    public double getTimeAwake() {
        return timeSinceSleep;
    }

    /**
     * @param timeSinceSleep Sets the number of minutes since the player last slept.
     */
    public void setTimeAwake(double timeSinceSleep) {
        this.timeSinceSleep = timeSinceSleep;
        markDirty();
    }

    /**
     * @return The number of minutes the player has been away from home.
     */
    public double getTimeAway() {
        return timeAway;
    }

    /**
     * @return The number of days away as a player readable value.
     */
    public double getDaysAwayRounded() {
        return Utils.round(timeAway / (double) ModConfig.minutesInDay, 100);
    }

    /**
     * @return The number of days since sleep as a player readable value.
     */
    public double getDaysAwakeRounded() {
        return Utils.round(timeSinceSleep / (double) ModConfig.minutesInDay, 100);
    }

    public void setTimeAway(double timeAway) {
        this.timeAway = timeAway;
        markDirty();
    }

    public void addTimeAwake(double minutes) {
        timeSinceSleep += minutes;
        markDirty();
    }

    public void modifyTimeAway(double minutes) {
        timeAway += minutes;
        if (timeAway < 0) timeAway = 0;
        markDirty();
    }

    public void tryMakeHomePermanent(EntityPlayerMP player) {
        if (hasHome() && !hasPermHome() && getHome().homeliness.getLevel() >= ModConfig.levelForPerm) {
            getHome().setPermanent(true);
            HSHEventHelper.fireEvent(MAKE_PERM_HOME, player, this);
        }
        else {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Error invalid make home packet received from client!"));
            LogHelper.bigError("Error invalid make home packet received from client!");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        markDirty();
    }

    public PlayerData loadFromNBT(NBTTagCompound compound) {
        playerID = compound.getString("PlayerID");
        username = compound.getString("Username");
        timeAway = compound.getDouble("Homesickness");
        timeSinceSleep = compound.getDouble("TimeSinceSleep");
        gracePeriod = compound.getInteger("GracePeriod");

        if (compound.hasKey("Home", 10)) {
            home = new PlayerHome(this);
            home.loadFromNBT(compound.getCompoundTag("Home"));
        }
        else {
            home = null;
        }

        return this;
    }

    public NBTTagCompound saveToNBT(NBTTagCompound compound) {
        compound.setString("PlayerID", playerID);
        compound.setString("Username", username);
        compound.setDouble("Homesickness", timeAway);
        compound.setDouble("TimeSinceSleep", timeSinceSleep);
        compound.setInteger("GracePeriod", (int) gracePeriod);

        if (home != null) {
            NBTTagCompound homeTag = home.saveToNBT(new NBTTagCompound());
            compound.setTag("Home", homeTag);
        }

        return compound;
    }

    /**
     * Convenience method to get player data
     */
    public static PlayerData getPlayerData(EntityPlayer player) {
        DataHandler data = DataHandler.getDataInstance(player.world);
        PlayerData playerData = data == null ? null : data.getPlayerData(player);
        if (playerData == null) {
            LogHelper.bigError("Detected null player data! This may cause issues!");
        }
        return playerData;
    }
}
