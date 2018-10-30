package com.brandon3055.homesweethome;

import com.brandon3055.brandonscore.registry.ModFeature;
import com.brandon3055.brandonscore.registry.ModFeatures;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by brandon3055 on 2/27/2018.
 */
@GameRegistry.ObjectHolder(HomeSweetHome.MOD_ID)
@ModFeatures(modid = HomeSweetHome.MOD_ID)
public class HSHFeatures {

    @ModFeature(name = "pill", variantMap = {"0:type=sleep", "1:type=caffeine"})
    public static Pill pill = new Pill();

}
