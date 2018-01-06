package com.brandon3055.homesweethome.helpers;

import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.homesweethome.HomeSweetHome;
import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.command.ChatBuilder;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.network.PacketMakeHome;
import com.google.common.base.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;

import java.util.ArrayList;
import java.util.List;

import static com.brandon3055.homesweethome.helpers.HSHEventHelper.Event.*;
import static net.minecraft.util.text.TextFormatting.BLUE;
import static net.minecraft.util.text.TextFormatting.RED;

/**
 * Created by brandon3055 on 2/01/2018.
 */
public class SleepHelper {

    public static List<String> playersAsleep = new ArrayList<>();
    public static List<String> playersVoted = new ArrayList<>();
    private static int playersReadyToSkip = 0;
    private static int tick = 0;

    public static void updateSleepingPlayers(MinecraftServer server) {
        if (tick++ % 5 != 0) return; //Should not need to update this more than once every 5 ticks.
        PlayerList playerList = server.getPlayerList();
        List<EntityPlayerMP> players = playerList.getPlayers();

        //If none is asleep then cancel votes and do nothing.
        if (playersAsleep.isEmpty()) {
            if (!playersVoted.isEmpty()) {
                DataUtils.forEachMatch(players, player -> playersVoted.contains(player.getName()), player -> ChatHelper.translate(player, "hsh.msg.sleep.voteCanceled", RED));
                playersVoted.clear();
            }
            return;
        }

        //Check if all sleeping players are fully asleep.
        //Or if it is day for the player and they are fully asleep
        boolean allAsleep = true;
        for (EntityPlayerMP player : players) {
            if (player.isPlayerFullyAsleep() && playersAsleep.contains(player.getName())) {
                if (isDay(player.world)) {
                    onPlayerCompleteSleep(player);
                    wakePlayer(player, true, true, false);
                }
            }
            else if (playersAsleep.contains(player.getName())) {
                allAsleep = false;
            }
        }

        if (!allAsleep) {
            return;
        }

        int required = (int) Math.round(players.size() * ModConfig.playersReqSkipNight);
        int readyToSkip = playersAsleep.size() + playersVoted.size();

        //Check if enough players are asleep or have voted to skip the night.
        if (readyToSkip >= required) {
            List<World> worlds = new ArrayList<>();
            for (EntityPlayerMP player : players) {
                if (!worlds.contains(player.world)) {
                    worlds.add(player.world);
                }

                if (playersAsleep.contains(player.getName())) {
                    onPlayerCompleteSleep(player);
                    wakePlayer(player, false, false, false);
                }
            }

            for (World world : worlds) {
                if (world.getGameRules().getBoolean("doDaylightCycle")) {
                    long i = world.getWorldTime() + 24000L;
                    world.setWorldTime(i - i % 24000L);
                }
            }

            playersAsleep.clear();
            playersVoted.clear();
            readyToSkip = 0;
        }
        else if (readyToSkip > playersReadyToSkip && readyToSkip > 0) {
            double percent = Utils.round((readyToSkip / (double) players.size()) * 100, 100);
            for (EntityPlayerMP player : players) {
                player.sendMessage(new TextComponentTranslation("hsh.msg.sleep.countReadyToSkip" + (readyToSkip == 1 ? "" : "2"), readyToSkip, percent, required - readyToSkip));
                if (!playersVoted.contains(player.getName()) && !playersAsleep.contains(player.getName())) {
                    PlayerData data = PlayerData.getPlayerData(player);
                    if (data != null && data.getTimeAwake() < ModConfig.timeAwakeToSleep) {
                        player.sendMessage(ChatBuilder.buildComponent(BLUE, "hsh.msg.sleep.clickToVoteToSkip", true, HoverEvent.Action.SHOW_TEXT, "hsh.msg.sleep.clickToVote", ClickEvent.Action.RUN_COMMAND, "/home_sweet_home voteday"));
                    }
                    else {
                        player.sendMessage(ChatBuilder.buildComponent(BLUE, "hsh.msg.sleep.sleepToSkipNight", true));
                    }
                }
                else if (player.isPlayerFullyAsleep() && playersAsleep.contains(player.getName())) {
                    player.sendMessage(ChatBuilder.buildComponent(BLUE, "hsh.msg.sleep.clickToWakeUpEarly", true, HoverEvent.Action.SHOW_TEXT, "hsh.msg.sleep.clickToWakeUp", ClickEvent.Action.RUN_COMMAND, "/home_sweet_home wakeup"));

                }
            }
        }

        playersReadyToSkip = readyToSkip;
    }

