package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.machine.TileGrouper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGrouper extends XUBlockStaticRotation {
	public BlockGrouper() {
		super(Material.ROCK);
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel model = BoxModel.newStandardBlock("extrautils2:machine/machine_base_side");
		model.setLayer(BlockRenderLayer.SOLID);
		model.setTextures(EnumFacing.UP, "extrautils2:machine/machine_base");
		model.setTextures(EnumFacing.DOWN, "extrautils2:machine/machine_base_bottom");
		Box box = model.addBox(0, 0, 0, 1, 1, 1);
		box.layer = BlockRenderLayer.TRANSLUCENT;
		box.setTexture("extrautils2:machine/machine_grouper");
		for (EnumFacing facing : EnumFacing.values()) {
			if (facing != EnumFacing.SOUTH)
				box.setInvisible(facing);
		}

		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileGrouper();
	}
}
