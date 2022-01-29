package com.rwtema.extrautils2.asm;

import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions(value = {"com.rwtema.extrautils2.asm.", "com.rwtema.extrautils2.asm.CoreXU2"})
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
//@IFMLLoadingPlugin.MCVersion("1.9.4")
public class CoreXU2 implements IFMLLoadingPlugin {
	protected static final ModMetadata md;
	public static boolean runtimeDeobfuscationEnabled;

	public static boolean loaded = false;
	public static Logger logger = LogManager.getLogger("ExtraUtils2CoreMod");

	static {
		md = new ModMetadata();
		md.autogenerated = false;
		md.authorList.add("RWTema");
		md.credits = "RWTema";
		md.modId = "CoreXU2";
		md.version = "1";
		md.name = "CoreXU2";
		md.description = "Core mod for Extra Utilities 2";
	}

	public CoreXU2() {
		//		super(md);
		loaded = true;
		logger.info("Extra Utils 2 Core Mod - Successfully loaded");
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{ClassTransformerHandler.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		runtimeDeobfuscationEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
		if (!runtimeDeobfuscationEnabled) {
			try {
				ClassTransformerHandler.transformers.add(LangGetterTransformer.class.newInstance());
				ClassTransformerHandler.transformers.add(ItemStackChecker.class.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
