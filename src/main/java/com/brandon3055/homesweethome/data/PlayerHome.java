package com.brandon3055.homesweethome.data;

import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class PlayerHome {

    private PlayerData playerData;
    private Vec3d pos = new Vec3d(0, 0, 0);
    private BlockPos spawn = new BlockPos(0, -1, 0);
    private int dimension;
    private boolean isPermanent = false;
    public final Homeliness homeliness = new Homeliness(this);

    protected PlayerHome(PlayerData playerData) {
        this.playerData = playerData;
    }

    protected PlayerHome(PlayerData playerData, Vec3d pos, int dimension) {
        this.playerData = playerData;
        this.pos = pos;
        this.spawn = new BlockPos(pos);
        this.dimension = dimension;
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

    public BlockPos getSpawn() {
        if (spawn.getY() == -1) {
            return new BlockPos(pos);
        }
        return spawn;
    }

    public int getDimension() {
        return dimension;
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
        if (player.dimension != dimension) return Integer.MAX_VALUE;
        return player.getDistance(pos.x, pos.y, pos.z);
    }

    public double getDistFromRadius(EntityPlayer player) {
        if (player.dimension != dimension) return Integer.MAX_VALUE;
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
        if (player.dimension != dimension) return false;
        double dist = player.getDistanceSq(pos.x, pos.y, pos.z);
        double rad = getHomeRadius();
        return dist <= rad * rad;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void setSpawn(BlockPos spawn) {
        this.spawn = spawn;
        markDirty();
    }

    public int getLoadingRange() {
        return ModConfig.baseLoadingRadius + (int) Math.floor(homeliness.getLevel() * ModConfig.loadRadiusMultiplier);
    }

    public List<ChunkPos> getLoadingChunks() {
        List<ChunkPos> chunks = new LinkedList<>();

        int rad = getLoadingRange();
        if (rad > 0) {
            ChunkPos pos = new ChunkPos(new BlockPos(getPos()));
            if (rad == 1) {
                chunks.add(pos);
            }
            else {
                rad--;
                for (int x = -rad; x <= rad; x++) {
                    for (int z = -rad; z <= rad; z++) {
                        chunks.add(new ChunkPos(pos.x + x, pos.z + z));
                    }
                }
            }
        }

        return chunks;
    }

    //endregion

    //region Save / load

    public void markDirty() {
        playerData.markDirty();
    }

    protected PlayerHome loadFromNBT(NBTTagCompound compound) {
        homeliness.loadFromNBT(compound);
        pos = new Vec3d(compound.getDouble("PosX"), compound.getDouble("PosY"), compound.getDouble("PosZ"));
        dimension = compound.getInteger("Dimension");
        isPermanent = compound.getBoolean("Permanent");

        if (compound.hasKey("Spawn", 11) && compound.getIntArray("Spawn").length == 3) {
            int[] p = compound.getIntArray("Spawn");
            spawn = new BlockPos(p[0], p[1], p[2]);
        }
        else {
            spawn = new BlockPos(pos);
        }

        return this;
    }

    protected NBTTagCompound saveToNBT(NBTTagCompound compound) {
        homeliness.saveToNBT(compound);
        compound.setDouble("PosX", pos.x);
        compound.setDouble("PosY", pos.y);
        compound.setDouble("PosZ", pos.z);
        compound.setInteger("Dimension", dimension);
        compound.setBoolean("Permanent", isPermanent);
        compound.setIntArray("Spawn", new int[] {spawn.getX(), spawn.getY(), spawn.getZ()});
        return compound;
    }

    //endregion

    @Override
    public String toString() {
        return String.format("PlayerHome: [PlayerID: %s, Pos: %s, Permanent: %s, Homeliness: %s]", playerData.getPlayerID(), pos, isPermanent, homeliness);
    }
}
