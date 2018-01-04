package com.brandon3055.homesweethome;

import com.brandon3055.homesweethome.data.PlayerData;
import com.brandon3055.homesweethome.data.PlayerHome;
import com.brandon3055.homesweethome.effects.EffectHelper;
import com.brandon3055.homesweethome.effects.EffectHelper.EffectHandler;
import com.brandon3055.homesweethome.effects.EffectHelper.EffectTrigger;
import com.brandon3055.homesweethome.effects.EffectHelper.EffectTrigger.Source;
import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brandon3055.homesweethome.effects.EffectHelper.EffectTrigger.Source.*;

/**
 * Created by brandon3055 on 4/01/2018.
 */
public class PlayerTickHandler {
    private String username;
    private int tick = 0;
    private int second = 0;
    private Map<EffectHandler, EffectTrigger> appliedEffects = new HashMap<>();


    public PlayerTickHandler(String username) {
        this.username = username;
    }


    public void update(EntityPlayerMP player) {
        if (tick++ % 20 == 0) {
            PlayerData data = PlayerData.getPlayerData(player);
            if (data == null) return;
            PlayerHome home = data.getHome();
            tick(player, data, home, home != null && home.isPlayerInHome(player));
        }
    }

    private void tick(EntityPlayerMP player, PlayerData data, PlayerHome home, boolean isInHome) {
        appliedEffects.entrySet().removeIf(entry -> {
            boolean shouldRemove = !entry.getValue().isConditionMet(data, player);
            if (shouldRemove){
                LogHelper.dev("Removing Effect: " + entry.getKey());
                removeEffect(entry.getKey(), player);
            }
            return shouldRemove;
        });

        if (second++ % 3 == 0) {
            data.addTimeAwake(0.05);
            if (isInHome) {
                data.modifyTimeAway(-(ModConfig.homesickReductionRate * 0.05D));
            }
            else {
                data.modifyTimeAway(0.05);
            }
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
}