    /**
     * Called when the player wakes up having slept and not canceled sleeping.
     */
    public static void onPlayerCompleteSleep(EntityPlayerMP player) {
        PlayerData data = PlayerData.getPlayerData(player);
        if (data == null) return;

        if (data.hasPermHome()) {
            PlayerHome home = data.getHome();

            if (home.isPlayerInHome(player)) { //Slept in permanent home
                //Update sleep time
                data.setTimeAwake(0);

                //decrease home sickness
                double timeAway = data.getTimeAway();
                data.setTimeAway(timeAway - (timeAway * ModConfig.homesickReductionSleep));

                //Add Homeliness
                data.getHome().homeliness.add(ModConfig.gainPerSleep);

                //apply effects
                EffectHelper.sleepHasHome(player, data, home, true);

                HSHEventHelper.fireEvent(SLEEP_HOME, player, data);
            }
            else { //Slept outside permanent home
                //Update sleep time
                data.setTimeAwake(ModConfig.resetSleepOutside);

                //increase home sickness
                double timeAway = data.getTimeAway();
                data.setTimeAway(timeAway + ModConfig.homesickSleepAway);

                //Remove Homeliness
                data.getHome().homeliness.remove(ModConfig.lossPerSleepAway);

                //Apply effects
                EffectHelper.sleepHasHome(player, data, home, false);

                HSHEventHelper.fireEvent(SLEEP_AWAY, player, data);
            }
        }
        else { //If player has no home
            data.setTimeAwake(ModConfig.resetSleepOutside);

            PlayerHome home = data.getHome();
            if (home == null || !home.isPlayerInHome(player)) {
                boolean first = home == null;
                home = data.setHome(new Vec3d(player.posX, player.posY, player.posZ));
                HSHEventHelper.fireEvent(first ? FIRST_HOME : MOVE_HOME, player, data);
            }
            else {
                HSHEventHelper.fireEvent(SLEEP_NON_PERM_HOME, player, data);
            }

            home.homeliness.add(ModConfig.gainPerSleep);
            EffectHelper.sleepNoHome(player, data);
            if (home.homeliness.getLevel() >= ModConfig.levelForPerm) {
                HomeSweetHome.network.sendTo(new PacketMakeHome(), player);
            }
        }
    }

    public static boolean isDay(World world) {
        if (world.provider instanceof WorldProviderHell || world.provider instanceof WorldProviderEnd) {
            return true;
        }
        else {
            return world.isDaytime() || !world.getGameRules().getBoolean("doDaylightCycle");
        }
    }

    //region sleep Events

    /**
     * Called by the forge player wake up event.
     * At this point if the player actually slept and did not cancel
     * that will have been handled by the custom sleep handling code.
     */
    public static void onPlayerWakeUp(EntityPlayer player) {
        if (playersAsleep.contains(player.getName())) {
            playersAsleep.remove(player.getName());
            playersReadyToSkip--;
        }
    }

    public static void onPlayerStartSleeping(EntityPlayer player) {
        if (!playersAsleep.contains(player.getName())) {
            playersAsleep.add(player.getName());
        }
        playersVoted.remove(player.getName());
    }

    public static void onPlayerVoted(EntityPlayer player) {
        PlayerData data = PlayerData.getPlayerData(player);
        if (data == null) return;

        if (data.getTimeAwake() >= ModConfig.timeAwakeToSleep) {
            ChatHelper.translate(player, "hsh.msg.sleep.youMustSleepCantVote", RED);
            return;
        }

        if (!playersVoted.contains(player.getName()) && !playersAsleep.contains(player.getName())) {
            playersVoted.add(player.getName());
        }
    }

    public static boolean shouldCancelSetSpawn(EntityPlayer player) {
        PlayerData data = PlayerData.getPlayerData(player);
        return data != null && data.hasPermHome();
    }

    //endregion

    //region Vanilla sleep logic replacement

    /**
     * This method replaces all of the required vanilla sleep logic.
     */
    public static void updateSleepState(EntityPlayer player) {
        if (player.isPlayerSleeping()) {
            ++player.sleepTimer;

            if (player.sleepTimer > 100) {
                player.sleepTimer = 100;
            }

            if (!player.world.isRemote) {
                if (!player.isInBed()) {
                    wakePlayer(player, true, true, false);
                }
//                else if (player.world.isDaytime()) { //Allow the player to sleep during the day.
//                    player.wakeUpPlayer(false, true, true);
//                }
            }
        }
        else if (player.sleepTimer > 0) {
            ++player.sleepTimer;

            if (player.sleepTimer >= 110) {
                player.sleepTimer = 0;
            }
        }
    }

