package com.rwtema.extrautils2.interblock;

import com.rwtema.extrautils2.entity.chunkdata.ChunkDataModuleManager;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class FlatStoneHandler extends ChunkDataModuleManager<List<FlatStoneHandler.FlatStone>> {


	@Override
	public List<FlatStone> getCachedBlank() {
		return null;
	}

	@Override
	public List<FlatStone> createBlank() {
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound base, List<FlatStone> flatStones) {

	}

	@Override
	public List<FlatStone> readFromNBT(NBTTagCompound tag) {
		return null;
	}

	@Override
	public void writeData(List<FlatStone> value, XUPacketBuffer buffer) {

	}

	@Override
	public void readData(List<FlatStone> value, XUPacketBuffer buffer) {

	}

	public static class FlatStone {
	}
}
