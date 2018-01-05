package com.brandon3055.homesweethome.data;

import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 2/01/2018.
 * It may be better to use a capability for this... But i hate capabilities so no.
 */
public class DataHandler extends WorldSavedData {

    private static final String SAVE_DATA_NAME = "HomeSweetHomeData";
    private Map<String, PlayerData> playerDataMap = new LinkedHashMap<>();

    public DataHandler(String name) {
        super(name);
    }

    /**
     * Returns the save data instance for this map.
     * @param world a world instance to retrieve the map storage from.
     * @return a data handler for this map (this is per map not per dimension)
     */
    @Nullable
    public static DataHandler getDataInstance(World world) {
        MapStorage storage = world.getMapStorage();
        if (storage == null) {
            LogHelper.bigError("Detected null MapStorage! This may cause issues!");
        }
        WorldSavedData data = storage.getOrLoadData(DataHandler.class, SAVE_DATA_NAME);
        if (data != null && data instanceof DataHandler) {
            return (DataHandler) data;
        }

        data = new DataHandler(SAVE_DATA_NAME);
        storage.setData(SAVE_DATA_NAME, data);
        data.markDirty();
        storage.saveAllData();
        return (DataHandler) data;
    }

    /**
     * Gets the player data for the specified player.
     * @param player the player who's data we are retrieving.
     * @return a modifiable PlayerData instance for the specified player
     */
    public PlayerData getPlayerData(EntityPlayer player) {
        String id = player.getGameProfile().getId().toString();
        return playerDataMap.computeIfAbsent(id, s -> new PlayerData(this, id));
    }

    public void clearPlayerData(EntityPlayer player) {
        String id = player.getGameProfile().getId().toString();
        playerDataMap.remove(id);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        playerDataMap.clear();
        NBTTagList dataList = nbt.getTagList("PlayerList", 10);
        dataList.forEach(nbtBase -> {
            PlayerData data = new PlayerData(this).loadFromNBT((NBTTagCompound) nbtBase);
            playerDataMap.put(data.getPlayerID(), data);
        });
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList dataList = new NBTTagList();
        playerDataMap.forEach((id, playerData) -> {
            NBTTagCompound data = playerData.saveToNBT(new NBTTagCompound());
            dataList.appendTag(data);
        });
        compound.setTag("PlayerList", dataList);
        return compound;
    }

}
