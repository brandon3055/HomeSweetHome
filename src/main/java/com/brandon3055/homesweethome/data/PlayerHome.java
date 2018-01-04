package com.brandon3055.homesweethome.data;

import com.brandon3055.homesweethome.ModConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class PlayerHome {

    private PlayerData playerData;
    private Vec3d pos;
    private boolean isPermanent = false;
    public final Homeliness homeliness = new Homeliness(this);

    protected PlayerHome(PlayerData playerData) {
        this.playerData = playerData;
    }

    protected PlayerHome(PlayerData playerData, Vec3d pos) {
        this.playerData = playerData;
        this.pos = pos;
    }

    //region Setters / Getters

    public boolean isPermanent() {
        return isPermanent;
    }

    public void setPermanent(boolean permanent) {
        isPermanent = permanent;
        markDirty();
    }

    public void setPos(BlockPos newPos) {
        setPos(new Vec3d(newPos));
    }

    public void setPos(Vec3d newPos) {
        this.pos = newPos;
        markDirty();
    }

    public Vec3d getPos() {
        return pos;
    }

    public double getHomeRadius() {
        return ModConfig.baseHomeRadius + (homeliness.getLevel() * ModConfig.homeRadiusMultiplier);
    }

    public int getLevelHere(double distance) {
        double lvl = homeliness.getLevelWithProgress();
        double distRatio = 1D - (distance / getHomeRadius());
        distRatio *= 2;
        return (int) MathHelper.clamp(lvl * distRatio, 0, lvl);
    }

    /**
     * Expensive call use sparingly.
     */
    public double getDistance(EntityPlayer player) {
        return player.getDistance(pos.x, pos.y, pos.z);
    }

    public double getDistFromRadius(EntityPlayer player) {
        return player.getDistance(pos.x, pos.y, pos.z) - getHomeRadius();
    }

    /**
     * Expensive call use sparingly.
     */
    public double getDistance(BlockPos blockPos) {
        return blockPos.getDistance(MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(pos.z));
    }

    /**
     * This is the preferred way to check if a player is in their home as it is a lot cheaper than getDistance
     */
    public boolean isPlayerInHome(EntityPlayer player) {
        double dist = player.getDistanceSq(pos.x, pos.y, pos.z);
        double rad = getHomeRadius();
        return dist <= rad * rad;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    //endregion

    //region Save / load

    public void markDirty() {
        playerData.markDirty();
    }

    protected PlayerHome loadFromNBT(NBTTagCompound compound) {
        homeliness.loadFromNBT(compound);
        pos = new Vec3d(compound.getDouble("PosX"), compound.getDouble("PosY"), compound.getDouble("PosZ"));
        isPermanent = compound.getBoolean("Permanent");
        return this;
    }

    protected NBTTagCompound saveToNBT(NBTTagCompound compound) {
        homeliness.saveToNBT(compound);
        compound.setDouble("PosX", pos.x);
        compound.setDouble("PosY", pos.y);
        compound.setDouble("PosZ", pos.z);
        compound.setBoolean("Permanent", isPermanent);
        return compound;
    }

    //endregion

    @Override
    public String toString() {
        return String.format("PlayerHome: [PlayerID: %s, Pos: %s, Permanent: %s, Homeliness: %s]", playerData.getPlayerID(), pos, isPermanent, homeliness);
    }
}
