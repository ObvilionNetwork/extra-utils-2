package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.blocks.BlockSynergy;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TileSynergyUnit extends TilePower implements IWorldPowerMultiplier {


	public final NBTSerializable.NBTEnum<BlockSynergy.SynergyType> synergy_type = registerNBT("synergy", new NBTSerializable.NBTEnum<>(BlockSynergy.SynergyType.BLANK));


	@Override
	public float getPower() {
		return -1;
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public float multiplier(@Nullable World world) {
		return 1;
	}

	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return this;
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeByte(synergy_type.value.ordinal());
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		synergy_type.value = BlockSynergy.SynergyType.values()[packet.readUnsignedByte()];
	}

    @Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);

	}
}