    /**
     * When a player attempts to sleep in
     */
    public static void playerTrySleep(EntityPlayerMP player, BlockPos bedPos) {
        PlayerData data = PlayerData.getPlayerData(player);
        if (data == null) {
            return;
        }

        //Don't allow the player to sleep if they are not tired enough.
        if (data.getTimeAwake() < ModConfig.timeAwakeToSleep) {
            int mins = (int) (ModConfig.timeAwakeToSleep - data.getTimeAwake());
            player.sendStatusMessage(new TextComponentTranslation("hsh.msg.sleep.notTired" + (mins != 1 ? "2" : ""), mins), true);
            return;
        }

        SleepResult result = trySleep(player, bedPos);
        if (result == SleepResult.OK) {
            player.addStat(StatList.SLEEP_IN_BED);
            Packet<?> packet = new SPacketUseBed(player, bedPos);
            player.getServerWorld().getEntityTracker().sendToTracking(player, packet);
            player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            player.connection.sendPacket(packet);
            CriteriaTriggers.SLEPT_IN_BED.trigger(player);
            onPlayerStartSleeping(player);
        }

        if (result != SleepResult.OK) {
            if (result == SleepResult.NOT_POSSIBLE_NOW) {
                player.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep"), true);
            }
            else if (result == SleepResult.NOT_SAFE) {
                player.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe"), true);
            }
            else if (result == SleepResult.TOO_FAR_AWAY) {
                player.sendStatusMessage(new TextComponentTranslation("tile.bed.tooFarAway"), true);
            }
        }
    }

    private static SleepResult trySleep(EntityPlayer player, BlockPos bedPos) {
        final IBlockState state = player.world.isBlockLoaded(bedPos) ? player.world.getBlockState(bedPos) : null;
        final boolean isBed = state != null && state.getBlock().isBed(state, player.world, bedPos, player);
        final EnumFacing enumfacing = isBed && state.getBlock() instanceof BlockHorizontal ? (EnumFacing) state.getValue(BlockHorizontal.FACING) : null;

        if (!player.world.isRemote) {
            if (player.isPlayerSleeping() || !player.isEntityAlive()) {
                return SleepResult.OTHER_PROBLEM;
            }

            //Allow the player to sleep in other dimensions and during the day.
//            if (!player.world.provider.isSurfaceWorld()) {
//                return SleepResult.NOT_POSSIBLE_HERE;
//            }

//            if (player.world.isDaytime()) {
//                //return SleepResult.NOT_POSSIBLE_NOW;
//            }

            if (!player.bedInRange(bedPos, enumfacing)) {
                return SleepResult.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List<EntityMob> list = player.world.<EntityMob>getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double) bedPos.getX() - 8.0D, (double) bedPos.getY() - 5.0D, (double) bedPos.getZ() - 8.0D, (double) bedPos.getX() + 8.0D, (double) bedPos.getY() + 5.0D, (double) bedPos.getZ() + 8.0D), new SleepEnemyPredicate(player));

            if (!list.isEmpty()) {
                return SleepResult.NOT_SAFE;
            }
        }

        if (player.isRiding()) {
            player.dismountRidingEntity();
        }

        player.spawnShoulderEntities();
        player.setSize(0.2F, 0.2F);

        if (enumfacing != null) {
            float f1 = 0.5F + (float) enumfacing.getFrontOffsetX() * 0.4F;
            float f = 0.5F + (float) enumfacing.getFrontOffsetZ() * 0.4F;
            player.setRenderOffsetForSleep(enumfacing);
            player.setPosition((double) ((float) bedPos.getX() + f1), (double) ((float) bedPos.getY() + 0.6875F), (double) ((float) bedPos.getZ() + f));
        }
        else {
            player.setPosition((double) ((float) bedPos.getX() + 0.5F), (double) ((float) bedPos.getY() + 0.6875F), (double) ((float) bedPos.getZ() + 0.5F));
        }

        player.sleeping = true;
        player.sleepTimer = 0;
        player.bedLocation = bedPos;
        player.motionX = 0.0D;
        player.motionY = 0.0D;
        player.motionZ = 0.0D;

        if (!player.world.isRemote) {
            player.world.updateAllPlayersSleepingFlag();
        }

        return SleepResult.OK;
    }

    public static void wakePlayer(EntityPlayer player, boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        if (player.bedLocation == null) {
            player.bedLocation = new BlockPos(player);
        }
        player.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
        playersAsleep.remove(player.getName());
        playersVoted.remove(player.getName());
    }


    public static class SleepEnemyPredicate implements Predicate<EntityMob> {
        private final EntityPlayer player;

        public SleepEnemyPredicate(EntityPlayer playerIn) {
            this.player = playerIn;
        }

        public boolean apply(EntityMob p_apply_1_) {
            return p_apply_1_.isPreventingPlayerRest(this.player);
        }
    }

    //endregion

}
