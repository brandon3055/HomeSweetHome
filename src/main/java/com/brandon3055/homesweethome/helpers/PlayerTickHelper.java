package com.brandon3055.homesweethome.helpers;

import com.brandon3055.homesweethome.ModConfig;
import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.helpers.EffectHelper.EffectHandler;
import com.brandon3055.homesweethome.helpers.EffectHelper.EffectTrigger;
import com.brandon3055.homesweethome.helpers.EffectHelper.EffectTrigger.Source;
import com.brandon3055.homesweethome.helpers.HSHEventHelper.Event;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;

import java.util.*;

import static com.brandon3055.homesweethome.helpers.EffectHelper.EffectTrigger.Source.*;
import static com.brandon3055.homesweethome.helpers.HSHEventHelper.Event.*;

/**
 * Created by brandon3055 on 4/01/2018.
 */
public class PlayerTickHelper {
    private String username;
    private int tick = 0;
    private long lastTime = -1;
    private Map<EffectHandler, EffectTrigger> appliedEffects = new HashMap<>();
    private Set<StateChangeTracker> trackers = new HashSet<>();


    public PlayerTickHelper(String username, EntityPlayerMP initPlayer, PlayerData initData) {
        this.username = username;

        trackers.add(new StateChangeTracker(BECOME_HOMESICK, END_HOMESICK, (player, data) -> data.isHomeSick()));
        trackers.add(new StateChangeTracker(BECOME_TIRED, END_TIRED, (player, data) -> data.isTired()));
        trackers.add(new StateChangeTracker(ENTER_HOME, LEAVE_HOME, (player, data) -> data.hasHome() && data.getHome().isPlayerInHome(player)));
        trackers.add(new StateChangeTracker(ENTER_PERM_HOME, LEAVE_PERM_HOME, (player, data) -> data.hasPermHome() && data.getHome().isPlayerInHome(player)));
        trackers.add(new StateChangeTracker(ENTER_NON_PERM_HOME, LEAVE_NON_PERM_HOME, (player, data) -> data.hasNonPermHome() && data.getHome().isPlayerInHome(player)));

        trackers.forEach(tracker -> tracker.init(initPlayer, initData));
    }


    public void update(EntityPlayerMP player) {
        if (tick++ % 20 == 0) {
            PlayerData data = PlayerData.getPlayerData(player);
            if (data == null) return;
            if (lastTime == -1) lastTime = System.currentTimeMillis();
            PlayerHome home = data.getHome();
            tick(player, data, home, home != null && home.isPlayerInHome(player));
            trackers.forEach(tracker -> tracker.update(player, data));
        }
    }

    private void tick(EntityPlayerMP player, PlayerData data, PlayerHome home, boolean isInHome) {
        appliedEffects.entrySet().removeIf(entry -> {
            boolean shouldRemove = !entry.getValue().isConditionMet(data, player);
            if (shouldRemove) {
                removeEffect(entry.getKey(), player);
            }
            return shouldRemove;
        });

        double timePassed = ((System.currentTimeMillis() - lastTime) / 1000D);
        //If more than a second (checking 10 in case of very low tps) has passed we can assume the game was paused or something so that time should not be counted.
        if (timePassed > 10) {
            timePassed = 0;
        }

        timePassed /= 60D;

        lastTime = System.currentTimeMillis();

        data.addTimeAwake(timePassed);
        if (isInHome) {
            data.modifyTimeAway(-(ModConfig.homesickReductionRate * timePassed));
        }
        else {
            data.modifyTimeAway(timePassed);
        }

        if (isInHome) {
            tryApplySource(IN_HOME, player, data);
            home.homeliness.add(ModConfig.homePassiveGain);
        }
        else {
            if (data.isTired()) {
                tryApplySource(TIREDNESS, player, data);
            }
            if (data.isHomeSick()) {
                tryApplySource(HOMESICKNESS, player, data);
            }
        }
    }

    private void tryApplySource(Source source, EntityPlayerMP player, PlayerData data) {
        List<EffectHandler> handlers = EffectHelper.sourceEffectMap.get(source);
        if (handlers != null) {
            for (EffectHandler handler : handlers) {
                handleApply(handler, handler.tryApply(source, player, data));
            }
        }
    }

    private void handleApply(EffectHandler handler, EffectTrigger trigger) {
        if (trigger != null) {
            EffectTrigger lastTrig = appliedEffects.get(handler);
            if (lastTrig == null || !lastTrig.equals(trigger)) {
                appliedEffects.put(handler, trigger);
            }
        }
    }

    public void clearEffects(EntityPlayerMP player) {
        appliedEffects.keySet().forEach(handler -> removeEffect(handler, player));
        appliedEffects.clear();
    }

    private void removeEffect(EffectHandler handler, EntityPlayerMP player) {
        Potion potion = handler.getPotion();
        if (potion != null) {
            player.removePotionEffect(potion);
        }
    }

    private static class StateChangeTracker {
        private final Event onTrue;
        private final Event onFalse;
        private final StateChecker checker;
        private boolean lastState;

        private StateChangeTracker(Event onTrue, Event onFalse, StateChecker checker) {
            this.onTrue = onTrue;
            this.onFalse = onFalse;
            this.checker = checker;
        }

        private StateChangeTracker init(EntityPlayerMP playerMP, PlayerData data) {
            lastState = checker.getState(playerMP, data);
            return this;
        }

        private void update(EntityPlayerMP player, PlayerData data) {
            boolean newState = checker.getState(player, data);
            if (newState != lastState) {
                if (newState && onTrue != null) {
                    HSHEventHelper.fireEvent(onTrue, player, data);
                }
                else if (!newState && onFalse != null) {
                    HSHEventHelper.fireEvent(onFalse, player, data);
                }
                lastState = newState;
            }
        }
    }

    private interface StateChecker {
        boolean getState(EntityPlayerMP player, PlayerData data);
    }
}
