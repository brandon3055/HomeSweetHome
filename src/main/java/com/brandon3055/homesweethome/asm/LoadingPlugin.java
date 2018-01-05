package com.brandon3055.homesweethome.asm;

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
