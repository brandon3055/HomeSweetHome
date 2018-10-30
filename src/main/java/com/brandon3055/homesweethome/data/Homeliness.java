package com.brandon3055.homesweethome.data;

import com.brandon3055.homesweethome.ChunkLoadingHandler;
import com.brandon3055.homesweethome.ModConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Created by brandon3055 on 2/01/2018.
 * This is based on the player experience system.
 */
public class Homeliness {
    private double total = 0;
    private double nextLevelProgress = 0;
    private int level = 0;
    private PlayerHome home;

    public Homeliness(PlayerHome home) {
        this.home = home;
    }

    public void add(double amount) {
        double i = Integer.MAX_VALUE - total;

        if (amount > i) {
            amount = i;
        }

        nextLevelProgress += amount / logModifier();

        for (total += amount; nextLevelProgress >= 1.0F; nextLevelProgress /= logModifier()) {
            nextLevelProgress = (nextLevelProgress - 1.0F) * logModifier();
            addL(1);
        }

        home.markDirty();
    }

    private void addL(int levels) {
        level += levels;
        if (level < 0) {
            level = 0;
            nextLevelProgress = 0.0F;
            total = 0;
        }
    }

    public void addLevel(int levels) {
        int homeRad = home.getLoadingRange();
        addL(levels);
        if (homeRad != home.getLoadingRange() && FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
            ChunkLoadingHandler.reloadPlayerChunks(home.getPlayerData().getUsername(), home, FMLCommonHandler.instance().getMinecraftServerInstance());
        }
        home.markDirty();
    }

    public void remove(double amount) {
        double newTotal = Math.max(0, total - amount);
        clear();
        add(newTotal);
    }

    /**
     * @return a logarithmic modifier based on homeliness Level used when adding to homeliness level
     */
    public double logModifier() {
        if (level >= ModConfig.levelForPerm) {
            return ModConfig.postPermGainOffset + ((level - ModConfig.levelForPerm) * ModConfig.postPermGainMultiplier);
        }
        else {
            return ModConfig.prePermGainOffset + (level * ModConfig.prePermGainMultiplier);
        }
    }

    public int getLevel() {
        return level;
    }

    public double getLevelWithProgress() {
        return getLevel() + Math.round(nextLevelProgress * 100D) / 100D;
    }

    public double getTotal() {
        return total;
    }

    public double getNextLevelProgress() {
        return nextLevelProgress;
    }

    public void clear() {
        level = 0;
        nextLevelProgress = 0.0F;
        total = 0;
    }

    protected void loadFromNBT(NBTTagCompound compound) {
        total = compound.getDouble("HomelinessTotal");
        level = compound.getInteger("HomelinessLevel");
        nextLevelProgress = compound.getDouble("HomelinessLevelP");
    }

    protected void saveToNBT(NBTTagCompound compound) {
        compound.setDouble("HomelinessTotal", total);
        compound.setInteger("HomelinessLevel", level);
        compound.setDouble("HomelinessLevelP", nextLevelProgress);
    }

    @Override
    public String toString() {
        return String.format("Homeliness: [level: %s, total: %s, next: %s]", level, total, nextLevelProgress);
    }
}