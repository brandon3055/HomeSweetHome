package com.brandon3055.homesweethome.asm;

import com.brandon3055.homesweethome.util.LogHelper;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Created by covers1624 on 5/01/2018.
 */
@IFMLLoadingPlugin.Name ("Home Sweet Home Loading Plugin")
public class LoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		if (ForgeVersion.getBuildVersion() < 2645) {
			LogHelper.bigWarn("Home Sweet Home requires forge version 1.12.2-14.23.2.2645 or higher. Bed asm will not be loaded!");
			return new String[0];
		}

		return new String[] { "com.brandon3055.homesweethome.asm.Transformer" };
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
