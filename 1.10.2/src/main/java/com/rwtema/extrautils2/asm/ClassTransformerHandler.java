package com.rwtema.extrautils2.asm;

import com.google.common.collect.Lists;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class ClassTransformerHandler implements IClassTransformer {
	final static ArrayList<IClassTransformer> transformers = Lists.newArrayList(
//			new LightingTransformer()
//			new TessellatorTransformer(),

	);
	static Logger logger = LogManager.getLogger("ExtraUtils2CoreMod");

	public ClassTransformerHandler() {
		logger.info("Transformer Created");
	}

	@Override
	public byte[] transform(String s, String s2, byte[] bytes) {
		StringBuilder builder = null;

		for (IClassTransformer transformer : transformers) {
			byte[] b = bytes;
			bytes = transformer.transform(s, s2, bytes);
			if (b != bytes) {
				if (builder == null)
					builder = new StringBuilder("XU Transformer: ").append(s).append("(").append(s2).append(")").append(" {").append(transformer);
				else
					builder.append(",  ").append(transformer);
			}
		}
		if (builder != null) {
			builder.append("}");
			logger.info(builder.toString());
		}
		return bytes;
	}
}